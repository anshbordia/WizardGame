package unimelb.wizardstandoff;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

public class Peer implements Runnable {

    private static Logger log = Logger.getLogger(Peer.class.getName());

    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private Wizard wizard;
    private VectorClock vectorClock;

    public Peer(String ip, int port) {
        // Attempt to connect to the lobby
        try {
            this.socket = new Socket(ip, port);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            log.info("Connected to lobby successfully");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a message to server
     * @param message message
     */
    private void send(String message) {
        try {
            // Send message
            log.info("Sending message " + message);
            out.write(message + '\n');
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Receive incoming messages from the input buffer. Note
     * this blocks until a message is received
     * @return received message
     */
    private String receive() {
        String message;
        try {
            // Receive message
            log.info("Waiting to receive message");
            message = in.readLine();
            log.info("Received message " + message);

            // Update local vector clock, given the
            // vector clock from the sender's message
            if (vectorClock != null) {
                JSONObject json = Messages.strToJson(message);
                JSONArray timestampsJson = (JSONArray) json.get("vectorClock");
                List<Long> timestamps = new ArrayList<>(timestampsJson);
                VectorClock other = new VectorClock(timestamps);
                vectorClock.onReceive(other);
            }
            return message;
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.warning("Returning null message");
        return null;
    }

    /**
     * Initialise this peer's vector clock, given
     * the total number of processes, and this peer's
     * assigned process ID
     * @param processInfo JSON object message containing process info
     */
    private void initialiseVectorClock(JSONObject processInfo) {
        long totalProcesses = (long) processInfo.get("totalProcesses");
        long processID = (long) processInfo.get("processID");
        this.vectorClock = new VectorClock((int) totalProcesses, (int) processID);
        log.info("Vector clock initialised: " + vectorClock);
    }

    @Override
    public void run() {
        log.info("Starting peer thread");
        String received; // Incoming message
        String message;  // Outgoing message
        String command = "";
        JSONObject json;

        // Initialise vector clock
        received = receive();
        json = Messages.strToJson(received);
        this.initialiseVectorClock(json);


        while (!command.equals("Begin")) {
            received = receive();
            json = Messages.strToJson(received);
            command = json.get("command").toString();
            double killProbability = (double) json.get("yourprob");
            long playerNum = (long) json.get("playernum");
            wizard = new Wizard(killProbability, playerNum);
            Helper.getStatistics(json);
        }

        while (wizard.getStatus() == 1) {
            Scanner scanner = new Scanner(System.in);
            int attackWho;
            long time;
            boolean success;

            try {
                // Prompt player to get ready to input attack
                log.info("Get ready to enter the player number you wish to attack! (E.g. '2' for Player 2)");
                Thread.sleep(3000);
                log.info("Ready?...");
                log.info("5");
                Thread.sleep(1000);
                log.info("4");
                Thread.sleep(1000);
                log.info("3");
                Thread.sleep(1000);
                log.info("2");
                Thread.sleep(1000);
                log.info("1");
                Thread.sleep(1000);
                log.info("Attack!");

                attackWho = scanner.nextInt();
                time = Helper.getTime();
                success = wizard.attack();

                // Send attack command to server
                message = Messages.sendAttack(attackWho, time, success, wizard.getWizardNum(), vectorClock).toString();
                send(message);

                // Receive response from server
                received = receive();
                log.info("Received from lobby message " + message);

                boolean deadOrAlive = Helper.processFeedback(received, wizard.getWizardNum());
                if (deadOrAlive) {
                    wizard.setStatus();
                    send("Over");
                } else {
                    if (Helper.amIwinner(received)) {
                        log.info("You are the winner!");
                        wizard.setStatus();
                        send("Over");
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // Close the connection to server
        try {
            log.info("Closing connection to server");
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tc] %2$s %4$s: %5$s%n");

        Peer peer = new Peer("127.0.0.1", 9000);
        Thread peerThread = new Thread(peer);
        peerThread.start();
    }
}