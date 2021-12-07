package consumer.resortmicroservice.model;

public class UniqueSkierMessage extends GetRequestRPCMessageBase{

    private Integer resortId;
    private Integer seasonId;
    private Integer dayId;

    public UniqueSkierMessage(String type, Integer resortId, Integer seasonId, Integer dayId) {
        super(type);
        this.resortId = resortId;
        this.seasonId = seasonId;
        this.dayId = dayId;
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

    public Integer getDayId() {
        return dayId;
    }

    public void setDayId(Integer dayId) {
        this.dayId = dayId;
    }
}
