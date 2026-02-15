<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // Vérifier si l'utilisateur est connecté
    String username = (String) session.getAttribute("username");
    if (username == null || session.getAttribute("socket") == null) {
        response.sendRedirect("index.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chat - <%= username %></title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <div class="chat-container">
        <div class="chat-header">
            <div class="header-left">
                <h1>💬 Chat Multi-Clients</h1>
                <span class="user-badge">Connecté en tant que: <strong><%= username %></strong></span>
            </div>
            <div class="header-right">
                <button id="disconnectBtn" class="btn-disconnect">Déconnexion</button>
            </div>
        </div>

        <div class="chat-main">
            <div class="messages-container" id="messagesContainer">
                <div class="welcome-message">
                    Bienvenue dans le chat, <%= username %>! 👋
                </div>
            </div>

            <div class="input-container">
                <form id="messageForm">
                    <input 
                        type="text" 
                        id="messageInput" 
                        placeholder="Tapez votre message..."
                        autocomplete="off"
                        maxlength="500"
                    >
                    <button type="submit" class="btn-send">Envoyer</button>
                </form>
            </div>
        </div>
    </div>

    <script>
        let pollingInterval;
        
        // Démarrer le polling pour recevoir les messages
        function startPolling() {
            pollingInterval = setInterval(async () => {
                try {
                    const response = await fetch('chat/receive');
                    const data = await response.json();
                    
                    if (data.success && data.messages && data.messages.length > 0) {
                        data.messages.forEach(msg => addMessage(msg));
                    }
                } catch (error) {
                    console.error('Erreur lors de la récupération des messages:', error);
                }
            }, 500); // Polling toutes les 500ms
        }
        
        // Ajouter un message à l'interface
        function addMessage(text) {
            const container = document.getElementById('messagesContainer');
            const messageDiv = document.createElement('div');
            
            // Déterminer le type de message
            if (text.startsWith('#')) {
                messageDiv.className = 'message-system message-join';
            } else if (text.startsWith('<>')) {
                messageDiv.className = 'message-system message-leave';
            } else {
                messageDiv.className = 'message-user';
            }
            
            messageDiv.textContent = text;
            container.appendChild(messageDiv);
            
            // Scroll automatique vers le bas
            container.scrollTop = container.scrollHeight;
        }
        
        // Envoyer un message
        document.getElementById('messageForm').addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const input = document.getElementById('messageInput');
            const message = input.value.trim();
            
            if (!message) return;
            
            try {
                const response = await fetch('chat/send', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: 'message=' + encodeURIComponent(message)
                });
                
                const data = await response.json();
                
                if (data.success) {
                    input.value = '';
                } else {
                    alert('Erreur lors de l\'envoi du message');
                }
            } catch (error) {
                console.error('Erreur:', error);
                alert('Impossible d\'envoyer le message');
            }
        });
        
        // Gérer la déconnexion
        document.getElementById('disconnectBtn').addEventListener('click', async function() {
            if (confirm('Voulez-vous vraiment vous déconnecter?')) {
                clearInterval(pollingInterval);
                
                try {
                    await fetch('chat/disconnect', { method: 'POST' });
                } catch (error) {
                    console.error('Erreur lors de la déconnexion:', error);
                }
                
                window.location.href = 'index.jsp';
            }
        });
        
        // Gérer la fermeture de la fenêtre
        window.addEventListener('beforeunload', async function(e) {
            try {
                await fetch('chat/disconnect', { 
                    method: 'POST',
                    keepalive: true 
                });
            } catch (error) {
                console.error('Erreur lors de la déconnexion:', error);
            }
        });
        
        // Démarrer le polling au chargement de la page
        startPolling();
        
        // Focus sur le champ de saisie
        document.getElementById('messageInput').focus();
    </script>
</body>
</html>
