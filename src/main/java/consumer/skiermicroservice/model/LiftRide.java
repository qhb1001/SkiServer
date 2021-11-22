package consumer.skiermicroservice.model;

public class LiftRide {
    private Integer id;
    private Integer skierId;
    private Integer liftId;
    private String seasonId;
    private String day;
    private Integer vertical;

    public LiftRide(Integer skierId, Integer liftId, String seasonId, String day, Integer vertical) {
        this.skierId = skierId;
        this.liftId = liftId;
        this.seasonId = seasonId;
        this.day = day;
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

    public String getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(String seasonId) {
        this.seasonId = seasonId;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public Integer getVertical() {
        return vertical;
    }

    public void setVertical(Integer vertical) {
        this.vertical = vertical;
    }
}
