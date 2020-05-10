package wizardstandoff;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Helper {

	public static long getTime() {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		long time = cal.getTimeInMillis();
		return time;
	}
	
	public static void getStatistics(JSONObject json) {
		System.out.println("Your Player Number:" + json.get("playernum"));
		System.out.println("Your Kill Probablity:" + json.get("yourprob"));
		System.out.println("All Kill Probablities" + json.get("probs"));
		
	}
	
	public static void seeAttacks(JSONObject json) {
		System.out.println("Player Attacked:" + json.get("attacked"));
		System.out.println("At Time:" + json.get("timestamp"));
		System.out.println("Attack successful?:" + json.get("success"));
		
	}
}
