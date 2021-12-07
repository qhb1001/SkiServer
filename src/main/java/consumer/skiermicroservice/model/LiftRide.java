package consumer.skiermicroservice.model;

public class LiftRide {
    private Integer id;
    private Integer skierId;
    private Integer liftId;
    private Integer seasonId;
    private Integer dayId;
    private Integer vertical;

    public LiftRide(Integer skierId, Integer liftId, Integer seasonId, Integer dayId, Integer vertical) {
        this.skierId = skierId;
        this.liftId = liftId;
        this.seasonId = seasonId;
        this.dayId = dayId;
        this.vertical = vertical;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSkierId() {
        return skierId;
    }

    public void setSkierId(Integer skierId) {
        this.skierId = skierId;
    }

    public Integer getLiftId() {
        return liftId;
    }

    public void setLiftId(Integer liftId) {
        this.liftId = liftId;
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

    public Integer getVertical() {
        return vertical;
    }

    public void setVertical(Integer vertical) {
        this.vertical = vertical;
    }
}
