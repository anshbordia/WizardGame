package unimelb.wizardstandoff;

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

public class ClientHandler implements Runnable {

    private static Logger log = Logger.getLogger(ClientHandler.class.getName());
    private static volatile List<String> merge;
    private static volatile List<VectorClock> vcMerge;
    private static volatile List<Long> disconnectedPlayers;
    private static volatile int iteration = 0;
    private static volatile VectorClock vectorClock;
    public static volatile int sharedAlive;

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

        // Initialise vector clock for server (shared by all client handler threads)
        int totalProcesses = maxPlayers + 1;
        vectorClock = new VectorClock(totalProcesses, 0);
        log.info("Vector clock initialised: " + vectorClock);

        // Initialise merge list
        merge = new ArrayList<>();
        
        //Initialise vcMerge list
        vcMerge = new ArrayList<>();
        
        disconnectedPlayers = new ArrayList<>();

        // Initialise wizards list
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
        merge = Helper.sortList(merge, vcMerge);
        log.info("Sorted merge: " + merge.toString());

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
            message = in.readLine();
            log.info("Received message " + message);
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
    
    private synchronized String receive2(String message) {
        log.info("Received message " + message);
		if (vectorClock != null) {
		    JSONObject json = Messages.strToJson(message);
		    JSONArray timestampsJson = (JSONArray) json.get("vectorClock");
		    List<Long> timestamps = new ArrayList<>(timestampsJson);
		    VectorClock other = new VectorClock(timestamps);
		    vectorClock.onReceive(other);
		}
		return message;
    }
    
    private synchronized void store(String message) {
    	merge.add(message);
        log.info("Merge: " + playerNum + ": " + merge.toString());
        vcMerge.add(vectorClock);
    }

    @Override
    public void run() {
        String received = ""; // Received message
        String message = "";  // Outgoing message
        String command = "";  // Command
        JSONObject json;
        boolean disconnected = false;
        // Send client's process ID to them so they can initialise their vector clock
        int totalProcesses = maxPlayers + 1;
        message = Messages.processInfo(totalProcesses, (int) playerNum).toJSONString();
        send(message);

        // Send kill probabilities to client
        message = Messages.init(killProbabilities, playerNum, killProbability, vectorClock).toString();
        send(message);

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
        	// Reset merge list
            merge = new ArrayList<>();
            vcMerge = new ArrayList<>();
            List<Long> deadPlayers = new ArrayList<Long>();
            disconnectedPlayers = new ArrayList<Long>();
            try {
            	log.info("Waiting to receive message");
                received = limiter.callWithTimeout(in::readLine, 60, TimeUnit.SECONDS);
            } catch (TimeoutException | UncheckedTimeoutException e) {
                System.out.println("This player has timedout");
                //vectorClock.onInternalEvent();
                disconnected = true;
            } catch (Exception e) {
                // something bad happened while reading the line
            }
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
            //received = receive();
            if (!command.equals("Over") && !command.equals("")) {
            	store(received);
                do {
                    deadPlayers = gameLogic(merge, disconnectedPlayers, vcMerge);
                } while (deadPlayers == null);
                iteration += 1;

                // Send feedback to the player
                message = Messages.feedback(deadPlayers, alivePlayers, vectorClock, specialAttack).toString();
                send(message);

                if (alivePlayers < 2) {
                    Wizard wizard = wizards.get((int) playerNum-1);
                    if (wizard.getStatus() == 1) {
                        log.info("Player " + playerNum + " wins!");
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