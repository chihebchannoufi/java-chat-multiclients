package com.chat.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Servlet pour gérer la connexion au serveur de chat et les opérations de chat
 */
@WebServlet({"/chat/connect", "/chat/send", "/chat/receive", "/chat/disconnect"})
public class ChatServlet extends HttpServlet {

    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 12345;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String path = request.getServletPath();
        
        switch (path) {
            case "/chat/connect":
                handleConnect(request, response);
                break;
            case "/chat/send":
                handleSend(request, response);
                break;
            case "/chat/disconnect":
                handleDisconnect(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String path = request.getServletPath();
        
        if ("/chat/receive".equals(path)) {
            handleReceive(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Connecte l'utilisateur au serveur de chat
     */
    private void handleConnect(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        String username = request.getParameter("username");
        
        if (username == null || username.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\": false, \"message\": \"Nom d'utilisateur requis\"}");
            return;
        }

        HttpSession session = request.getSession(true);
        
        // Vérifier si déjà connecté
        if (session.getAttribute("socket") != null) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": true, \"message\": \"Déjà connecté\"}");
            return;
        }

        try {
            // Créer une connexion socket au serveur
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // File de messages reçus
            ConcurrentLinkedQueue<String> messageQueue = new ConcurrentLinkedQueue<>();
            
            // Thread pour recevoir les messages du serveur
            Thread receiverThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        messageQueue.offer(message);
                    }
                } catch (IOException e) {
                    // Connexion fermée
                }
            });
            receiverThread.setDaemon(true);
            receiverThread.start();
            
            // Attendre le message de demande de nom
            Thread.sleep(100);
            messageQueue.poll(); // Enlever "Entrez votre nom : "
            
            // Envoyer le nom d'utilisateur
            out.println(username);
            
            // Attendre la réponse
            Thread.sleep(200);
            String serverResponse = messageQueue.poll();
            
            // Vérifier si le nom est déjà utilisé
            if (serverResponse != null && serverResponse.contains("déjà utilisé")) {
                socket.close();
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"Nom déjà utilisé\"}");
                return;
            }
            
            // Stocker dans la session
            session.setAttribute("socket", socket);
            session.setAttribute("out", out);
            session.setAttribute("in", in);
            session.setAttribute("messageQueue", messageQueue);
            session.setAttribute("username", username);
            session.setAttribute("receiverThread", receiverThread);
            
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": true, \"message\": \"Connecté avec succès\"}");
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"Erreur de connexion: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Envoie un message au serveur de chat
     */
    private void handleSend(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute("socket") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"Non connecté\"}");
            return;
        }

        String message = request.getParameter("message");
        
        if (message == null || message.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"Message vide\"}");
            return;
        }

        try {
            PrintWriter out = (PrintWriter) session.getAttribute("out");
            out.println(message);
            
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": true}");
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"Erreur d'envoi\"}");
        }
    }

    /**
     * Récupère les messages en attente (polling)
     */
    private void handleReceive(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute("socket") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"messages\": []}");
            return;
        }

        try {
            @SuppressWarnings("unchecked")
            ConcurrentLinkedQueue<String> messageQueue = 
                (ConcurrentLinkedQueue<String>) session.getAttribute("messageQueue");
            
            StringBuilder messages = new StringBuilder();
            messages.append("{\"success\": true, \"messages\": [");
            
            boolean first = true;
            String msg;
            while ((msg = messageQueue.poll()) != null) {
                if (!first) {
                    messages.append(",");
                }
                // Échapper les guillemets et autres caractères spéciaux JSON
                String escapedMsg = msg.replace("\\", "\\\\")
                                      .replace("\"", "\\\"")
                                      .replace("\n", "\\n")
                                      .replace("\r", "\\r");
                messages.append("\"").append(escapedMsg).append("\"");
                first = false;
            }
            
            messages.append("]}");
            
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(messages.toString());
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"messages\": []}");
        }
    }

    /**
     * Déconnecte l'utilisateur du serveur de chat
     */
    private void handleDisconnect(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            try {
                Socket socket = (Socket) session.getAttribute("socket");
                PrintWriter out = (PrintWriter) session.getAttribute("out");
                
                if (out != null) {
                    out.println("/quit");
                }
                
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                
                session.invalidate();
                
            } catch (Exception e) {
                // Ignorer les erreurs de déconnexion
            }
        }
        
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.getWriter().write("{\"success\": true}");
    }
}
