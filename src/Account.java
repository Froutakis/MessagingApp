import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class Account implements Serializable {
    String username;
    int authToken;
    List<Message> messageList;

    public Account(String username,int authToken){
        this.username = username;
        this.authToken = authToken;
        this.messageList = new ArrayList<>();
    }
}
