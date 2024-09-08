import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static Map<PrintWriter, Integer> clientMap = new HashMap<>();
    private static int clientCounter = 1;

    public static void main(String[] args) {
        System.out.println("Chat server started...");
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
        private int clientNumber;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.clientNumber = clientCounter++;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                synchronized (clientMap) {
                    clientMap.put(out, clientNumber);
                }

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received from Client #" + clientNumber + ": " + message);
                    broadcast(message, out);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                synchronized (clientMap) {
                    clientMap.remove(out);
                }
            }
        }

        private void broadcast(String message, PrintWriter sender) {
            synchronized (clientMap) {
                for (Map.Entry<PrintWriter, Integer> entry : clientMap.entrySet()) {
                    PrintWriter writer = entry.getKey();
                    int clientNum = entry.getValue();
                    if (writer != sender) {
                        writer.println("User #" + clientNum + ": " + message);
                    }
                }
            }
        }
    }
}
