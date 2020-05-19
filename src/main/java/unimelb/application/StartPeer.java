package main.java.unimelb.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StartPeer extends Application {
	
	private Stage stage;
	
	public static StartPeer instance;
	
	public static PeerController peerController;

	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		stage = primaryStage;
        stage.setTitle("Lobby");
        stage.setResizable(false);
        gotoMain();
        stage.show();
	}
	
	public StartPeer() {
		instance = this;
	}
	
	public static PeerController getController() {
		return peerController;
	}
	
	public static StartPeer getInstance() {
		return instance;
	}
	
	
	
	public void gotoMain() {
		try {
			replaceSceneContent("PEER.fxml");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Parent replaceSceneContent(String fxml) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml), null, new JavaFXBuilderFactory());
		Parent page = loader.load();
		peerController = (PeerController)loader.getController();
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
