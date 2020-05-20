package main.java.unimelb.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.util.TimeZone;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Helper {

	//Display kill probabilities of all players
    public static void getStatistics(JSONObject json) {
        System.out.println("Your Player Number:" + json.get("playernum"));
        System.out.println("Your Kill Probablity:" + json.get("yourprob"));
        System.out.println("All Kill Probablities" + json.get("probs"));
    }
    
    //Get information of the attack
    public static void seeAttacks(JSONObject json) {
        System.out.println("Player Attacked:" + json.get("attacked"));
        System.out.println("At Time:" + json.get("timestamp"));
        System.out.println("Attack successful?:" + json.get("success"));

    }

    //Retrieve vector clock from list of messages
    public static VectorClock getVectorClock(List<String> mergelist, int index) {
        JSONObject json = Messages.strToJson(mergelist.get(index));
        JSONArray timestampsJson = (JSONArray) json.get("vectorClock");
        List<Long> timestamps = new ArrayList<>(timestampsJson);
        VectorClock vectorClock = new VectorClock(timestamps);
        return vectorClock;
    }
    
    //Retrieve success value of attack 
    public static boolean getSuccess(List<String> mergelist, int index) {
        JSONObject json = new JSONObject();
        json = Messages.strToJson(mergelist.get(index));
        boolean success = (boolean) json.get("success");
        return success;
    }
   
    //Unused function
    public static long getTime() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        long time = cal.getTimeInMillis();
        return time;
    }
    
    //Unused Function
    public static long getTimestamp(List<String> mergelist, int index) {
        JSONObject json = new JSONObject();
        json = Messages.strToJson(mergelist.get(index));
        long timestamp = (long) json.get("timestamp");
        return timestamp;

    }

    //Retrieve attacked player from message
    public static long attackedWho(List<String> mergelist, int index) {
        JSONObject json = new JSONObject();
        json = Messages.strToJson(mergelist.get(index));
        long attacked =  (long) json.get("attacked");
        return attacked;

    }
    
    //Retrieve the attacker from the message
    public static long getAttacker(List<String> mergelist, int index) {
        JSONObject json = new JSONObject();
        json = Messages.strToJson(mergelist.get(index));
        long who = (long) json.get("who");
        return who;
    }

    //Used by player to find out if they are the winner from the message received from server
    public static boolean amIwinner(String feedback) {
        JSONObject json = new JSONObject();
        json = Messages.strToJson(feedback);
        if((long) json.get("aliveplayers") == 1) {
            return true;
        }
        return false;
    }

    //Sort attacks by players based on Vector Clock values
    public static List<String> sortList(List<String> mergelist, List<VectorClock> vcMerge) {
        for(int i = 0; i < vcMerge.size(); i++) {
            int bestIndex = i;
            for(int j = i + 1; j < vcMerge.size(); j++) {
            	VectorClock vc1 = vcMerge.get(j);
            	VectorClock vc2 = vcMerge.get(bestIndex);
                if (vc1.compareTo(vc2) == -1) {
                    bestIndex = j;
                    continue;
                }
            }
          
            VectorClock tempVC = vcMerge.get(i);
            vcMerge.set(i, vcMerge.get(bestIndex));
            vcMerge.set(bestIndex, tempVC);
            
            String temp = mergelist.get(i);
            mergelist.set(i, mergelist.get(bestIndex));
            mergelist.set(bestIndex, temp);
        }
        
        return mergelist;
    }
    
    //Used by player to find out if it was killed or not in the current round and also get status of other players
    public static boolean processFeedback(String feedback, long player_num) {
        JSONObject json = new JSONObject();
        json = Messages.strToJson(feedback);
        JSONArray deadList = new JSONArray();
        deadList = (JSONArray) json.get("dead");
        List<Long> deadList2 = new ArrayList<>();
        for(int i = 0; i < deadList.size(); i++) {
            deadList2.add((Long) deadList.get(i));
        }
        for(int i = 0; i < deadList2.size(); i++) {
            if(deadList2.get(i) == player_num) {
                return true;
            }
        }
        return false;

    }
    //Each player will be passed the special attack token one at a time.
    //The token will be passed around by the server in each round
    //The order will be based on the player number: Player 1, Player 2...Player N
    //This function will help the player decide if they can special attack based on the message received.
    public static boolean canIspecialAttack(String feedback) {
    	JSONObject json = new JSONObject();
        json = Messages.strToJson(feedback);
        boolean specialAttack = (boolean) json.get("specialattack");
        return specialAttack;
     	
    }
    
    //Retreive vector clock from a message received.
    public static VectorClock getVC(String received) {
    	JSONObject json = new JSONObject();
        json = Messages.strToJson(received);
        VectorClock vc = (VectorClock) json.get("vectorClock");
        return vc;
    }
}