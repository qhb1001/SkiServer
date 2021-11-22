package consumer.resortmicroservice.model;

public class LiftRide {
    private Integer id;
    private Integer resortId;
    private Integer skierId;
    private Integer liftId;
    private String day;
    private Integer time;
    private Integer vertical;

    public LiftRide(Integer resortId, Integer skierId, Integer liftId, String day, Integer time, Integer vertical) {
        this.resortId = resortId;
        this.skierId = skierId;
        this.liftId = liftId;
        this.day = day;
        this.time = time;
        this.vertical = vertical;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getResortId() {
        return resortId;
    }

    public void setResortId(Integer resortId) {
        this.resortId = resortId;
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

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public Integer getVertical() {
        return vertical;
    }

    public void setVertical(Integer vertical) {
        this.vertical = vertical;
    }
}
