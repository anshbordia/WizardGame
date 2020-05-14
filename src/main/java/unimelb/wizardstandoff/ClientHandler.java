package unimelb.wizardstandoff;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {

    private static Logger log = Logger.getLogger(ClientHandler.class.getName());
    private static volatile List<String> merge;
    private static volatile int iteration = 0;

    private BufferedReader in;
    private BufferedWriter out;
    private final List<Double> killProbabilities;
    private final double killProbability;
    private long playerNum;
    private int maxPlayers;
    private int alivePlayers;
    private List<Wizard> wizards;


    public ClientHandler(BufferedReader in, BufferedWriter out, List<Double> killProbabilities, long playerNum, int maxPlayers) {
        this.in = in;
        this.out = out;
        this.killProbabilities = killProbabilities;
        this.killProbability = killProbabilities.get((int) playerNum-1);
        this.playerNum = playerNum;
        this.maxPlayers = maxPlayers;
        this.alivePlayers = maxPlayers;

        // Initialise merge list
        merge = new ArrayList<>();

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
    private List<Long> gameLogic(List<String> merge) {
        if (merge.size() < this.alivePlayers) {
            return null;
        }
        List<Long> deadPlayers = new ArrayList<>();
        merge = Helper.sortList(merge);
        log.info("Sorted merge: " + merge.toString());

        for (int i = 0; i < merge.size(); i++) {
            long attacker = Helper.getAttacker(merge, i);
            Wizard attackingWizard = wizards.get((int) attacker-1);
            if (attackingWizard.getStatus() == 1) {
                if (Helper.getSuccess(merge, i)) {
                    long attacked = Helper.attackedWho(merge, i);
                    Wizard attackedWizard = wizards.get((int) attacked-1);
                    attackedWizard.setStatus();
                    deadPlayers.add(attacked);
                    alivePlayers--;
                }
            }
        }

        // Reset merge list
        merge = new ArrayList<>();

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
    private String receive() {
        String message;
        try {
            log.info("Waiting to receive message");
            message = in.readLine();
            log.info("Received message " + message);
            return message;
        } catch (IOException e) {
            log.warning("Exception here");
            e.printStackTrace();
        }
        log.warning("Returning null message");
        return null;
    }


    @Override
    public void run() {
        String received; // Received message
        String message;  // Outgoing message

        // Send kill probabilities to client
        message = Messages.init(killProbabilities, playerNum, killProbability).toString();
        send(message);

        // Main loop of the game
        while (!(received = receive()).equals("Over")) {
            List<Long> deadPlayers;
            if (!received.equals("Over")) {
                merge.add(received);
                log.info("Merge: " + playerNum + ": " + merge.toString());
                do {
                    deadPlayers = gameLogic(merge);
                } while (deadPlayers == null);
                iteration += 1;

                // Send feedback to the player
                message = Messages.feedback(deadPlayers, alivePlayers).toString();
                send(message);

                if (alivePlayers < 2) {
                    Wizard wizard = wizards.get((int) playerNum-1);
                    if (wizard.getStatus() == 1) {
                        log.info("Player " + playerNum + " wins!");
                    }
                }
            }
        }

        // Close connection to client player
        log.info("Closing connection to Player " + playerNum);
        try {
            if (in != null && out != null) {
                in.close();
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}