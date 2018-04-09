package browser.controller;

import browser.util.AutoCompleteTextField;
import browser.util.win32.NativeCalls;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import org.apache.commons.io.FileUtils;
import org.apache.commons.validator.routines.UrlValidator;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class BrowserController implements Initializable {

    @FXML
    BorderPane browserBorderPane;
    @FXML
    WebView browserWebView;
    @FXML
    TextField addressBar;
    @FXML
    Label status;
    @FXML
    ProgressIndicator progressIndicator;
    @FXML
    ImageView reloadImageView;
    @FXML
    ComboBox<String> searchBox;
    @FXML
    ProgressBar progressBar;
    @FXML
    ComboBox<String> comboBoxHistory;
    @FXML
    ComboBox<String> comboBoxBC;

    private SortedSet<String> WEBSITE_PROPOSALS = new TreeSet<>();
    private NativeCalls nativeCalls = new NativeCalls();

    public BrowserController() {
        loadListSites();
    }

    /*When new tab added*/
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        browserWebView.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                status.setText("loading... " + browserWebView.getEngine().getLocation());
                reloadImageView.setImage(new Image(getClass().getResourceAsStream("/images/stoploading.png")));
                progressIndicator.setVisible(true);
                progressBar.setVisible(true);
                status.setVisible(true);

                if (newValue == Worker.State.SUCCEEDED) {
                    addressBar.setText(browserWebView.getEngine().getLocation());
                    status.setText("loaded");
                    progressIndicator.setVisible(false);
                    progressBar.setVisible(false);
                    status.setVisible(false);
                    reloadImageView.setImage(new Image(getClass().getResourceAsStream("/images/reload.png")));

                    if (browserBorderPane.getParent() != null) {
                        TabPane tabPane = (TabPane) browserBorderPane.getParent().getParent();
                        for (Tab tab : tabPane.getTabs()) {
                            if (tab.getContent() == browserBorderPane) {
                                tab.setText(browserWebView.getEngine().getTitle() + "   ");
                                break;
                            }
                        }
                    }
                }
            }
        });

        /*Create autocomplete text field*/
        new AutoCompleteTextField().bindAutoCompletion(addressBar, 15, true, WEBSITE_PROPOSALS);

        progressBar.progressProperty().bind(browserWebView.getEngine().getLoadWorker().progressProperty());

        /*Load search engine*/
        searchBox.getItems().addAll("Google", "Bing", "Yahoo", "Yandex");
        searchBox.getSelectionModel().select(0);

        /*Setting web history settings*/
        final WebHistory history = browserWebView.getEngine().getHistory();
        history.getEntries().addListener(new ListChangeListener<WebHistory.Entry>() {
            @Override
            public void onChanged(Change<? extends WebHistory.Entry> c) {
                c.next();
                for (WebHistory.Entry e : c.getRemoved()) {
                    comboBoxHistory.getItems().remove(e.getUrl());
                }
                for (WebHistory.Entry e : c.getAddedSubList()) {
                    comboBoxHistory.getItems().add(e.getUrl());
                }
            }
        });

        comboBoxHistory.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int offset = comboBoxHistory.getSelectionModel().getSelectedIndex() - history.getCurrentIndex();
                history.go(offset);
            }
        });

        /*Fill combo box with bookmarks and create behavior*/
        fillComboBoxBookmarks();
        comboBoxBC.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                browserWebView.getEngine().load(comboBoxBC.getSelectionModel().getSelectedItem());
            }
        });

        /*Deleting a bookmark*/
        comboBoxBC.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.DELETE)) {
                String searchString = comboBoxBC.getSelectionModel().getSelectedItem();
                File file = new File("bookmarks.txt");
                try {
                    List<String> lines = FileUtils.readLines(file, "UTF-8");
                    List<String> updatedLines = lines.stream().filter(s -> !s.contains(searchString)).collect(Collectors.toList());
                    FileUtils.writeLines(file, updatedLines, false);
                    comboBoxBC.getItems().remove(searchString);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Information Dialog");
                    alert.setHeaderText(null);
                    alert.setContentText("Bookmark was successfully deleted");
                    alert.showAndWait();
                } catch (IOException ex) {
                    System.out.println("Input output exception");
                }
            }
        });

        /*Load start page*/
        try {
            browserWebView.getEngine().load(getStartPage());
        } catch (IOException e) {
            browserWebView.getEngine().load("https://www.google.com/");
        }
    }

    @FXML
    private void browserBackButtonAction(ActionEvent actionEvent) {
        if (browserWebView.getEngine().getHistory().getCurrentIndex() <= 0) {
            return;
        }
        browserWebView.getEngine().getHistory().go(-1);
    }

    @FXML
    private void browserForwardButtonAction(ActionEvent actionEvent) {
        if ((browserWebView.getEngine().getHistory().getCurrentIndex() + 1) >=
                browserWebView.getEngine().getHistory().getEntries().size()) {
            return;
        }
        browserWebView.getEngine().getHistory().go(1);
    }

    @FXML
    private void browserGoButtonAction(ActionEvent actionEvent) {
        String url = addressBar.getText().trim();
        UrlValidator urlValidator = new UrlValidator();

        if (url.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No url provided");
            return;
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }

        /*If URL is valid - load, no - use search*/
        if (urlValidator.isValid(url)) {
            browserWebView.getEngine().load(url);
        } else {
            browserWebView.getEngine().load(getSearchEngineSearchUrl(
                    searchBox.getSelectionModel().getSelectedItem()) + addressBar.getText());
        }
    }

    @FXML
    private void browserHomeButtonAction(ActionEvent actionEvent) {
        try {
            browserWebView.getEngine().load(getStartPage());
        } catch (IOException e) {
            browserWebView.getEngine().load("https://www.google.com/");
        }
    }

    @FXML
    private void browserReloadingButtonAction(ActionEvent actionEvent) {
        if (browserWebView.getEngine().getLoadWorker().isRunning()) {
            browserWebView.getEngine().getLoadWorker().cancel();
            status.setText("loaded");
            progressIndicator.setVisible(false);
            reloadImageView.setImage(new Image(getClass().getResourceAsStream("/images/reload.png")));
        } else {
            browserWebView.getEngine().reload();
            reloadImageView.setImage(new Image(getClass().getResourceAsStream("/images/stoploading.png")));
        }
    }

    /*Load chosen page to startPage.prop*/
    @FXML
    private void setStartPage() throws IOException {
        Properties properties = new Properties();
        properties.setProperty("startPage", addressBar.getText());
        FileOutputStream out = new FileOutputStream("startPage.prop");
        properties.store(out, "Start page");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText(null);
        alert.setContentText("Started page has been successfully replaced");
        alert.showAndWait();

    }

    /*Load chosen page to bookmarks.txt*/
    @FXML
    private void addBookmark() throws IOException {
        FileWriter writer = new FileWriter("bookmarks.txt", true);
        writer.write(addressBar.getText() + "\n");
        writer.close();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText(null);
        alert.setContentText("Bookmark is successfully added");
        alert.showAndWait();
    }

    /*Return chosen search engine from combo box*/
    private String getSearchEngineSearchUrl(String searchEngine) {
        switch (searchEngine) {
            case "Yandex":
                return "https://yandex.by/search/?text=";
            case "Google":
                return "https://www.google.com/search?q=";
            case "Bing":
                return "http://www.bing.com/search?q=";
            default: //then yahoo
                return "https://search.yahoo.com/search?p=";
        }
    }

    /*Load start page from startPage.prop*/
    private String getStartPage() throws IOException {
        FileInputStream fis = new FileInputStream("startPage.prop");
        Properties properties = new Properties();
        properties.load(fis);
        return properties.getProperty("startPage") == null ? "https://www.google.com/" :
                properties.getProperty("startPage");
    }

    /*Load bookmarks from bookmarks.txt to combo box*/
    private void fillComboBoxBookmarks() {
        File file = new File("bookmarks.txt");
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            System.out.println("File with bookmarks wasn't found");
        }
        while (scanner.hasNextLine()) {
            comboBoxBC.getItems().add(scanner.nextLine());
        }
        scanner.close();
    }

    /*Fill a TreeSet with URL from listsites.txt*/
    private void loadListSites() {
        File file = new File("listsites.txt");
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            System.out.println("File with sites wasn't found");
        }
        while (scanner.hasNextLine()) {
            this.WEBSITE_PROPOSALS.add(scanner.nextLine());
        }
        scanner.close();
    }

    @FXML
    private void showProcessInfo(ActionEvent actionEvent) {
        nativeCalls.showProcessInfo();
    }

    @FXML
    private void showSystemTime(ActionEvent actionEvent) {
        nativeCalls.showLocalTime();
    }

    @FXML
    private void showSystemInfo(ActionEvent actionEvent) {
        nativeCalls.showSystemInfo();
    }

    @FXML
    private void closeWindow(ActionEvent actionEvent) {
        nativeCalls.closeWindow();
    }

    @FXML
    private void minimizeWindow(ActionEvent actionEvent) {
        nativeCalls.minimizeWindow();
    }

    @FXML
    private void lockWorkstation(ActionEvent actionEvent) {
        nativeCalls.lockWorkstation();
    }

    @FXML
    private void makeScreenshot() {
        nativeCalls.makeScreenshoot();
    }
}
