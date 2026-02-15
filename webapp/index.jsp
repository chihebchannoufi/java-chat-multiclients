<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chat Multi-Clients - Connexion</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <div class="container">
        <div class="login-box">
            <h1>💬 Chat Multi-Clients</h1>
            <p class="subtitle">Connectez-vous pour commencer à discuter</p>
            
            <form id="loginForm">
                <div class="form-group">
                    <label for="username">Nom d'utilisateur</label>
                    <input 
                        type="text" 
                        id="username" 
                        name="username" 
                        placeholder="Entrez votre pseudo"
                        required
                        autocomplete="off"
                        maxlength="20"
                    >
                </div>
                
                <button type="submit" class="btn-primary">Se connecter</button>
                
                <div id="errorMessage" class="error-message" style="display: none;"></div>
            </form>
            
            <div class="info-box">
                <p><strong>Instructions:</strong></p>
                <ul>
                    <li>Choisissez un nom d'utilisateur unique</li>
                    <li>Vous pourrez discuter en temps réel avec d'autres utilisateurs</li>
                    <li>Les messages sont diffusés à tous les participants</li>
                </ul>
            </div>
        </div>
    </div>

    <script>
        document.getElementById('loginForm').addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const username = document.getElementById('username').value.trim();
            const errorDiv = document.getElementById('errorMessage');
            
            if (!username) {
                showError('Veuillez entrer un nom d\'utilisateur');
                return;
            }
            
            try {
                const response = await fetch('chat/connect', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: 'username=' + encodeURIComponent(username)
                });
                
                const data = await response.json();
                
                if (data.success) {
                    // Rediriger vers la page de chat
                    window.location.href = 'chat.jsp';
                } else {
                    showError(data.message || 'Erreur de connexion');
                }
            } catch (error) {
                showError('Impossible de se connecter au serveur. Assurez-vous que le serveur de chat est démarré.');
            }
        });
        
        function showError(message) {
            const errorDiv = document.getElementById('errorMessage');
            errorDiv.textContent = message;
            errorDiv.style.display = 'block';
            
            setTimeout(() => {
                errorDiv.style.display = 'none';
            }, 5000);
        }
    </script>
</body>
</html>
