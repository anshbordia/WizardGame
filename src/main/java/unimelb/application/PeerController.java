package main.java.unimelb.application;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import main.java.unimelb.core.Peer;

public class PeerController implements Initializable {
	
	private Peer peer = null;
	
	@FXML
	private Text gameText;
	
	@FXML
	private TextField portTextField;
	
	@FXML
	private TextField hostTextField;
	
	@FXML
	private Button connectButton;
	
	@FXML
	private Button attackButton;
	
	@FXML
	private ListView<String> listView;
	
	@FXML
	private ChoiceBox<String> choiceBox;
	
	@FXML
	private ListView<String> logListView;
	
	@FXML
	private VBox vbox;
	
	@FXML
	private Text statusText;
	
	private ObservableList<String> playerList = FXCollections.observableArrayList();
	
	private ObservableList<String> onlineList = FXCollections.observableArrayList();
	
	private ObservableList<String> logList = FXCollections.observableArrayList();
	
	private int counter = 1;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		vbox.setVisible(false);
		numberOnly();
		attackButton.setDisable(true);
	}
	
	public void setGameText(String string) {
		gameText.setText(string);
	}
	
	public void setStatusText(String string) {
		statusText.setText(string);
	}
	
	public void setButtonDisable(Boolean status) {
		attackButton.setDisable(status);
	}
	
	public void addUser(String prob) {
		onlineList.add("Player: " + counter + " Hit Rate: " + prob);
		playerList.add(Integer.toString(counter));
		counter++;
	}
	
	public void addLog(String string) {
		logList.add(string);
	}
	
	public void updateLog() {
		logListView.setItems(logList);
	}
	
	public void updateList() {
		listView.setItems(onlineList);
		choiceBox.setItems(playerList);
	}
	
	private void numberOnly() {
		portTextField.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, 
		        String newValue) {
		        if (!newValue.matches("\\d*")) {
		        	portTextField.setText(newValue.replaceAll("[^\\d]", ""));
		        }
		    }
		});
	}
	
	@FXML
	private void connectServer() {
		if (Pattern.matches("(6553[0-5]|655[0-2][0-9]\\d|65[0-4](\\d){2}|6[0-4](\\d){3}|[1-5](\\d){4}|[1-9](\\d){0,3})", portTextField.getText())) {
			try {
				peer = new Peer(hostTextField.getText(), Integer.parseInt(portTextField.getText()));
				Thread peerThread = new Thread(peer);
		        peerThread.start();
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				gameText.setText("Connected, Waiting for players");
		        portTextField.setDisable(true);
		        hostTextField.setDisable(true);
		        connectButton.setDisable(true);
		        vbox.setVisible(true);
			}
		} else {
			return;
		}
	}
	
	@FXML
	private void attack() {
		Thread thread = new Thread(() -> peer.attack(Integer.parseInt(choiceBox.getSelectionModel().getSelectedItem())));
		thread.setDaemon(true);
		thread.start();
	}

}
