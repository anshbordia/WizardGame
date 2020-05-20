package main.java.unimelb.core;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javafx.application.Platform;
import main.java.unimelb.application.StartLobby;
import main.java.unimelb.application.StartPeer;

public class Lobby implements Runnable {

    private static Logger log = Logger.getLogger(Lobby.class.getName());

    private int port;
    private ServerSocket serverSocket;
    private List<Socket> clients;
    private List<Double> killProbabilities;  // Kill probabilities of all players
    private int maxPlayers;
    private int playersJoined = 0;

    public Lobby(int port, int number) {
        this.port = port;
        this.killProbabilities = new ArrayList<>();
        this.clients = new ArrayList<>();
        this.maxPlayers = number;
    }

    @Override
    public void run() {
        try {
            // Initialize the server (i.e. lobby)
            this.serverSocket = new ServerSocket(port);
            log.info("Lobby created! Waiting for players to join...");
            Platform.runLater(() -> StartLobby.getController().addLog("Lobby created! Waiting for players to join..."));
            Platform.runLater(() -> StartLobby.getController().updateLog());
            // Allow up to 'maxPlayers' players to join the lobby,
            // and assign each player with a 'killProbability'.
            
            //Wait until all players have joined.
            while (playersJoined < maxPlayers) {
                Socket clientSocket = serverSocket.accept();
                this.clients.add(clientSocket);
                playersJoined += 1;
                log.info("Player " + playersJoined + " has joined the lobby");
                Platform.runLater(() -> StartLobby.getController().addLog("Player " + playersJoined + " has joined the lobby"));
                Platform.runLater(() -> StartLobby.getController().updateLog());
                double killProbability = Math.random() * 0.9 + 0.05;
                killProbabilities.add(killProbability);
            }

            // Create and run a client handler thread for each player.
            for (int playerNum = 0; playerNum < maxPlayers; playerNum++) {
                Socket socket = this.clients.get(playerNum);
                BufferedReader in =
                        new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                BufferedWriter out =
                        new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                ClientHandler clientHandler = new ClientHandler(in, out, killProbabilities, (long) (playerNum+1), maxPlayers);
                Thread clientHandlerThread = new Thread(clientHandler);
                clientHandlerThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tc] %2$s %4$s: %5$s%n");

        Lobby lobby = new Lobby(9000, 2);
        Thread lobbyThread = new Thread(lobby);
        lobbyThread.start();
    }
}