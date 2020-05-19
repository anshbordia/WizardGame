package main.java.unimelb.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StartLobby extends Application {

	private Stage stage;
	
	public static StartLobby instance;
	
	public static LobbyController lobbyController;

	
	@Override
	public void start(Stage primaryStage) throws Exception {
		stage = primaryStage;
        stage.setTitle("Lobby");
        stage.setResizable(false);
        gotoMain();
        stage.show();
	}
	

	
	public StartLobby() {
		instance = this;
	}
	
	public static LobbyController getController() {
		return lobbyController;
	}
	
	public static StartLobby getInstance() {
		return instance;
	}
	
	
	
	public void gotoMain() {
		try {
			replaceSceneContent("LOBBY.fxml");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	private Parent replaceSceneContent(String fxml) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml), null, new JavaFXBuilderFactory());
		Parent page = loader.load();
		lobbyController = (LobbyController)loader.getController();
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(page);
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            stage.setScene(scene);
        } 
        else {
        	scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            stage.getScene().setRoot(page);
        }
        stage.sizeToScene();
        return page;
	}
	
	public static void main(String[] args) {
        launch(args);
    }

}
