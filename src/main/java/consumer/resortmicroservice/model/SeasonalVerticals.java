package consumer.resortmicroservice.model;

import java.util.ArrayList;

public class SeasonalVerticals {
    private ArrayList<SeasonalVertical> resorts;

    public SeasonalVerticals(ArrayList<SeasonalVertical> resorts) {
        this.resorts = resorts;
    }

    public ArrayList<SeasonalVertical> getResorts() {
        return resorts;
    }

    public void setResorts(ArrayList<SeasonalVertical> resorts) {
        this.resorts = resorts;
    }
}
