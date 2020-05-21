package main.java.unimelb.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import com.google.common.util.concurrent.UncheckedTimeoutException;

import javafx.application.Platform;
import main.java.unimelb.application.StartLobby;
import main.java.unimelb.utilities.Helper;
import main.java.unimelb.utilities.Messages;
import main.java.unimelb.utilities.VectorClock;

public class ClientHandler implements Runnable {

    private static Logger log = Logger.getLogger(ClientHandler.class.getName());
    private static volatile List<String> merge;  // List of player attacks in each round 
    private static volatile List<VectorClock> vcMerge; // List of vector clocks corresponding to player attacks in each round
    private static volatile List<Long> disconnectedPlayers;
    private static volatile VectorClock vectorClock; // Vector clock for the server (lobby)
    public static volatile int sharedAlive;
    
    private String received = ""; // Received message
    private String message = "";  // Outgoing message

    private BufferedReader in;
    private BufferedWriter out;
    private final List<Double> killProbabilities;
    private final double killProbability;
    private long playerNum;
    private int maxPlayers;
    private int alivePlayers;
    private List<Wizard> wizards;
    private SimpleTimeLimiter limiter = SimpleTimeLimiter.create(Executors.newSingleThreadExecutor());
    private int specialAttackCtr = 0;
    private boolean specialAttack = false;

    public ClientHandler(BufferedReader in, BufferedWriter out, List<Double> killProbabilities, long playerNum, int maxPlayers) {
        this.in = in;
        this.out = out;
        this.killProbabilities = killProbabilities;
        this.killProbability = killProbabilities.get((int) playerNum-1);
        this.playerNum = playerNum;
        this.maxPlayers = maxPlayers;
        this.alivePlayers = maxPlayers;
        sharedAlive = maxPlayers;

        // Initialize vector clock for server (shared by all client handler threads)
        int totalProcesses = maxPlayers + 1;
        vectorClock = new VectorClock(totalProcesses, 0);
        log.info("Vector clock initialised: " + vectorClock);

        // Initialize merge list
        merge = new ArrayList<>();
        
        //Initialize vcMerge list
        vcMerge = new ArrayList<>();
        
        disconnectedPlayers = new ArrayList<>();

        // Initialize wizards list
        this.wizards = new ArrayList<>();
        for (int i = 0; i < maxPlayers; i++) {
            Wizard wizard = new Wizard(killProbabilities.get(i), i+1);
            wizards.add(wizard);
        }
        // TODO: Log wizards list
    }

    /**
     * The main loop of the
     * @param merge merge list
     * @return list of dead players (numbers)
     */
    private List<Long> gameLogic(List<String> merge, List<Long> disconnectedPlayers, List<VectorClock> vcMerge) {
    	// Wait until response from all alive players received
        if (merge.size() < sharedAlive) {
            return null;
        }
        List<Long> deadPlayers = new ArrayList<>();
        if(disconnectedPlayers.size() > 0) {
        	for(int i = 0; i < disconnectedPlayers.size(); i++) {
        		deadPlayers.add(disconnectedPlayers.get(i));
        		alivePlayers--;
        	}
        }
        // Sort attacks based on vector clocks
        merge = Helper.sortList(merge, vcMerge);
        log.info("Sorted merge: " + merge.toString());

        /*For every attack in sorted list of attacks:
         * If attacker is alive and attack is successful:
         * Kill attacked player if it is alive.
         */
        for (int i = 0; i < merge.size(); i++) {
            long attacker = Helper.getAttacker(merge, i);
            Wizard attackingWizard = wizards.get((int) attacker-1);
            if (attackingWizard.getStatus() == 1) {
                if (Helper.getSuccess(merge, i)) {
                    long attacked = Helper.attackedWho(merge, i);
                    if(wizards.get((int) attacked-1).getStatus() == 1) {
                    	wizards.get((int) attacked-1).setStatus();
                    	deadPlayers.add(attacked);
                    	alivePlayers--;
                    }
                }
            }
        }

        

        log.info("Dead players: " + deadPlayers.toString());
        return deadPlayers;
    }

