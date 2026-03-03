import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;


public class ClientController {

    private final String ip;
    private final int port;
    private Integer authToken = null;
    private String username = null;
    private boolean loggedIn = false;

    public ClientController(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    private String execute(Request req) throws IOException, ClassNotFoundException {
        try (Socket socket = new Socket(ip, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(req);
            Response res = (Response) in.readObject();
            return res.output;
        } catch (ConnectException e) {
            return "Connection refused. Is the server running?";
        }
    }

    public String register(String username) throws IOException, ClassNotFoundException {
        Request req = new Request();
        req.fnId = 1;
        req.username = username;
        
        String output = execute(req);

        Integer parsedToken = parseLastInteger(output);
        
        if (parsedToken!=null){
            this.username = username;
            this.authToken = parsedToken;
            this.loggedIn = true;
        } 
        return output;
    }


    public String login(String username, int authToken) throws IOException, ClassNotFoundException {
        Request req = new Request();
        req.fnId = 7;
        req.username = username;
        req.authToken = authToken;

        String output = execute(req);

        // If login was successful, keep the session data.
        if (output != null && output.contains("logged in successfully")) {
            this.username = username;
            this.authToken = authToken;
            this.loggedIn = true;
        }

        return output;
    }

    public String showAccounts() throws IOException, ClassNotFoundException {
        ensureLoggedIn();
        Request req = new Request();
        req.fnId = 2;
        req.authToken = authToken;
        return execute(req);
    }

    public String sendMessage(String recipient, String message)
            throws IOException, ClassNotFoundException {
        ensureLoggedIn();
        Request req = new Request();
        req.fnId = 3;
        req.authToken = authToken;
        req.recipient = recipient;
        req.message = message;
        return execute(req);
    }

    public String ShowInbox() throws IOException, ClassNotFoundException {
        ensureLoggedIn();
        Request req = new Request();
        req.fnId = 4;
        req.authToken = authToken;
        return execute(req);
    }

    public String ReadMessage(int messageId)
            throws IOException, ClassNotFoundException {
        ensureLoggedIn();
        Request req = new Request();
        req.fnId = 5;
        req.authToken = authToken;
        req.messageId = messageId;
        return execute(req);
    }

    public String DeleteMessage(int messageId)
            throws IOException, ClassNotFoundException {
        ensureLoggedIn();
        Request req = new Request();
        req.fnId = 6;
        req.authToken = authToken;
        req.messageId = messageId;
        return execute(req);
    }

    // Check if the user is logged in
    private void ensureLoggedIn() {
        if (!loggedIn || authToken == null) {
            throw new IllegalStateException("You must call login(username, token) successfully before using this operation.");
        }
    }


    // Helper function to parse the last integer from a string
    private static Integer parseLastInteger(String text) {
        if (text == null) {
            return null;
        }
        String[] parts = text.trim().split("\\s+");
        for (int i = parts.length - 1; i >= 0; i--) {
            try {
                return Integer.parseInt(parts[i]);
            } catch (NumberFormatException ignored) {
                
            }
        }
        return null;
    }
}