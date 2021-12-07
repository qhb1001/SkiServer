package consumer.resortmicroservice.model;

public class SkiDayVerticalMessage extends GetRequestRPCMessageBase {
    private Integer resortId;
    private Integer seasonId;
    private Integer dayId;
    private Integer skierId;

    public SkiDayVerticalMessage(String type, Integer resortId, Integer seasonId, Integer dayId, Integer skierId) {
        super(type);
        this.resortId = resortId;
        this.seasonId = seasonId;
        this.dayId = dayId;
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

    public Integer getDayId() {
        return dayId;
    }

    public void setDayId(Integer dayId) {
        this.dayId = dayId;
    }

    public Integer getSkierId() {
        return skierId;
    }

    public void setSkierId(Integer skierId) {
        this.skierId = skierId;
    }
}
