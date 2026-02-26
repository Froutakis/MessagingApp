import java.io.Serializable;

class Response implements Serializable {
    String output;

    public Response(String output){
        this.output = output;
    }
}
