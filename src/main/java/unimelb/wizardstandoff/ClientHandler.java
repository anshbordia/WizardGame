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
	static int total_players = 0;
	final double kill_probablity ;
	final int player_num;
	final double[] kill_probablities;
	public static volatile ArrayList<String> merge = new ArrayList<String>();;
	public static int iteration = 0; 
	
	public ClientHandler(Socket socket, BufferedReader input, PrintWriter out, double[] kill_probablities, int player_num) {
		this.socket = socket;
		this.input = input;
		this.out = out;
		this.kill_probablities = kill_probablities;
		this.player_num = player_num;
		this.kill_probablity = kill_probablities[player_num - 1];
		total_players += 1;
		
	}

	public static void gameLogic(ArrayList<String> mergelist) {
		
	}
	
	public void run() {
		String line = "";
		out.println(Messages.init(kill_probablities, player_num, kill_probablity).toString());
		
		while (!line.equals("Over")) 
		{ 
			try
			{ 
				line = input.readLine(); 
				merge.add(line);
				System.out.println("Merge: " + player_num + ": " + merge.toString());
				JSONObject json = Messages.strToJson(line);
				//Helper.seeAttacks(json);
				iteration++;
				//wizard.setStatus(line);
				System.out.println("Peer: " + line);
				line = Integer.toString(counter);
				counter += 1;
				
				
				//line = wizard.attack(wizard.hitRate); 
				//line = sc.nextLine();
				out.println(line); 
				
			} 
			catch(IOException e) {
				System.out.println(e);
			}
		}
		System.out.println("Lobby: Closing Connection to you");
		 try
	        { 
	            this.input.close(); 
	            this.out.close(); 
	              
	        }catch(IOException e){ 
	            e.printStackTrace(); 
	        } 
	}
}
