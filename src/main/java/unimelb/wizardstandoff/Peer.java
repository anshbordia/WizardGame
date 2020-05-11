package unimelb.wizardstandoff;

import java.net.*;
import java.util.Scanner;

import org.json.simple.JSONObject;

import java.io.*;

public class Peer {
	private Socket socket = null;
	private BufferedReader input = null;
	PrintWriter out = null; 
	Scanner sc = new Scanner(System.in);
	private Wizard wizard = null;
	
	//Constructor
	public Peer(String ip, int port) {
		try {
			//wait(10000);
			socket = new Socket(ip, port);
			System.out.println("Peer: Connected to lobby peer");
			input = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
			out = new PrintWriter(
					new BufferedWriter(new OutputStreamWriter(
							socket.getOutputStream(), "UTF-8")), true);
		}
		catch(UnknownHostException e) {
			System.out.println(e);
		}
		catch(IOException e) {
			System.out.println(e);
		}
		
		
		String line = "";
		String command = "";
		JSONObject json;
		while(!command.equals("Begin")) {
			try {
				line = input.readLine();
				json = Messages.strToJson(line);
				command = json.get("command").toString();
				wizard = new Wizard((double) json.get("yourprob"));
				Helper.getStatistics(json);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		line = "";
		String status = "";
		int attackWho = -1;
		long time = -1;
		boolean success = true;
		while (!status.equals("Dead")) 
		{ 
			try
			{
				wizard.attack(wizard.hitRate);
				System.out.println("Ready?...");
				Thread.sleep(3000);
				System.out.println("Shoot!!!");
				attackWho = sc.nextInt();
				time = Helper.getTime();
				success = wizard.attack(wizard.hitRate);
				out.println(Messages.sendAttack(attackWho, time, success)); 
				line = input.readLine(); 
				//wizard.setStatus(line);
				System.out.println("Lobby: " + line);
			} 
			catch(IOException i) 
			{ 
					System.out.println(i); 
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		} 
		try {
			input.close();
			out.close();
			socket.close();
		}
		catch(IOException e) {
			System.out.println(e);
		}
	}
}