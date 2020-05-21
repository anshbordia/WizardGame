package main.java.unimelb.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * This file contains the VectorClock class which is responsible
 * for generating a partial ordering of events within our
 * distributed wizard game. Each process in the system will
 * abide by the following clock update rules:
 *
 *  1. Initially all clocks are zero
 *  2. Each time a process sends message, increment own logical
 *     clock by one in vector, before sending a copy of the vector
 *  3. Each time a process receives a message, take max of local value
 *     and value received in message, and increment own logical clock by
 *     one
 */

public class VectorClock {

    private static Logger log = Logger.getLogger(VectorClock.class.getName());

    private int totalProcesses;
    private int processID;
    private long timestamp;
    private List<Long> timestamps;

    public VectorClock(int totalProcesses, int processID) {
        this.totalProcesses = totalProcesses;
        this.processID = processID;
        this.timestamp = 0;
        this.timestamps = new ArrayList<>();
        for (int i = 0; i < totalProcesses; i++) {
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
     * using '<', '=', '>', and '||' operators
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
}
