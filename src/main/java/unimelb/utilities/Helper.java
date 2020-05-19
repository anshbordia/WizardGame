package unimelb.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
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

    public static long getTimestamp(List<String> mergelist, int index) {
        JSONObject json = new JSONObject();
        json = Messages.strToJson(mergelist.get(index));
        long timestamp = (long) json.get("timestamp");
        return timestamp;

    }
    
    public static VectorClock getVectorClock(List<String> mergelist, int index) {
        JSONObject json = Messages.strToJson(mergelist.get(index));
        JSONArray timestampsJson = (JSONArray) json.get("vectorClock");
        List<Long> timestamps = new ArrayList<>(timestampsJson);
        VectorClock vectorClock = new VectorClock(timestamps);
        return vectorClock;
    }

    public static boolean getSuccess(List<String> mergelist, int index) {
        JSONObject json = new JSONObject();
        json = Messages.strToJson(mergelist.get(index));
        boolean success = (boolean) json.get("success");
        return success;
    }

    public static long attackedWho(List<String> mergelist, int index) {
        JSONObject json = new JSONObject();
        json = Messages.strToJson(mergelist.get(index));
        long attacked =  (long) json.get("attacked");
        return attacked;

    }

    public static long getAttacker(List<String> mergelist, int index) {
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

    
    public static List<String> sortList(List<String> mergelist, List<VectorClock> vcMerge) {
        //List<String> sortAL = new List<String>();
        for(int i = 0; i < vcMerge.size(); i++) {
            int bestIndex = i;
            for(int j = i + 1; j < vcMerge.size(); j++) {
                //VectorClock vc1 = Helper.getVectorClock(mergelist, j);
                //VectorClock vc2 = Helper.getVectorClock(mergelist, bestIndex);
            	VectorClock vc1 = vcMerge.get(j);
            	VectorClock vc2 = vcMerge.get(bestIndex);
                if (vc1.compareTo(vc2) == -1) {
                    bestIndex = j;
                    continue;
                }
                /*if(Helper.getTimestamp(mergelist, j) < Helper.getTimestamp(mergelist, bestIndex)) {
                    bestIndex = j;
                }*/
            }
            //sortAL.add(mergelist.get(bestIndex));
            VectorClock tempVC = vcMerge.get(i);
            vcMerge.set(i, vcMerge.get(bestIndex));
            vcMerge.set(bestIndex, tempVC);
            
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
        List<Long> deadList2 = new ArrayList<>();
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
    
    public static boolean canIspecialAttack(String feedback) {
    	JSONObject json = new JSONObject();
        json = Messages.strToJson(feedback);
        boolean specialAttack = (boolean) json.get("specialattack");
        return specialAttack;
     	
    }
    
    public static VectorClock getVC(String received) {
    	JSONObject json = new JSONObject();
        json = Messages.strToJson(received);
        VectorClock vc = (VectorClock) json.get("vectorClock");
        return vc;
    }
}