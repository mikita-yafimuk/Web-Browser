package browser;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Starter extends Application {

    private TabPane root;
    private final static Logger logger = Logger.getLogger(Starter.class.getName());

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent browser = FXMLLoader.load(getClass().getResource("fxml/Browser.fxml"));
        Tab browserTab = new Tab("New Tab", browser);
        Tab addTab = new Tab("+", null);
        addTab.setClosable(false);
        addTab.setOnSelectionChanged(event -> addNewTab());
        root = new TabPane(browserTab, addTab);
        root.setTabMinWidth(50);
        root.setTabMinHeight(30);
        Scene scene = new Scene(root);

        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });

        primaryStage.getIcons().add(new Image("/images/icon.png"));
        primaryStage.setScene(scene);
        primaryStage.setTitle("Penguin");
        primaryStage.show();
    }

    private void addNewTab() {
        try {
            Parent browser = FXMLLoader.load(getClass().getResource("fxml/Browser.fxml"));
            Tab browserTab = new Tab("New Tab", browser);
            root.getTabs().add(root.getTabs().size() - 1, browserTab);
            root.getSelectionModel().select(browserTab);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
