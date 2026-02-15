# 💬 Chat Multi-Clients en Java

## 📌 Description du projet
Ce projet est une application de **chat multi-clients** développée en **Java** selon une architecture **client/serveur**.

Le serveur accepte plusieurs clients simultanément grâce à l'utilisation des **threads**.  
Chaque message envoyé par un client est **diffusé à tous les autres clients connectés**.

## 🌟 Nouveauté : Interface Web

Le projet propose maintenant **deux modes de client** :
- ✅ **Client Console** (original) - Interface en ligne de commande
- 🆕 **Client Web** - Interface web moderne avec HTML, CSS, Servlets et JSP

**Le serveur reste inchangé** et fonctionne toujours en console !

---

## 🚀 Démarrage Rapide

### Option 1 : Client Console (Original)

#### Compilation et exécution du serveur
```bash
cd src 
javac ChatServer.java
java ChatServer
```

#### Compilation et exécution du client console
```bash
cd src
javac ChatClient.java
java ChatClient
```

### Option 2 : Client Web (Nouveau)

#### 1. Démarrer le serveur (obligatoire)
```bash
cd src
javac ChatServer.java
java ChatServer
```

#### 2. Compiler et déployer le client web
```bash
# Avec Maven (recommandé)
mvn clean package

# Copier le WAR dans Tomcat
cp target/chat-web.war $CATALINA_HOME/webapps/
```

#### 3. Démarrer Tomcat
```bash
# Linux/Mac
$CATALINA_HOME/bin/startup.sh

# Windows
%CATALINA_HOME%\bin\startup.bat
```

#### 4. Accéder à l'application
Ouvrir un navigateur : `http://localhost:8080/chat-web/`

---

## 📁 Structure du Projet

```
java-chat-multiclients/
│
├── src/
│   ├── ChatServer.java                    # Serveur socket (inchangé)
│   ├── ChatClient.java                    # Client console (original)
│   └── main/java/com/chat/servlet/
│       └── ChatServlet.java              # Servlet pour client web
│
├── webapp/                                # Application web
│   ├── index.jsp                         # Page de connexion
│   ├── chat.jsp                          # Interface de chat
│   ├── error.jsp                         # Page d'erreur
│   ├── css/style.css                     # Styles CSS modernes
│   └── WEB-INF/
│       └── web.xml                       # Configuration servlet
│
├── pom.xml                               # Configuration Maven
├── DEPLOYMENT.md                         # Guide de déploiement détaillé
└── README.md                             # Ce fichier
```

---

## 📖 Documentation Complète

Pour le **guide de déploiement complet** incluant :
- Configuration détaillée de Tomcat
- Instructions de compilation Maven
- Explications techniques (comment la Servlet communique avec le serveur)
- Gestion des sessions
- Tests multi-clients
- Dépannage

👉 **Consultez [DEPLOYMENT.md](DEPLOYMENT.md)**

---

## 🎯 Fonctionnalités du Client Web

### Interface Moderne
- 🎨 Design responsive (mobile, tablette, desktop)
- 🌈 Interface colorée et intuitive
- ⚡ Animations et transitions fluides

### Fonctionnalités
- 👤 Connexion avec nom d'utilisateur unique
- 💬 Envoi et réception de messages en temps réel
- 📊 Affichage des notifications (arrivée/départ d'utilisateurs)
- 🔄 Mise à jour automatique des messages (polling AJAX)
- 🚪 Déconnexion propre
- 📱 Support multi-appareils

---

## 🏗️ Architecture Technique

### Client Web → Serveur Console

```
┌─────────────┐         ┌──────────────┐         ┌──────────────┐
│  Navigateur │  HTTP   │   Servlet    │  Socket │ ChatServer   │
│  (HTML/JS)  │ ◄─────► │ (Java)       │ ◄─────► │ (Console)    │
└─────────────┘         └──────────────┘         └──────────────┘
     AJAX                    Session                  Thread
   Polling                   HTTP                   per Client
```

### Communication
1. Le **navigateur** envoie des requêtes HTTP au **Servlet**
2. Le **Servlet** maintient une connexion **Socket TCP** avec le serveur
3. Le **serveur console** gère tous les clients (web et console) de la même manière
4. Les messages sont diffusés à tous les clients connectés

### Avantages
- ✅ **Pas de modification du serveur** - Le code serveur reste identique
- ✅ **Compatible** - Les clients console et web peuvent communiquer ensemble
- ✅ **Multi-utilisateurs** - Chaque session web = 1 connexion socket
- ✅ **Temps réel** - Réception des messages via polling AJAX rapide

---

## 🔧 Prérequis

### Pour le Client Console
- Java JDK 11 ou supérieur

### Pour le Client Web
- Java JDK 11 ou supérieur
- Apache Maven 3.6+
- Apache Tomcat 10.x
- Navigateur web moderne (Chrome, Firefox, Safari, Edge)

---

## 🧪 Tests Multi-Clients

Vous pouvez mélanger les types de clients :

```bash
# Terminal 1 : Serveur
java ChatServer

# Terminal 2 : Client console
java ChatClient

# Navigateur 1 : Client web
http://localhost:8080/chat-web/

# Navigateur 2 : Autre client web
http://localhost:8080/chat-web/
```

Tous les clients peuvent communiquer entre eux en temps réel ! 🎉

---

## 📚 Technologies Utilisées

### Serveur
- Java Socket API
- Threads Java
- Collections thread-safe

### Client Console
- Java I/O Streams
- Scanner

### Client Web
- **Backend :** Jakarta Servlets, JSP
- **Frontend :** HTML5, CSS3, JavaScript (Vanilla)
- **Build :** Maven
- **Serveur :** Apache Tomcat 10.x

---

## 🤝 Contribution

Les contributions sont les bienvenues ! N'hésitez pas à :
- Signaler des bugs
- Proposer des améliorations
- Soumettre des pull requests

---

## 📄 Licence

Ce projet est open source et disponible sous licence MIT.

---

## 👨‍💻 Auteur

Chiheb Channoufi

---

## 📞 Support

- Pour des questions sur le **déploiement** : voir [DEPLOYMENT.md](DEPLOYMENT.md)
- Pour des **problèmes techniques** : ouvrir une issue sur GitHub
