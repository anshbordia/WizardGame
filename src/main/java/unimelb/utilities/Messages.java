package main.java.unimelb.utilities;

import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Messages {

    public static synchronized JSONObject init(List<Double> probs, long playernum, double your_prob, VectorClock vectorClock) {
        JSONObject json = new JSONObject();
        json.put("command", "Begin");
        json.put("yourprob", your_prob);
        JSONArray prob = new JSONArray();
        for (int i = 0; i < probs.size(); i++) {
            prob.add(probs.get(i));
        }
        json.put("probs", prob);
        json.put("playernum", (long) playernum);
        vectorClock.onSend();
        JSONArray timestamps = new JSONArray();
        timestamps.addAll(vectorClock.getTimestamps());
        json.put("vectorClock", timestamps);
        return json;
    }

    public static JSONObject strToJson(String jsonstring) {
        JSONParser parser = new JSONParser();
        JSONObject json = null;
        try {
            json = (JSONObject) parser.parse(jsonstring);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return json;
    }

    public static synchronized JSONObject sendAttack(long x, long timestamp, boolean success, long who, VectorClock vectorClock) {
        JSONObject json = new JSONObject();
        json.put("command", "Attack");
        json.put("attacked", (long) x);
        json.put("timestamp", timestamp);
        json.put("success", success);
        json.put("who", who);
        vectorClock.onSend();
        JSONArray timestamps = new JSONArray();
        timestamps.addAll(vectorClock.getTimestamps());
        json.put("vectorClock", timestamps);
        return json;
    }

    public static synchronized JSONObject winner(VectorClock vectorClock) {
        JSONObject json = new JSONObject();
        json.put("command", "Winner");
        vectorClock.onSend();
        JSONArray timestamps = new JSONArray();
        timestamps.addAll(vectorClock.getTimestamps());
        json.put("vectorClock", timestamps);
        return json;
    }

    public static synchronized JSONObject feedback(List<Long> deadPlayers, long alivePlayers, VectorClock vectorClock, boolean specialAttack) {
        JSONObject json = new JSONObject();
        JSONArray deadList = new JSONArray();
        for (int i = 0; i < deadPlayers.size(); i++) {
            deadList.add(deadPlayers.get(i));
        }
        json.put("command", "Feedback");
        json.put("dead", deadList);
        json.put("aliveplayers", alivePlayers);
        vectorClock.onSend();
        JSONArray timestamps = new JSONArray();
        timestamps.addAll(vectorClock.getTimestamps());
        json.put("vectorClock", timestamps);
        json.put("specialattack", specialAttack);
        return json;
    }
    
    public static synchronized JSONObject over(VectorClock vectorClock) {
        JSONObject json = new JSONObject();
        json.put("command", "Over");

        vectorClock.onSend();
        JSONArray timestamps = new JSONArray();
        timestamps.addAll(vectorClock.getTimestamps());
        json.put("vectorClock", timestamps);
        return json;
    }

    public static JSONObject processInfo(int totalProcesses, int processID) {
        JSONObject json = new JSONObject();
        json.put("totalProcesses", totalProcesses);
        json.put("processID", processID);
        return json;
    }
}