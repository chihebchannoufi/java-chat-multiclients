# 🏗️ Architecture et Communication - Chat Multi-Clients Web

## Vue d'Ensemble de l'Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         UTILISATEURS                            │
└─────────────────────────────────────────────────────────────────┘
                                 │
                    ┌────────────┼────────────┐
                    │            │            │
                    ▼            ▼            ▼
              ┌──────────┐ ┌──────────┐ ┌──────────┐
              │ Client 1 │ │ Client 2 │ │ Client N │
              │(Browser) │ │(Browser) │ │(Console) │
              └──────────┘ └──────────┘ └──────────┘
                    │            │            │
                  HTTP         HTTP        Socket
                    │            │            │
                    ▼            ▼            │
              ┌─────────────────────┐        │
              │   Apache Tomcat     │        │
              │  (Port 8080)        │        │
              │                     │        │
              │  ┌──────────────┐   │        │
              │  │ ChatServlet  │   │        │
              │  └──────────────┘   │        │
              │         │            │        │
              └─────────┼────────────┘        │
                        │                     │
                      Socket                Socket
                      TCP/IP               TCP/IP
                        │                     │
                        ▼                     ▼
              ┌─────────────────────────────────┐
              │      ChatServer (Console)       │
              │         Port 12345              │
              │                                 │
              │  ┌─────────┐   ┌─────────┐     │
              │  │ Thread1 │   │ Thread2 │ ... │
              │  └─────────┘   └─────────┘     │
              └─────────────────────────────────┘
```

## Flux de Données Détaillé

### 1. Connexion d'un Client Web

```
┌─────────┐                  ┌──────────┐                 ┌────────────┐
│Browser  │                  │ Servlet  │                 │ChatServer  │
└────┬────┘                  └────┬─────┘                 └─────┬──────┘
     │                            │                              │
     │ POST /chat/connect         │                              │
     │ username=Alice             │                              │
     ├───────────────────────────>│                              │
     │                            │                              │
     │                            │ new Socket(localhost, 12345) │
     │                            ├─────────────────────────────>│
     │                            │                              │
     │                            │ <--TCP Connection OK-->      │
     │                            │<─────────────────────────────┤
     │                            │                              │
     │                            │ "Entrez votre nom : "        │
     │                            │<─────────────────────────────┤
     │                            │                              │
     │                            │ out.println("Alice")         │
     │                            ├─────────────────────────────>│
     │                            │                              │
     │                            │ # Alice a rejoint le chat    │
     │                            │<─────────────────────────────┤
     │                            │                              │
     │                            │ [Store in HttpSession:       │
     │                            │  - socket, out, in,          │
     │                            │  - messageQueue, username]   │
     │                            │                              │
     │ {"success": true}          │                              │
     │<───────────────────────────┤                              │
     │                            │                              │
     │ Redirect to chat.jsp       │                              │
     │                            │                              │
```

### 2. Envoi d'un Message

```
┌─────────┐                  ┌──────────┐                 ┌────────────┐
│Browser  │                  │ Servlet  │                 │ChatServer  │
└────┬────┘                  └────┬─────┘                 └─────┬──────┘
     │                            │                              │
     │ POST /chat/send            │                              │
     │ message=Bonjour!           │                              │
     ├───────────────────────────>│                              │
     │                            │                              │
     │                            │ [Get from session:           │
     │                            │  PrintWriter out]            │
     │                            │                              │
     │                            │ out.println("Bonjour!")      │
     │                            ├─────────────────────────────>│
     │                            │                              │
     │                            │                              │
     │ {"success": true}          │                              │
     │<───────────────────────────┤                              │
     │                            │                              │
     │                            │      [Server broadcasts:     │
     │                            │       to ALL clients]        │
     │                            │                              │
```

### 3. Réception des Messages (Polling)

```
┌─────────┐                  ┌──────────┐                 ┌────────────┐
│Browser  │                  │ Servlet  │                 │ChatServer  │
└────┬────┘                  └────┬─────┘                 └─────┬──────┘
     │                            │                              │
     │                            │ [Receiver Thread running:    │
     │                            │  continuously reads socket   │
     │                            │  and fills messageQueue]     │
     │                            │<─────────────────────────────┤
     │                            │ "[12:30] Bob : Salut Alice!" │
     │                            │                              │
     │                            │ messageQueue.offer(msg)      │
     │                            │                              │
     │ GET /chat/receive          │                              │
     │ (every 500ms)              │                              │
     ├───────────────────────────>│                              │
     │                            │                              │
     │                            │ [Get from session:           │
     │                            │  messageQueue]               │
     │                            │                              │
     │                            │ messages = queue.poll()      │
     │                            │                              │
     │ {"success": true,          │                              │
     │  "messages": [             │                              │
     │   "[12:30] Bob : Salut!"   │                              │
     │  ]}                        │                              │
     │<───────────────────────────┤                              │
     │                            │                              │
     │ [JavaScript displays       │                              │
     │  message in UI]            │                              │
```

### 4. Déconnexion

```
┌─────────┐                  ┌──────────┐                 ┌────────────┐
│Browser  │                  │ Servlet  │                 │ChatServer  │
└────┬────┘                  └────┬─────┘                 └─────┬──────┘
     │                            │                              │
     │ POST /chat/disconnect      │                              │
     ├───────────────────────────>│                              │
     │                            │                              │
     │                            │ out.println("/quit")         │
     │                            ├─────────────────────────────>│
     │                            │                              │
     │                            │ socket.close()               │
     │                            ├─────────────────────────────>│
     │                            │                              │
     │                            │ session.invalidate()         │
     │                            │                              │
     │                            │      <> Alice a quitté       │
     │                            │<─────────────────────────────┤
     │                            │      [Broadcast to others]   │
     │                            │                              │
     │ {"success": true}          │                              │
     │<───────────────────────────┤                              │
     │                            │                              │