    /**
     * Sends a message to the server (lobby)
     * @param message message
     */
    private void send(String message) {
        try {
            log.info("Sending message " + message);
            out.write(message + '\n');
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Receive incoming messages from the input buffer.
     * Note that this blocks until a message is received
     */
    private synchronized String receive() {
        String message;
        try {
            log.info("Waiting to receive message");
            Platform.runLater(() -> StartLobby.getController().addLog("Waiting to receive message"));
            Platform.runLater(() -> StartLobby.getController().updateLog());
            message = in.readLine();
            log.info("Received message " + message);
            Platform.runLater(() -> StartLobby.getController().addLog("Received message " + message));
            Platform.runLater(() -> StartLobby.getController().updateLog());
            if (vectorClock != null) {
                JSONObject json = Messages.strToJson(message);
                JSONArray timestampsJson = (JSONArray) json.get("vectorClock");
                List<Long> timestamps = new ArrayList<>(timestampsJson);
                VectorClock other = new VectorClock(timestamps);
                vectorClock.onReceive(other);
            }
            return message;
        } catch (IOException e) {
            log.warning("Exception here");
            e.printStackTrace();
        }
        log.warning("Returning null message");
        return null;
    }
    
    /**
     * Receive incoming messages from the input buffer.
     * A variation to receive() to handle timeouts and does not block
     */
    private synchronized String receive2(String message) {
        log.info("Received message " + message);
        Platform.runLater(() -> StartLobby.getController().addLog("Received message " + message));
        Platform.runLater(() -> StartLobby.getController().updateLog());
		if (vectorClock != null) {
		    JSONObject json = Messages.strToJson(message);
		    JSONArray timestampsJson = (JSONArray) json.get("vectorClock");
		    List<Long> timestamps = new ArrayList<>(timestampsJson);
		    VectorClock other = new VectorClock(timestamps);
		    vectorClock.onReceive(other);
		}
		return message;
    }
    
    /**
     * Store attacks received from each alive player in a single shared list
     * Also store corresponding vector clock values 
     */
    private synchronized void store(String message) {
    	merge.add(message);
        log.info("Merge: " + playerNum + ": " + merge.toString());
        Platform.runLater(() -> StartLobby.getController().addLog("Merge: " + playerNum + ": " + merge.toString()));
        Platform.runLater(() -> StartLobby.getController().updateLog());
        vcMerge.add(vectorClock);
    }

    @Override
    public void run() {

        String command = "";  // Command
        JSONObject json;
        boolean disconnected = false;
        // Send client's process ID to them so they can initialize their vector clock
        int totalProcesses = maxPlayers + 1;
        message = Messages.processInfo(totalProcesses, (int) playerNum).toJSONString();
        send(message);
        Platform.runLater(() -> StartLobby.getController().addLog("Send to Client: " + message));
        Platform.runLater(() -> StartLobby.getController().updateLog());
        // Send kill probabilities to client
        message = Messages.init(killProbabilities, playerNum, killProbability, vectorClock).toString();
        send(message);
        Platform.runLater(() -> StartLobby.getController().addLog("Send to Client: " + message));
        Platform.runLater(() -> StartLobby.getController().updateLog());

        // Main loop of the game
        while (!command.equals("Over")) {
        	specialAttackCtr += 1;
        	if((((specialAttackCtr - 1) % maxPlayers) + 1) == playerNum) {
        		specialAttack = true;
        	}
        	else {
        		specialAttack = false;
        	}
        	sharedAlive = this.alivePlayers;
        	// Reset shared lists in each round
            merge = new ArrayList<>();
            vcMerge = new ArrayList<>();
            List<Long> deadPlayers = new ArrayList<Long>();
            disconnectedPlayers = new ArrayList<Long>();
            try {
            	log.info("Waiting to receive message");
                received = limiter.callWithTimeout(in::readLine, 60, TimeUnit.SECONDS);
            } catch (TimeoutException | UncheckedTimeoutException e) {
                System.out.println("This player has timedout");
                Platform.runLater(() -> StartLobby.getController().addLog("This player has timedout"));
                Platform.runLater(() -> StartLobby.getController().updateLog());
                // Update Vector Clock on timeout
                vectorClock.onInternalEvent();
                disconnected = true;
            } catch (Exception e) {
                // something bad happened while reading the line
            }
            // Killing timed out players
            if(disconnected) {
            	wizards.get((int) (playerNum - 1)).setStatus();
            	sharedAlive = sharedAlive - 1;
            	disconnectedPlayers.add(playerNum);
            	break;
            }
            else {
            	receive2(received);
            	json = Messages.strToJson(received);
            	command = json.get("command").toString();
            }
            if (!command.equals("Over") && !command.equals("")) {
            	store(received);
            	// Wait until responses are received from all alive players
                do {
                    deadPlayers = gameLogic(merge, disconnectedPlayers, vcMerge);
                } while (deadPlayers == null);
             

                // Send feedback to the player of result of the round
                message = Messages.feedback(deadPlayers, alivePlayers, vectorClock, specialAttack).toString();
                send(message);

                if (alivePlayers < 2) {
                    Wizard wizard = wizards.get((int) playerNum-1);
                    if (wizard.getStatus() == 1) {
                        log.info("Player " + playerNum + " wins!");
                        Platform.runLater(() -> StartLobby.getController().addLog("Player " + playerNum + " wins!"));
                        Platform.runLater(() -> StartLobby.getController().updateLog());
                        vectorClock.onInternalEvent();
                    }
                }
            }
           

        }

        // Close connection to client player
        log.info("Closing connection to Player " + playerNum);
        try {
            if (in != null && out != null) {
            	vectorClock.onInternalEvent();
                in.close();
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}