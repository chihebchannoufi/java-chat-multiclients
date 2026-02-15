<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Erreur - Chat Multi-Clients</title>
    <link rel="stylesheet" href="css/style.css">
    <style>
        .error-container {
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            padding: 20px;
        }
        .error-box {
            background: white;
            border-radius: 16px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            padding: 40px;
            text-align: center;
            max-width: 500px;
        }
        .error-icon {
            font-size: 4rem;
            margin-bottom: 20px;
        }
        .error-title {
            color: #ff4d4f;
            font-size: 1.5rem;
            margin-bottom: 10px;
        }
        .error-message {
            color: #666;
            margin-bottom: 30px;
        }
        .btn-home {
            display: inline-block;
            padding: 12px 30px;
            background: #4a90e2;
            color: white;
            text-decoration: none;
            border-radius: 8px;
            font-weight: 600;
            transition: all 0.3s;
        }
        .btn-home:hover {
            background: #357abd;
            transform: translateY(-2px);
        }
    </style>
</head>
<body>
    <div class="error-container">
        <div class="error-box">
            <div class="error-icon">⚠️</div>
            <h1 class="error-title">Oups ! Une erreur s'est produite</h1>
            <p class="error-message">
                Nous sommes désolés, mais quelque chose s'est mal passé.
            </p>
            <a href="index.jsp" class="btn-home">Retour à l'accueil</a>
        </div>
    </div>
</body>
</html>
