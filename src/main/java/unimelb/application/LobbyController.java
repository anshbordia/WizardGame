package main.java.unimelb.application;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import main.java.unimelb.core.Lobby;

public class LobbyController implements Initializable {
	
	@FXML
	private TextField portTextField;
	
	@FXML
	private TextField hostTextField;
	
	@FXML
	private ChoiceBox<String> choiceBox;
	
	@FXML
	private ListView<String> logListView;
	
	private int port;
	
	@FXML
	private Button button;
	
	private ObservableList<String> logList = FXCollections.observableArrayList();

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		ObservableList<String> list = FXCollections.observableArrayList();
		list.add("2");
		list.add("3");
		list.add("4");
		choiceBox.setItems(list);
		Platform.setImplicitExit(false);
		numberOnly();
		String ipString = "Unknown IP";
		try {
			InetAddress inetAddress = InetAddress.getLocalHost();
			ipString = inetAddress.getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		hostTextField.setText(ipString);
		hostTextField.setDisable(true);
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
	
	public void addLog(String string) {
		logList.add(string);
	}
	
	public void updateLog() {
		logListView.setItems(logList);
	}
	
	@FXML
	private void StartServer(ActionEvent event) {
		System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tc] %2$s %4$s: %5$s%n");
		if (Pattern.matches("(6553[0-5]|655[0-2][0-9]\\d|65[0-4](\\d){2}|6[0-4](\\d){3}|[1-5](\\d){4}|[1-9](\\d){0,3})", portTextField.getText())) {
			try {
				port = Integer.parseInt(portTextField.getText());
		        Lobby lobby = new Lobby(port, Integer.parseInt(choiceBox.getSelectionModel().getSelectedItem()));
		        Thread lobbyThread = new Thread(lobby);
		        lobbyThread.start();
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				button.setDisable(true);
				portTextField.setDisable(true);
				choiceBox.setDisable(true);
			}
			
		} else {
			return;
		}

	}
	

}
