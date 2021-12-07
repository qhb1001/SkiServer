package consumer.resortmicroservice.model;

public class SeasonalVertical {
    private Integer seasonId;
    private Integer totalVert;

    public SeasonalVertical(Integer seasonId, Integer totalVert) {
        this.seasonId = seasonId;
        this.totalVert = totalVert;
    }

    public Integer getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(Integer seasonId) {
        this.seasonId = seasonId;
    }

    public Integer getTotalVert() {
        return totalVert;
    }

    public void setTotalVert(Integer totalVert) {
        this.totalVert = totalVert;
    }
}
