package unimelb.wizardstandoff;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
	
	public static long getTimestamp(ArrayList<String> mergelist, int index) {
		JSONObject json = new JSONObject();
		json = Messages.strToJson(mergelist.get(index));
		long timestamp = (long) json.get("timestamp");
		return timestamp;
		
	}
	
	public static boolean getSuccess(ArrayList<String> mergelist, int index) {
		JSONObject json = new JSONObject();
		json = Messages.strToJson(mergelist.get(index));
		boolean success = (boolean) json.get("success");
		return success;
	}
	
	public static long attackedWho(ArrayList<String> mergelist, int index) {
		JSONObject json = new JSONObject();
		json = Messages.strToJson(mergelist.get(index));
		long attacked =  (long) json.get("attacked");
		return attacked;
		
	}
	
	public static long getAttacker(ArrayList<String> mergelist, int index) {
		JSONObject json = new JSONObject();
		json = Messages.strToJson(mergelist.get(index));
		long who = (long) json.get("who");
		return who;
	}
	
	public static boolean amIwinner(String feedback) {
		JSONObject json = new JSONObject();
		json = Messages.strToJson(feedback);
		if((long) json.get("aliveplayers") == 1) {
			return true;
		}
		return false;
	}
	
	public static ArrayList<String> sortArrayList(ArrayList<String> mergelist) {
		//ArrayList<String> sortAL = new ArrayList<String>();
		for(int i = 0; i < mergelist.size(); i++) {
			int bestIndex = i;
			for(int j = i + 1; j < mergelist.size(); j++) {
				if(Helper.getTimestamp(mergelist, j) < Helper.getTimestamp(mergelist, bestIndex)) {
					bestIndex = j;
				}
			}
			//sortAL.add(mergelist.get(bestIndex));
			String temp = mergelist.get(i);
			mergelist.set(i, mergelist.get(bestIndex));
			mergelist.set(bestIndex, temp);
		}
		return mergelist;
		
	}
	public static boolean processFeedback(String feedback, long player_num) {
		JSONObject json = new JSONObject();
		json = Messages.strToJson(feedback);
		JSONArray deadList = new JSONArray();
		//obj.getJSONArray("rows");
		deadList = (JSONArray) json.get("dead");
		System.out.println("C1");
		ArrayList<Long> deadList2 = new ArrayList<Long>();
		System.out.println("C2");
		for(int i = 0; i < deadList.size(); i++) {
			deadList2.add((Long) deadList.get(i));
		}
		System.out.println("C3");
		for(int i = 0; i < deadList2.size(); i++) {
			if(deadList2.get(i) == player_num) {
				return true;
			}
		}
		System.out.println("C4");
		return false;
		
	}
}
