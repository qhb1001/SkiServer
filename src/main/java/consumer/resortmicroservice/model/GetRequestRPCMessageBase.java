package consumer.resortmicroservice.model;

public class GetRequestRPCMessageBase {
    private String type;

    public GetRequestRPCMessageBase(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
