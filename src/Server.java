import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    private static final Map<String, Account> accounts = new HashMap<>();
    private static final Map<Integer, Account> tokens = new HashMap<>();

    private static final AtomicInteger tokenCounter = new AtomicInteger(1000);
    private static final AtomicInteger messageIdCounter = new AtomicInteger(1);

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java MessagingServer <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            ) {
                Request req = (Request) in.readObject();
                Response res = handleRequest(req);
                out.writeObject(res);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try { socket.close(); } catch (IOException e) {}
            }
        }

        private Response handleRequest(Request req) {
            synchronized (accounts) {

                Account user = null;
                if (req.fnId != 1) {
                    user = tokens.get(req.authToken);
                    if (user == null) {
                        return new Response("Invalid Auth Token");
                    }
                }

                switch (req.fnId) {
                    case 1:
                        return createAccount(req.username);
                    case 2:
                        return showAccounts();
                    case 3:
                        return sendMessage(user, req.recipient, req.message);
                    case 4:
                        return showInbox(user);
                    case 5:
                        return readMessage(user, req.messageId);
                    case 6:
                        return deleteMessage(user, req.messageId);
                    default:
                        return new Response("Unknown Function ID");
                }
            }
        }

        private Response createAccount(String username) {
            if (!username.matches("^[a-zA-Z0-9_]+$")) {
                return new Response("Invalid Username");
            }
            if (accounts.containsKey(username)) {
                return new Response("Sorry, the user already exists");
            }

            Random rand = new Random();
            int newToken ;

            do {
                newToken = 1000 + rand.nextInt(9000);
            } while (tokens.containsKey(newToken));

            Account newAcc = new Account(username, newToken);
            accounts.put(username, newAcc);
            tokens.put(newToken, newAcc);

            return new Response(String.valueOf(newToken));
        }

        private Response showAccounts() {
            StringBuilder sb = new StringBuilder();
            int count = 1;
            for (String user : accounts.keySet()) {
                sb.append(count++).append(". ").append(user).append("\n");
            }
            if (sb.length() > 0) sb.setLength(sb.length() - 1);
            return new Response(sb.toString());
        }

        private Response sendMessage(Account sender, String recipientUsername, String body) {
            Account recipient = accounts.get(recipientUsername);
            if (recipient == null) {
                return new Response("User does not exist");
            }

            int msgId = messageIdCounter.incrementAndGet();
            Message msg = new Message(msgId, sender.username, recipient.username, body);
            recipient.messageList.add(msg);

            return new Response("OK");
        }

        private Response showInbox(Account user) {
            StringBuilder sb = new StringBuilder();
            for (Message msg : user.messageList) {
                sb.append(msg.id).append(". from:").append(msg.sender);
                if (!msg.isRead) sb.append("*");
                sb.append("\n");
            }
            if (sb.length() > 0) sb.setLength(sb.length() - 1);
            return new Response(sb.toString());
        }

        private Response readMessage(Account user, int msgId) {
            for (Message msg : user.messageList) {
                if (msg.id == msgId) {
                    msg.isRead = true;
                    return new Response("(" + msg.sender + ") " + msg.mes);
                }
            }
            return new Response("Message ID does not exist");
        }

        private Response deleteMessage(Account user, int msgId) {
            Iterator<Message> iterator = user.messageList.iterator();
            while (iterator.hasNext()) {
                Message msg = iterator.next();
                if (msg.id == msgId) {
                    iterator.remove();
                    return new Response("OK");
                }
            }
            return new Response("Message does not exist");
        }
    }
}

