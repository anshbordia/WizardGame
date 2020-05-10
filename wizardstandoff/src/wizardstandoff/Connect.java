package wizardstandoff;

import java.util.Scanner;

public class Connect {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter 1 to host a game, 2 to join a game");
		int gameState = sc.nextInt();
		if(gameState == 1) {
			Thread lobbyThread = new Thread(new Runnable() {
				  @Override
				  public void run() {
				    new Lobby(9000);
				  }
				});
			lobbyThread.start();
		}
		else {
			Thread peerThread = new Thread(new Runnable() {
				  @Override
				  public void run() {
				    new Peer("127.0.0.1", 9000);
				  }
				});
				peerThread.start();
			
		}
			
	}

}
