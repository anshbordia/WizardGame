package unimelb.core;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javafx.application.Platform;
import unimelb.application.StartPeer;
import unimelb.utilities.Helper;
import unimelb.utilities.Messages;
import unimelb.utilities.VectorClock;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import java.text.DecimalFormat;

public class Peer implements Runnable {

    private static Logger log = Logger.getLogger(Peer.class.getName());
    private static DecimalFormat df2 = new DecimalFormat("#.##");
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private Wizard wizard;
    private VectorClock vectorClock;
    private long startTime;
    private long endTime;
    private boolean specialAttack = false;
    private long time;
    private boolean success;
    private String received; // Incoming message
    private String message;  // Outgoing message

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
     */
    @SuppressWarnings("unchecked")
	private synchronized String receive() {
        String message;
        try {
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

    @SuppressWarnings("unchecked")
	@Override
    public void run() {
        log.info("Starting peer thread");

        String command = "";
        JSONObject json;

        // Initialise vector clock
        received = receive();
        Platform.runLater(() -> StartPeer.getController().addLog("Received from Server: " + received));
        Platform.runLater(() -> StartPeer.getController().updateLog());
        json = Messages.strToJson(received);
        this.initialiseVectorClock(json);


        while (!command.equals("Begin")) {
            received = receive();
            Platform.runLater(() -> StartPeer.getController().addLog("Received from Server: " + received));
            Platform.runLater(() -> StartPeer.getController().updateLog());
            json = Messages.strToJson(received);
            command = json.get("command").toString();
            List<Double> arrayList = (List<Double>) json.get("probs");
            for (Double string : arrayList) {
            	Platform.runLater(() -> StartPeer.getController().addUser(df2.format(string)));
			}
            double killProbability = (double) json.get("yourprob");
            long playerNum = (long) json.get("playernum");
            Platform.runLater(() -> StartPeer.getController().updateList());
            Platform.runLater(() -> StartPeer.getController().setStatusText("Player ID: " + playerNum + " Hit Rate: " + df2.format(killProbability)));
            wizard = new Wizard(killProbability, playerNum);
            Helper.getStatistics(json);
        }
        countDown();

//        while (wizard.getStatus() == 1) {
//            
//        }
        // Close the connection to server

    } 
    
    private void countDown() {
    	try {
    		if (wizard.getStatus() == 1) {
    			Platform.runLater(() -> StartPeer.getController().setButtonDisable(true));
                // Prompt player to get ready to input attack
            	Platform.runLater(() -> StartPeer.getController().setGameText("Ready?..."));
                Thread.sleep(3000);
                log.info("Ready?...");
                log.info("5");
                Platform.runLater(() -> StartPeer.getController().setGameText("5"));
                Thread.sleep(1000);
                log.info("4");
                Platform.runLater(() -> StartPeer.getController().setGameText("4"));
                Thread.sleep(1000);
                log.info("3");
                Platform.runLater(() -> StartPeer.getController().setGameText("3"));
                Thread.sleep(1000);
                log.info("2");
                Platform.runLater(() -> StartPeer.getController().setGameText("2"));
                Thread.sleep(1000);
                log.info("1");
                Platform.runLater(() -> StartPeer.getController().setGameText("1"));
                Thread.sleep(1000);
                Platform.runLater(() -> StartPeer.getController().setButtonDisable(false));
                log.info("Attack!");
                Platform.runLater(() -> StartPeer.getController().setGameText("Attack!"));
			}

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public void attack(int attackWho) {
    	startTime = System.currentTimeMillis();
        vectorClock.onInternalEvent();
        time = Helper.getTime();
        if(specialAttack) {
        	Platform.runLater(() -> StartPeer.getController().setGameText("Special Attack!"));
        	success = true;
        }
        else {
        	success = wizard.attack();
        }
        // Send attack command to server
        message = Messages.sendAttack(attackWho, time, success, wizard.getWizardNum(), vectorClock).toString();
        send(message);
        Platform.runLater(() -> StartPeer.getController().addLog("Send to Server: " + message));
        Platform.runLater(() -> StartPeer.getController().updateLog());
        endTime = System.currentTimeMillis();
        if((endTime - startTime) > 35000) {
        	log.info("Damn it I was disconnected :(");
        	wizard.setStatus();
        	Platform.runLater(() -> StartPeer.getController().setButtonDisable(true));
        	Platform.runLater(() -> StartPeer.getController().setGameText("Disconnected!"));
            try {
                log.info("Closing connection to server");
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
     
        // Receive response from server
        received = receive();
        log.info("Received from lobby message " + received);
        Platform.runLater(() -> StartPeer.getController().addLog("Received from Server: " + received));
        Platform.runLater(() -> StartPeer.getController().updateLog());
        boolean deadOrAlive = Helper.processFeedback(received, wizard.getWizardNum());
        specialAttack = Helper.canIspecialAttack(received);
     
        System.out.println("Special Attack:" + specialAttack);
        if (deadOrAlive) {
        	Platform.runLater(() -> StartPeer.getController().setGameText("You died!"));
            wizard.setStatus();
            Platform.runLater(() -> StartPeer.getController().setButtonDisable(true));
            message = Messages.over(vectorClock).toString();
            send(message);
            Platform.runLater(() -> StartPeer.getController().addLog("Send to Server: " + message));
            Platform.runLater(() -> StartPeer.getController().updateLog());
        } else {
            if (Helper.amIwinner(received)) {
                log.info("You are the winner!");
                Platform.runLater(() -> StartPeer.getController().setGameText("You are the Winner!"));
                Platform.runLater(() -> StartPeer.getController().setButtonDisable(true));
                wizard.setStatus();
                message = Messages.over(vectorClock).toString();
                send(message);
                Platform.runLater(() -> StartPeer.getController().addLog("Send to Server: " + message));
                Platform.runLater(() -> StartPeer.getController().updateLog());
            } else {
            	countDown();
			}
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
