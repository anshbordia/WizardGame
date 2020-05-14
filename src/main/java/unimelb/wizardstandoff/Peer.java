package unimelb.wizardstandoff;

import org.json.simple.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.logging.Logger;

public class Peer implements Runnable {

    private static Logger log = Logger.getLogger(Peer.class.getName());

    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private Wizard wizard;

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
    private String receive() {
        String message;
        try {
            log.info("Waiting to receive message");
            message = in.readLine();
            log.info("Received message " + message);
            return message;
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.warning("Returning null message");
        return null;
    }

    @Override
    public void run() {
        log.info("Starting peer thread");
        String received; // Incoming message
        String message;  // Outgoing message
        String command = "";
        JSONObject json;

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
                log.info("Ready?...");
                log.info("5");
                log.info("4");
                log.info("3");
                log.info("2");
                log.info("1");
                Thread.sleep(3000);
                log.info("Attack!");

                attackWho = scanner.nextInt();
                time = Helper.getTime();
                success = wizard.attack();

                // Send attack command to server
                message = Messages.sendAttack(attackWho, time, success, wizard.getWizardNum()).toString();
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