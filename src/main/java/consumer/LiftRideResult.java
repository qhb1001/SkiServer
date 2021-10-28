package consumer;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LiftRideResult {
    private ConcurrentHashMap<Integer, Integer> results; // key: skierID, value: total time of the lift rides.

    public LiftRideResult() {
        results = new ConcurrentHashMap<>();
    }

    public synchronized void addTime(Integer skierID, Integer time) {
        results.put(skierID, results.get(skierID) + time);
    }

    public Integer getTotalTime(Integer skierID) {
        return results.get(skierID);
    }

    public synchronized List<Integer> getSkierIDs() {
        return Collections.list(results.keys());
    }
}
