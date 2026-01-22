import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChatServer {

    private static final int PORT = 12345;

    // Collections thread-safe
    private static Set<PrintWriter> clientWriters =
            Collections.synchronizedSet(new HashSet<>());

    private static Map<PrintWriter, String> clientNames =
            Collections.synchronizedMap(new HashMap<>());

    private static SimpleDateFormat sdf =
            new SimpleDateFormat("HH:mm");

    public static void main(String[] args) {
        System.out.println("Serveur de chat démarré...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String clientName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Choix du nom (unique)
                while (true) {
                    out.println("Entrez votre nom : ");
                    clientName = in.readLine();
                    if (clientName == null) return;

                    synchronized (clientNames) {
                        if (!clientNames.containsValue(clientName)) {
                            clientNames.put(out, clientName);
                            clientWriters.add(out);
                            break;
                        } else {
                            out.println("Nom déjà utilisé, choisissez-en un autre.");
                        }
                    }
                }

                broadcast("# " + clientName + " a rejoint le chat.", null);

                String message;
                while ((message = in.readLine()) != null) {

                    if (message.equalsIgnoreCase("/quit")) {
                        break;
                    }

                    String time = sdf.format(new Date());
                    broadcast("[" + time + "] " + clientName + " : " + message, out);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }

        private void broadcast(String message, PrintWriter exclude) {
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    if (writer != exclude) {
                        writer.println(message);
                    }
                }
            }
            System.out.println(message);
        }

        private void disconnect() {
            if (clientName != null) {
                clientWriters.remove(out);
                clientNames.remove(out);
                broadcast("<> " + clientName + " a quitté le chat.", null);
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
