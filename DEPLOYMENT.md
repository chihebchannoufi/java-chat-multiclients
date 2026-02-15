# 📘 Guide de Déploiement - Chat Multi-Clients Web

## 📋 Table des matières
- [Prérequis](#prérequis)
- [Architecture du Projet](#architecture-du-projet)
- [Installation](#installation)
- [Compilation et Déploiement](#compilation-et-déploiement)
- [Lancement de l'Application](#lancement-de-lapplication)
- [Explication Technique](#explication-technique)
- [Tests Multi-Clients](#tests-multi-clients)
- [Dépannage](#dépannage)

---

## 🔧 Prérequis

### Logiciels Requis

1. **Java Development Kit (JDK) 11 ou supérieur**
   ```bash
   java -version
   # Devrait afficher: java version "11" ou supérieur
   ```

2. **Apache Maven 3.6 ou supérieur**
   ```bash
   mvn -version
   ```

3. **Apache Tomcat 10.x**
   - Télécharger depuis: https://tomcat.apache.org/download-10.cgi
   - Décompresser dans un répertoire (ex: `/opt/tomcat` ou `C:\tomcat`)

### Configuration de Tomcat

Définir la variable d'environnement `CATALINA_HOME`:

**Linux/Mac:**
```bash
export CATALINA_HOME=/chemin/vers/tomcat
export PATH=$PATH:$CATALINA_HOME/bin
```

**Windows:**
```cmd
set CATALINA_HOME=C:\chemin\vers\tomcat
set PATH=%PATH%;%CATALINA_HOME%\bin
```

---

## 📁 Architecture du Projet

```
java-chat-multiclients/
│
├── src/
│   ├── ChatServer.java              # Serveur socket (INCHANGÉ)
│   ├── ChatClient.java              # Client console (ANCIEN, optionnel)
│   └── main/
│       └── java/
│           └── com/
│               └── chat/
│                   └── servlet/
│                       └── ChatServlet.java  # Servlet principal
│
├── webapp/
│   ├── index.jsp                    # Page de connexion
│   ├── chat.jsp                     # Interface de chat
│   ├── error.jsp                    # Page d'erreur
│   ├── css/
│   │   └── style.css               # Styles CSS
│   └── WEB-INF/
│       ├── web.xml                 # Configuration servlet
│       └── lib/                    # Bibliothèques (auto-générées)
│
├── pom.xml                          # Configuration Maven
└── README.md                        # Documentation
```

---

## 🚀 Installation

### Étape 1: Cloner le Projet

```bash
git clone https://github.com/chihebchannoufi/java-chat-multiclients.git
cd java-chat-multiclients
```

### Étape 2: Vérifier la Structure

Assurez-vous que tous les fichiers sont présents:
```bash
ls -R
```

---

## 🔨 Compilation et Déploiement

### Méthode 1: Maven (Recommandée)

#### 1. Compiler le projet
```bash
mvn clean package
```

Cela va:
- Compiler le servlet Java
- Créer le fichier WAR dans le dossier `target/`
- Le fichier sera nommé `chat-web.war`

#### 2. Déployer sur Tomcat

**Option A: Copie manuelle**
```bash
cp target/chat-web.war $CATALINA_HOME/webapps/
```

**Option B: Via le manager Tomcat**
- Accéder à http://localhost:8080/manager/html
- Utiliser l'interface pour déployer le fichier WAR

### Méthode 2: Compilation Manuelle (Sans Maven)

#### 1. Compiler le Servlet
```bash
# Télécharger Jakarta Servlet API JAR
# Puis compiler:
javac -cp servlet-api.jar:. -d webapp/WEB-INF/classes src/main/java/com/chat/servlet/ChatServlet.java

# Créer le WAR manuellement
cd webapp
jar -cvf ../chat-web.war *
```

#### 2. Déployer
```bash
cp chat-web.war $CATALINA_HOME/webapps/
```

---

## ▶️ Lancement de l'Application

### Étape 1: Démarrer le Serveur de Chat (Console)

**Important:** Le serveur socket DOIT être démarré en premier!

```bash
cd src
javac ChatServer.java
java ChatServer
```

Vous devriez voir:
```
Serveur de chat démarré...
```

Le serveur écoute sur le port **12345**.

### Étape 2: Démarrer Tomcat

**Linux/Mac:**
```bash
$CATALINA_HOME/bin/startup.sh
```

**Windows:**
```cmd
%CATALINA_HOME%\bin\startup.bat
```

Vérifier que Tomcat est démarré:
```bash
# Consulter les logs
tail -f $CATALINA_HOME/logs/catalina.out
```

### Étape 3: Accéder à l'Application Web

Ouvrir un navigateur et accéder à:
```
http://localhost:8080/chat-web/
```

### Étape 4: Se Connecter

1. Entrer un nom d'utilisateur unique
2. Cliquer sur "Se connecter"
3. Vous êtes redirigé vers la page de chat

### Étape 5: Tester avec Plusieurs Clients

Ouvrir plusieurs fenêtres/onglets de navigateur (ou différents navigateurs):
```
http://localhost:8080/chat-web/
```

Chaque client peut:
- Se connecter avec un nom différent
- Envoyer des messages
- Recevoir les messages des autres en temps réel

---

## 🔍 Explication Technique

### 1. Communication Servlet ↔ Serveur Socket

#### Architecture
```
[Navigateur] → [HTTP] → [Servlet] → [Socket TCP] → [ChatServer]
     ↑                       ↓
     └─── [AJAX Polling] ────┘
```

#### Flux de Connexion

1. **Client se connecte:**
   - L'utilisateur entre son nom sur `index.jsp`
   - JavaScript envoie une requête POST à `/chat/connect`
   - Le servlet crée un socket TCP vers le serveur (port 12345)
   - Le servlet envoie le nom d'utilisateur au serveur
   - Le serveur valide le nom (unicité)
   - Le socket, les streams I/O et la file de messages sont stockés dans la session HTTP

2. **Envoi de messages:**
   - L'utilisateur tape un message dans `chat.jsp`
   - JavaScript envoie POST à `/chat/send`
   - Le servlet récupère le `PrintWriter` de la session
   - Le message est envoyé au serveur via le socket
   - Le serveur diffuse le message à tous les clients connectés

3. **Réception de messages:**
   - Un thread côté servlet lit en continu depuis le socket
   - Les messages reçus sont placés dans une `ConcurrentLinkedQueue`
   - JavaScript effectue du polling (GET `/chat/receive` toutes les 500ms)
   - Le servlet retourne les messages en attente en JSON
   - JavaScript affiche les nouveaux messages

#### Code Clé dans ChatServlet.java

```java
// Création du socket et connexion au serveur
Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

// Thread de réception (daemon)
Thread receiverThread = new Thread(() -> {
    String message;
    while ((message = in.readLine()) != null) {
        messageQueue.offer(message);  // File thread-safe
    }
});
receiverThread.setDaemon(true);
receiverThread.start();

// Stockage dans la session HTTP
session.setAttribute("socket", socket);
session.setAttribute("out", out);
session.setAttribute("messageQueue", messageQueue);
```

### 2. Gestion Multi-Clients Côté Serveur

Le serveur (`ChatServer.java`) n'est **PAS MODIFIÉ**. Il continue à:
- Accepter les connexions socket
- Créer un thread par client
- Diffuser les messages à tous les clients connectés

**Point important:** Chaque client web (session HTTP) = 1 connexion socket = 1 thread serveur

### 3. Gestion des Sessions

#### Session HTTP (côté servlet)
```java
HttpSession session = request.getSession(true);
```

Contient:
- `socket`: La connexion Socket au serveur
- `out`: PrintWriter pour envoyer des messages
- `in`: BufferedReader pour recevoir des messages
- `messageQueue`: ConcurrentLinkedQueue des messages reçus
- `username`: Nom de l'utilisateur
- `receiverThread`: Thread de réception

#### Durée de vie
- Session créée lors de la connexion
- Session maintenue tant que le client est connecté
- Session invalidée lors de la déconnexion ou timeout (30 min)

#### Déconnexion propre
```java
// Envoi de /quit au serveur
out.println("/quit");

// Fermeture du socket
socket.close();

// Invalidation de la session
session.invalidate();
```

### 4. Protocole de Communication

Le serveur utilise un protocole texte simple:

**Messages système:**
- `# <username> a rejoint le chat.` → Utilisateur connecté
- `<> <username> a quitté le chat.` → Utilisateur déconnecté

**Messages utilisateur:**
- `[HH:mm] <username> : <message>` → Message normal

Le client web détecte le type de message (préfixe `#`, `<>` ou `[`) et applique le style CSS approprié.

### 5. Polling vs WebSocket

**Choix du Polling:**
- Compatible avec tous les navigateurs
- Pas de modification du serveur socket
- Simple à implémenter
- Fonctionne derrière la plupart des proxies/firewalls

**Alternative WebSocket:**
Nécessiterait de modifier le serveur ou d'ajouter une couche intermédiaire.

---

## 👥 Tests Multi-Clients

### Scénario de Test 1: Deux Clients Locaux

1. Démarrer le serveur console
2. Ouvrir deux onglets de navigateur
3. Se connecter avec "Alice" dans le premier
4. Se connecter avec "Bob" dans le second
5. Alice envoie: "Bonjour Bob!"
6. Vérifier que Bob reçoit le message
7. Bob répond: "Salut Alice!"
8. Vérifier qu'Alice reçoit la réponse

### Scénario de Test 2: Mix Console + Web

1. Démarrer le serveur console
2. Lancer un client console (`java ChatClient`)
3. Ouvrir un navigateur et se connecter
4. Les deux clients doivent pouvoir communiquer

### Scénario de Test 3: Déconnexion

1. Connecter plusieurs clients
2. Fermer l'onglet d'un client (ou cliquer Déconnexion)
3. Les autres clients doivent voir: `<> Username a quitté le chat.`

### Scénario de Test 4: Nom Unique

1. Se connecter avec "Alice"
2. Dans un autre onglet, essayer de se connecter avec "Alice"
3. Devrait afficher: "Nom déjà utilisé"

---

## 🛠️ Dépannage

### Problème: "Impossible de se connecter au serveur"

**Solutions:**
1. Vérifier que `ChatServer` est démarré:
   ```bash
   ps aux | grep ChatServer
   ```

2. Vérifier que le port 12345 est libre:
   ```bash
   netstat -an | grep 12345
   ```

3. Vérifier l'adresse dans `ChatServlet.java`:
   ```java
   private static final String SERVER_ADDRESS = "127.0.0.1";
   ```

### Problème: "404 Not Found"

**Solutions:**
1. Vérifier que le WAR est déployé:
   ```bash
   ls -l $CATALINA_HOME/webapps/chat-web.war
   ls -l $CATALINA_HOME/webapps/chat-web/
   ```

2. Vérifier l'URL: `http://localhost:8080/chat-web/`

3. Consulter les logs Tomcat:
   ```bash
   tail -f $CATALINA_HOME/logs/catalina.out
   ```

### Problème: "Nom déjà utilisé" en boucle

**Solution:**
Le nom est peut-être déjà pris. Utiliser un autre nom ou redémarrer le serveur console.

### Problème: Messages non reçus en temps réel

**Solutions:**
1. Ouvrir la console du navigateur (F12) et vérifier les erreurs JavaScript
2. Vérifier que le polling fonctionne (requêtes GET régulières à `/chat/receive`)
3. Augmenter la fréquence de polling dans `chat.jsp`:
   ```javascript
   setInterval(async () => { ... }, 500);  // 500ms → 250ms
   ```

### Problème: Erreur de compilation Maven

**Solutions:**
1. Vérifier la version Java:
   ```bash
   java -version
   mvn -version
   ```

2. Nettoyer et recompiler:
   ```bash
   mvn clean
   mvn package
   ```

3. Vérifier que toutes les dépendances sont téléchargées:
   ```bash
   mvn dependency:resolve
   ```

### Problème: Tomcat ne démarre pas

**Solutions:**
1. Vérifier qu'aucun autre processus n'utilise le port 8080:
   ```bash
   netstat -an | grep 8080
   ```

2. Changer le port dans `$CATALINA_HOME/conf/server.xml`:
   ```xml
   <Connector port="8081" protocol="HTTP/1.1" ... />
   ```

3. Consulter les logs d'erreur:
   ```bash
   cat $CATALINA_HOME/logs/catalina.out
   ```

---

## 📚 Ressources Supplémentaires

### Documentation
- [Jakarta Servlet Specification](https://jakarta.ee/specifications/servlet/)
- [Apache Tomcat Documentation](https://tomcat.apache.org/tomcat-10.0-doc/)
- [Maven Getting Started](https://maven.apache.org/guides/getting-started/)

### Architecture
- Le serveur socket reste **totalement inchangé**
- La communication serveur-client utilise le protocole existant
- Chaque session web = 1 connexion socket distincte
- Les threads de réception sont des daemon threads (terminés automatiquement)

### Sécurité
- Sessions HTTP sécurisées (http-only cookies)
- Validation des entrées utilisateur
- Timeout de session (30 minutes)
- Fermeture propre des sockets

---

## 📞 Support

Pour toute question ou problème:
1. Consulter cette documentation
2. Vérifier les logs (Tomcat et serveur console)
3. Ouvrir une issue sur GitHub

---

**Version:** 1.0  
**Dernière mise à jour:** Février 2026
