# 💬 Chat Multi-Clients en Java

## 📌 Description du projet
Ce projet est une application de **chat multi-clients** développée en **Java** selon une architecture **client/serveur**.

Le serveur accepte plusieurs clients simultanément grâce à l’utilisation des **threads**.  
Chaque message envoyé par un client est **diffusé à tous les autres clients connectés**.

## 🛠️ Compilation et exécution

```bash
cd src 

javac ChatServer.java

java ChatServer

cd src

javac ChatClient.java

java ChatClient
