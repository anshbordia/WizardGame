package wizardstandoff;

import java.net.*;
import java.util.Scanner;
import java.io.*;

public class Lobby {
	private Socket socket = null;
	private ServerSocket lobby = null;
	private BufferedReader input = null;
	PrintWriter out = null; 
	Scanner sc = new Scanner(System.in);
	private Socket[] socketlist = new Socket[2];
	private double[] kill_probablities = new double[2];


	public Lobby(int port) {
		try {
			lobby = new ServerSocket(port);
			
			System.out.println("Lobby: Lobby Started! Waiting for players...");
			int player_num = 1;
			while(player_num < 3) {
				socketlist[player_num - 1] = lobby.accept();
				System.out.println("Player " + player_num + " Connected!");
				kill_probablities[player_num - 1] = Math.random() * 0.9 + 0.05;
				player_num += 1;
				
			}
			for(int k = 0; k < 2; k++ ) {
				out = new PrintWriter(
						new BufferedWriter(new OutputStreamWriter(
								socketlist[k].getOutputStream(), "UTF-8")), true); 		
				input = new BufferedReader(new InputStreamReader(socketlist[k].getInputStream(), "UTF-8"));
				System.out.println("Assigning thread to this player");
				ClientHandler player = new ClientHandler(socketlist[k], input, out, kill_probablities, k + 1);
				Thread t = new Thread(player);
				t.start();
			}
		}
		catch(IOException e) {
			try {
				socket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}
	//Constructor
	/*public Lobby(int port) {
		try {
			lobby = new ServerSocket(port);
			System.out.println("Lobby: Lobby Started! Waiting for players...");
			socket = lobby.accept();
			System.out.println("Lobby: Peer Joined!");
			
			out = new PrintWriter(
					new BufferedWriter(new OutputStreamWriter(
							socket.getOutputStream(), "UTF-8")), true); 		
			input = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
			
			Wizard wizard = new Wizard(0.6);
			String line = "";
			
			while (!line.equals("Attack")) 
			{ 
				try
				{ 
					line = input.readLine(); 
					//wizard.setStatus(line);
					System.out.println("Peer: " + line); 
					//line = wizard.attack(wizard.hitRate); 
					line = sc.nextLine();
					out.println(line); 
				} 
				catch(IOException e) {
					System.out.println(e);
				}
			}
			System.out.println("Lobby: Closing Connection");
			input.close();
			out.close();
			socket.close();
			
		}
		catch(IOException e) {
			System.out.println(e);
		}
	}*/
}