```

## Composants Clés

### 1. ChatServlet.java

**Responsabilités:**
- Gérer les connexions socket au serveur
- Maintenir une connexion par session HTTP
- Envoyer les messages au serveur
- Recevoir les messages via un thread dédié
- Stocker les messages dans une file thread-safe

**Endpoints:**
- `POST /chat/connect` - Établir connexion
- `POST /chat/send` - Envoyer un message
- `GET /chat/receive` - Récupérer messages en attente
- `POST /chat/disconnect` - Fermer connexion

### 2. Session HTTP

**Contenu:**
```java
session.setAttribute("socket", socket);              // Socket TCP
session.setAttribute("out", PrintWriter);            // Stream sortie
session.setAttribute("in", BufferedReader);          // Stream entrée
session.setAttribute("messageQueue", Queue);         // File messages
session.setAttribute("username", String);            // Nom utilisateur
session.setAttribute("receiverThread", Thread);      // Thread réception
```

**Durée de vie:** 30 minutes (configurable dans web.xml)

### 3. Thread de Réception

```java
Thread receiverThread = new Thread(() -> {
    try {
        String message;
        while ((message = in.readLine()) != null) {
            messageQueue.offer(message);  // Thread-safe
        }
    } catch (IOException e) {
        // Connexion fermée
    }
});
receiverThread.setDaemon(true);  // Se termine avec la JVM
receiverThread.start();
```

**Caractéristiques:**
- Thread daemon (terminé automatiquement)
- Lit continuellement depuis le socket
- Stocke dans ConcurrentLinkedQueue (thread-safe)
- Pas de blocage du thread principal

### 4. Polling AJAX

```javascript
setInterval(async () => {
    const response = await fetch('chat/receive');
    const data = await response.json();
    
    if (data.success && data.messages.length > 0) {
        data.messages.forEach(msg => displayMessage(msg));
    }
}, 500);  // Toutes les 500ms
```

**Avantages:**
- Simple à implémenter
- Compatible avec tous les navigateurs
- Fonctionne derrière proxies/firewalls
- Pas de modification du serveur

**Alternative:** WebSocket (nécessiterait de modifier le serveur)

## Gestion Multi-Clients

### Côté Serveur (ChatServer - INCHANGÉ)

Le serveur gère tous les clients de la même manière:

```java
// Collections thread-safe
private static Set<PrintWriter> clientWriters;
private static Map<PrintWriter, String> clientNames;

// Broadcast à tous les clients
private void broadcast(String message, PrintWriter exclude) {
    synchronized (clientWriters) {
        for (PrintWriter writer : clientWriters) {
            if (writer != exclude) {
                writer.println(message);
            }
        }
    }
}
```

**Principe:**
- 1 Client Web = 1 Session HTTP = 1 Socket = 1 Thread serveur
- 1 Client Console = 1 Socket = 1 Thread serveur
- Tous égaux du point de vue du serveur !

### Côté Servlet

Chaque session HTTP est indépendante:

```
Session 1 (Alice)  →  Socket 1  →  Thread Serveur 1
Session 2 (Bob)    →  Socket 2  →  Thread Serveur 2
Session 3 (Charlie)→  Socket 3  →  Thread Serveur 3
```

Pas de partage de données entre sessions (isolation complète)

## Sécurité

### Mesures Implémentées

1. **HttpOnly Cookies** (web.xml)
   ```xml
   <cookie-config>
       <http-only>true</http-only>
   </cookie-config>
   ```

2. **Validation des Entrées**
   ```java
   if (username == null || username.trim().isEmpty()) {
       response.setStatus(SC_BAD_REQUEST);
       return;
   }
   ```

3. **Échappement JSON**
   ```java
   String escapedMsg = msg.replace("\\", "\\\\")
                          .replace("\"", "\\\"")
                          .replace("\n", "\\n");
   ```

4. **Gestion des Sessions**
   - Timeout automatique (30 min)
   - Invalidation à la déconnexion
   - Vérification de session avant chaque opération

### Améliorations Possibles

- HTTPS (SSL/TLS)
- Authentication/Authorization
- Rate limiting
- Input sanitization
- CSRF tokens
- WebSocket avec authentification

## Performance

### Optimisations

1. **Thread Daemon** - Se termine automatiquement
2. **ConcurrentLinkedQueue** - Operations O(1), thread-safe
3. **Polling Intelligent** - 500ms (ajustable)
4. **Session HTTP** - Réutilisation des connexions
5. **Buffering** - BufferedReader/PrintWriter

### Limites

- **Polling:** Latence maximum = 500ms
- **Scalabilité:** Limitée par le nombre de threads Tomcat
- **Mémoire:** Une queue par session

### Améliorations Futures

- WebSocket pour latence < 50ms
- Long-polling pour réduire la charge
- Server-Sent Events (SSE)
- Redis pour partager les messages entre instances

## Comparaison Client Console vs Client Web

| Aspect              | Client Console      | Client Web           |
|---------------------|---------------------|----------------------|
| Interface           | Terminal            | Navigateur           |
| Communication       | Socket direct       | HTTP → Servlet → Socket |
| Threads             | 1 receiver          | 1 receiver (servlet) |
| État                | Variables locales   | Session HTTP         |
| Réception           | Blocante (readline) | Polling AJAX         |
| Envoi               | Scanner input       | Formulaire HTML      |
| Multi-instance      | Multi-terminal      | Multi-onglet         |
| Compatibilité       | Serveur inchangé    | Serveur inchangé     |

**Conclusion:** Les deux types de clients peuvent coexister et communiquer !

---

**Version:** 1.0  
**Dernière mise à jour:** Février 2026
