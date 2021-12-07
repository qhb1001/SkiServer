package consumer.resortmicroservice.model;

public class TotalVerticalMessage extends GetRequestRPCMessageBase{
    private Integer skierId;
    private Integer resortId;
    private Integer seasonId;

    public TotalVerticalMessage(String type, Integer skierId, Integer resortId, Integer seasonId) {
        super(type);
        this.skierId = skierId;
        this.resortId = resortId;
        this.seasonId = seasonId;
    }

    public Integer getSkierId() {
        return skierId;
    }

    public void setSkierId(Integer skierId) {
        this.skierId = skierId;
    }

    public Integer getResortId() {
        return resortId;
    }

    public void setResortId(Integer resortId) {
        this.resortId = resortId;
    }

    public Integer getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(Integer seasonId) {
        this.seasonId = seasonId;
    }
}
