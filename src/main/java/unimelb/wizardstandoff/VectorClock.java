package unimelb.wizardstandoff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Vector clock logic
 *  1. initially all clocks zero
 *  2. Each time a process sends message, increment own logical clock by one in vector, before sending copy
 *  3. Each time a process receives a message, increment own logical clock by one and take max of local value
 *     and value received in message
 */

public class VectorClock {

    private static Logger log = Logger.getLogger(VectorClock.class.getName());

    private int N;
    private int processID;
    private long timestamp;
    private List<Long> timestamps;

    public VectorClock(int N, int processID) {
        this.N = N;
        this.processID = processID;
        this.timestamp = 0;
        this.timestamps = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            timestamps.add((long) 0);
        }
    }

    public VectorClock(List<Long> timestamps) {
        this.timestamps = timestamps;
    }

    public void onSend() {
        incrementLocalTimestamp();
    }

    public void onReceive(VectorClock other) {
        this.timestamps = getMax(this, other);
        incrementLocalTimestamp();
    }

    public void onInternalEvent() {
        incrementLocalTimestamp();
    }

    private void incrementLocalTimestamp() {
        this.timestamp += 1;
        this.timestamps.set(processID, timestamp);
    }

    /**
     * Used to compare one vector clock with another
     * using '<', '=' and '>' operators
     * @param other vector clock
     * @return -1, 0, 1, null for less than, equal to, greater than, asynchronous
     */
    public Integer compareTo(VectorClock other) {
        List<Long> ts1 = this.getTimestamps();
        List<Long> ts2 = other.getTimestamps();
        if (ts1.size() != ts2.size()) {
            log.warning("Vector clock sizes do not match!");
        }
        // Check equality
        if (ts1.equals(ts2)) {
            return 0;
        }
        // Check less than
        boolean lessThan = true;
        for (int i = 0; i < ts1.size(); i++) {
            if (ts1.get(i) > ts2.get(i)) {
                lessThan = false;
            }
        }
        if (lessThan) {
            return -1;
        }
        // Check greater than
        boolean greaterThan = true;
        for (int i = 0; i < ts1.size(); i++) {
            if (ts1.get(i) < ts2.get(i)) {
                greaterThan = false;
            }
        }
        if (greaterThan) {
            return 1;
        }
        // Otherwise, vector clocks are asynchronous
        return null;
    }

    /**
     * Calculates and returns maximum timestamps as a
     * list, with respect to two input vector clocks
     * @param vc1 vector clock one
     * @param vc2 vector clock two
     * @return max timestamps list of both vector clocks
     */
    private List<Long> getMax(VectorClock vc1, VectorClock vc2) {
        List<Long> ts1 = vc1.getTimestamps();
        List<Long> ts2 = vc2.getTimestamps();
        List<Long> tsMax = new ArrayList<>();
        for (int i = 0; i < ts1.size(); i++) {
            tsMax.add(Math.max(ts1.get(i), ts2.get(i)));
        }
        return tsMax;
    }

    public List<Long> getTimestamps() {
        // Return a copy to ensure no external mutation
        return new ArrayList<>(this.timestamps);
    }

    /**
     * Returns string representation of vector clock
     * @return string
     */
    @Override
    public String toString() {
        return "Process " + processID + ": " + timestamps;
    }


    // Entry point for debugging only (ignore this code, will delete later)
    public static void main(String[] args) {
        int maxPlayers = 4;
        int playerNum = 4;
        String message = Messages.processInfo(maxPlayers, playerNum).toJSONString();
        System.out.println("Message: " + message);


        List<Long> longs = new ArrayList<>();
        longs.add((long) 2);
        longs.add((long) 4);
        longs.add((long) 3);
        System.out.println("Process 4: " + longs);

        JSONObject json = new JSONObject();
        JSONArray arr = new JSONArray();
        arr.addAll(longs);
        json.put("vectorClock", arr);
        System.out.println(json.toJSONString());
        System.out.println(json.toString());

        VectorClock vc1 = new VectorClock(2, 0);
        VectorClock vc2 = new VectorClock(2, 1);

        List<Long> timestamps = vc1.getTimestamps();
        JSONArray a = new JSONArray();
        a.addAll(timestamps);

        System.out.println(a.toJSONString());



        List<Double> probs = new ArrayList<>();
        probs.add(0.2);
        probs.add(0.8);
        JSONObject j = Messages.init(probs, (long) 0, 0.2, vc1);
        String m = j.toJSONString();
        System.out.println("M = " + m);

        JSONObject j_prime = Messages.strToJson(m);
        JSONArray tsJson = (JSONArray) j_prime.get("vectorClock");
        List<Long> ts = new ArrayList<>(tsJson);
        System.out.println(j_prime);
        System.out.println("Timestamps: " + ts);


        List<Long> ls1 = new ArrayList<>();
        ls1.add((long) 2);
        ls1.add((long) 3);

        List<Long> ls2 = new ArrayList<>();
        ls2.add((long) 2);
        ls2.add((long) 3);

        System.out.println("ls1 = " + ls1);
        System.out.println("ls2 = " + ls2);
        System.out.println(ls1.equals(ls2));

    }
}