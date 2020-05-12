package unimelb.wizardstandoff;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import org.json.simple.JSONObject;

public class ClientHandler implements Runnable {

	final BufferedReader input;
	final PrintWriter out;
	final Socket socket;
	Scanner sc = new Scanner(System.in);
	static int counter = 0;
	public static int total_players;
	final double kill_probablity ;
	final long player_num;
	final double[] kill_probablities;
	public static volatile ArrayList<String> merge;;
	public long alivePlayers;
	public static volatile int iteration = 0; 
	//public static volatile int received = 0;
	public Wizard[] wizardList; 
	
	public ClientHandler(Socket socket, BufferedReader input, PrintWriter out, double[] kill_probablities, long player_num) {
		this.socket = socket;
		this.input = input;
		this.out = out;
		this.kill_probablities = kill_probablities;
		this.player_num = player_num;
		this.kill_probablity = kill_probablities[(int) (player_num - 1)];
		alivePlayers = kill_probablities.length;
		total_players = kill_probablities.length;
		wizardList = new Wizard[kill_probablities.length];
		for(int i = 0; i < kill_probablities.length; i++) {
			wizardList[i] = new Wizard(kill_probablities[i], i + 1);
		}
		System.out.println(wizardList.toString());
	}

	public ArrayList<Long> gameLogic(ArrayList<String> mergelist) {
		if(mergelist.size() < this.alivePlayers) {
			return null;
		}
		ArrayList<Long> deadPlayers = new ArrayList<Long>();
		mergelist = Helper.sortArrayList(mergelist);
		System.out.println("Sorted merge: " + mergelist.toString());
		for(int i = 0; i < mergelist.size(); i++) {
			long cur_attacker = Helper.getAttacker(mergelist, i);
			if(wizardList[(int) (cur_attacker - 1)].getStatus() == 1) {
				if(Helper.getSuccess(mergelist, i)) {
					long attacked = Helper.attackedWho(mergelist, i);
					wizardList[(int) (attacked - 1)].setStatus();
					deadPlayers.add(attacked);
					alivePlayers--;
					
				}
				else {
					continue;
				}
			}
			
		}
		System.out.println("deadplayers: " + deadPlayers.toString());
		return deadPlayers;
	}
	
	
	
	public void run() {
		String line = "";
		System.out.println("Hellllllo");
		out.println(Messages.init(kill_probablities, player_num, kill_probablity).toString());

		while (!line.equals("Over")) {
			merge = new ArrayList<String>();
			try
			{ 
				ArrayList<Long> deadPlayers = new ArrayList<Long>();
				line = input.readLine(); 
				System.out.println("Peer: " + line);
				if(!line.equals("Over")) {
					merge.add(line);
					System.out.println("Merge: " + player_num + ": " + merge.toString());
					do {
						deadPlayers = gameLogic(merge);
					} while(deadPlayers == null);
			
					//JSONObject json = Messages.strToJson(line);
					//Helper.seeAttacks(json);
					iteration++;
					//wizard.setStatus(line);
					//line = Integer.toString(counter);
					//counter += 1;
					//line = wizard.attack(wizard.hitRate); 
					//line = sc.nextLine();
					out.println(Messages.feedback(deadPlayers, alivePlayers).toString());
					if(alivePlayers < 2) {
						if(wizardList[(int) (player_num - 1)].getStatus() == 1) {
							System.out.println("Player: " + player_num + " wins!!!");
						}
					}
					}
			} 
			catch(IOException e) {
				System.out.println(e);
			}
		}
		System.out.println("Lobby: Closing Connection to: " + player_num);
		 try
	        { 
	            this.input.close(); 
	            this.out.close(); 
	              
	        }catch(IOException e){ 
	            e.printStackTrace(); 
	        } 
	}
}
