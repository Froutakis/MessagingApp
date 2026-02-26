import java.io.Serializable;

class Message implements Serializable {
    int id;
    boolean isRead;
    String sender;
    String receiver;
    String mes;

    public Message(int id,String sender,String receiver,String mes){
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.mes = mes ;
    }
}
