import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java client <ip> <port> <FN_ID> <args...>");
            return;
        }

        String ip = args[0];
        int port = Integer.parseInt(args[1]);
        int fnId = Integer.parseInt(args[2]);

        Request req = new Request();
        req.fnId = fnId;

        try {
            switch (fnId) {
                case 1:
                    if (args.length < 4) throw new IllegalArgumentException();
                    req.username = args[3];
                    break;

                case 2:
                case 4:
                    if (args.length < 4) throw new IllegalArgumentException();
                    req.authToken = Integer.parseInt(args[3]);
                    break;

                case 3:
                    if (args.length < 6) throw new IllegalArgumentException();
                    req.authToken = Integer.parseInt(args[3]);
                    req.recipient = args[4];
                    StringBuilder body = new StringBuilder();
                    for(int i = 5; i < args.length; i++) body.append(args[i]).append(" ");
                    req.message = body.toString().trim();
                    break;

                case 5:
                case 6:
                    if (args.length < 5) throw new IllegalArgumentException();
                    req.authToken = Integer.parseInt(args[3]);
                    req.messageId = Integer.parseInt(args[4]);
                    break;
                case 7:
                    if (args.length < 5) throw new IllegalArgumentException();
                    req.authToken = Integer.parseInt(args[3]);
                    req.username = args[4];
                    break;

                default:
                    System.out.println("Invalid Function ID");
                    return;
            }
        } catch (Exception e) {
            System.out.println("Invalid arguments for Function ID: " + fnId);
            return;
        }

        try (Socket socket = new Socket(ip, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(req);

            Response res = (Response) in.readObject();

            System.out.println(res.output);

        } catch (ConnectException e) {
            System.out.println("Connection refused. Is the server running?");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
