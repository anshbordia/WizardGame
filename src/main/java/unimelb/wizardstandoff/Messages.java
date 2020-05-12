package unimelb.wizardstandoff;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Messages {
	
	public static JSONObject init(double[] probs, long playernum, double your_prob) {
		JSONObject json = new JSONObject();
		json.put("command", "Begin");
		json.put("yourprob", your_prob);
		JSONArray prob = new JSONArray();
		for(int i = 0; i < probs.length; i++) {
			prob.add(probs[i]);
		}
		json.put("probs", prob);
		json.put("playernum", (long) playernum);
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
	
	public static JSONObject sendAttack(long x, long timestamp, boolean success, long who) {
		JSONObject json = new JSONObject();
		json.put("command", "Attack");
		json.put("attacked", (long) x);
		json.put("timestamp", timestamp);
		json.put("success", success);
		json.put("who", who);
		return json;
	}
	
	public static JSONObject winner() {
		JSONObject json = new JSONObject();
		json.put("command", "Winner");
		return json;
	}
	
	public static JSONObject feedback(ArrayList<Long> deadPlayers, long alivePlayers) {
		JSONObject json = new JSONObject();
		JSONArray deadList = new JSONArray();
		for(int i = 0; i < deadPlayers.size(); i++) {
			deadList.add(deadPlayers.get(i));
		}
		json.put("command", "Feedback");
		json.put("dead", deadList);
		json.put("aliveplayers", alivePlayers);
		return json;
	}
	
	
	
	
//	public static void main(String[] args) {
//		JSONObject json = new JSONObject();
//		json.put("command", "Begin");
//		JSONArray messages = new JSONArray();
//	    messages.add("Hey!");
//	    messages.add("What's up?!");
//	    json.put("msgs", messages);
//	    //System.out.println(json.get("msgs")[0]);
//	    strToJson(json.toString());
//	}

}
