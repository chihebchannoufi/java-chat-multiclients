# 🚀 Guide de Démarrage Rapide

## Lancement du Client Web en 5 Minutes

### Étape 1: Prérequis Minimaux

- Java JDK 11+
- Apache Maven 3.6+
- Apache Tomcat 10.x

### Étape 2: Compilation

```bash
# Depuis la racine du projet
mvn clean package
```

Résultat : Le fichier `target/chat-web.war` est créé (environ 3.8 MB)

### Étape 3: Démarrage du Serveur Console

**Terminal 1** - Démarrer le serveur de chat :
```bash
cd src
javac ChatServer.java
java ChatServer
```

Vous devriez voir :
```
Serveur de chat démarré...
```

⚠️ **Important:** Le serveur DOIT être démarré AVANT d'utiliser le client web !

### Étape 4: Déploiement sur Tomcat

**Option A: Copie Automatique**
```bash
cp target/chat-web.war $CATALINA_HOME/webapps/
```

**Option B: Copie Manuelle**
Copier `target/chat-web.war` dans le dossier `webapps` de Tomcat

### Étape 5: Démarrage de Tomcat

**Linux/Mac:**
```bash
$CATALINA_HOME/bin/startup.sh
```

**Windows:**
```cmd
%CATALINA_HOME%\bin\startup.bat
```

### Étape 6: Accès à l'Application

Ouvrir un navigateur et accéder à :
```
http://localhost:8080/chat-web/
```

### Étape 7: Test Multi-Clients

1. Ouvrir plusieurs onglets/navigateurs
2. Se connecter avec des noms différents (ex: "Alice", "Bob", "Charlie")
3. Commencer à chater !

---

## 📝 Exemple de Session

### Navigateur 1 (Alice)
1. Accéder à `http://localhost:8080/chat-web/`
2. Entrer "Alice" comme nom
3. Cliquer sur "Se connecter"
4. Taper un message : "Bonjour tout le monde !"
5. Cliquer "Envoyer"

### Navigateur 2 (Bob)
1. Accéder à `http://localhost:8080/chat-web/`
2. Entrer "Bob" comme nom
3. Cliquer sur "Se connecter"
4. Voir le message d'Alice : `[HH:mm] Alice : Bonjour tout le monde !`
5. Répondre : "Salut Alice !"

Les deux utilisateurs voient les messages en temps réel ! ✅

---

## ❓ Problèmes Courants

### "Impossible de se connecter au serveur"
→ Vérifier que `ChatServer` est démarré (voir Étape 3)

### "404 Not Found"
→ Vérifier que le WAR est dans `$CATALINA_HOME/webapps/`
→ Vérifier l'URL : `http://localhost:8080/chat-web/`

### "Nom déjà utilisé"
→ Choisir un autre nom ou redémarrer le serveur

### Le port 8080 est déjà utilisé
→ Modifier le port dans `$CATALINA_HOME/conf/server.xml`
→ Ou arrêter le processus qui utilise le port 8080

---

## 📚 Pour Aller Plus Loin

- **Guide Complet:** Voir [DEPLOYMENT.md](DEPLOYMENT.md)
- **Architecture:** Voir [README.md](README.md)
- **Code Source:** Examiner `src/main/java/com/chat/servlet/ChatServlet.java`

---

## 🎯 Fonctionnalités Disponibles

✅ Connexion avec nom unique  
✅ Envoi de messages en temps réel  
✅ Réception automatique des messages (polling 500ms)  
✅ Notifications système (arrivée/départ)  
✅ Interface responsive (mobile, tablette, desktop)  
✅ Déconnexion propre  
✅ Compatible avec client console existant  

---

Bon chat ! 💬
