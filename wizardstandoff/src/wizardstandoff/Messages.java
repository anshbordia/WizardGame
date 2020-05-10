package wizardstandoff;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Messages {
	
	public static JSONObject init(double[] probs, int playernum, double your_prob) {
		JSONObject json = new JSONObject();
		json.put("command", "Begin");
		json.put("yourprob", your_prob);
		JSONArray prob = new JSONArray();
		for(int i = 0; i < probs.length; i++) {
			prob.add(probs[i]);
		}
		json.put("probs", prob);
		json.put("playernum", playernum);
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
	
	public static JSONObject sendAttack(int x, long timestamp, boolean success) {
		JSONObject json = new JSONObject();
		json.put("command", "Attack");
		json.put("attacked", x);
		json.put("timestamp", timestamp);
		json.put("success", success);
		return json;
	}
	
	public static JSONObject feedback(int[] deadPlayers) {
		JSONObject json = new JSONObject();
		json.put("command", "Feedback");
		json.put("dead", deadPlayers);
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
