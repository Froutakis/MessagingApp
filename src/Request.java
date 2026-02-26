import java.io.Serializable;

class Request implements Serializable {
    int fnId ;
    String username;
    int authToken;
    String recipient;
    String message;
    int messageId;

    public Request() {}
}
