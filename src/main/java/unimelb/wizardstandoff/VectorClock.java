package unimelb.wizardstandoff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


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

public class VectorClock implements Comparable<VectorClock> {

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
     * @param vectorClock vector clock
     * @return -1, 0, 1 for less than, equal, greater than
     */
    @Override
    public int compareTo(VectorClock vectorClock) {
        // TODO: Implement interface
        return 0;
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
        // Return a copy
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


    // Entry point for debugging only
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



    }


}
