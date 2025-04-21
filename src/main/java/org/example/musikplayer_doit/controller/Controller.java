//todo: Button für Sleep Timer: Mit oder ohne Volumeverringerung über Zeit.
//todo: drag & drop songs

//todo: public private nochmal genau anschauen, was und warum setzt man sie?
// falls mit Datenbank, Dump im Zip mitschicken.
//debugging thread: Thread.currentThread().getName()
// oder Platform.isFxApplicationThread()

// - - - - - - - - - - - - - - - - - - - - - Controller Aufbau: - - - - - - - - - - - - - - - - - - - - - - - - -
// - - - - - - - - - - - - - - - - - - - - - initialize Start - - - - - - - - - - - - - - - - - - - - - - - - -
// - - - - - - - - - - - - - - - - - - - - - - Initialize Ende - - - - - - - - - - - - - - - - - - - - - -
// - - - - - - - - - - - - - - - - - - - - - - TableView Start - - - - - - - - - - - - - - - - - - - - - -
// - - - - - - - - - - - - - - - - - - - - - - TableView Ende - - - - - - - - - - - - - - - - - - - - - -
// - - - - - - - - - - - - - - - - - - - - - Player Behavior Start - - - - - - - - - - - - - - - - - - - - - - - - -
// - - - - - - - - - - - - - - - - - - - - - Player Behavior Ende - - - - - - - - - - - - - - - - - - - - - - - - -
// - - - - - - - - - - - - - - - - - - - - - Playback Start - - - - - - - - - - - - - - - - - - - - - - - - -
// - - - - - - - - - - - - - - - - - - - - - Playback Ende - - - - - - - - - - - - - - - - - - - - - - - - -

package org.example.musikplayer_doit.controller;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Callback;
import javafx.util.Duration;
import org.example.musikplayer_doit.model.MP3FileMetadataExtractor;
import org.example.musikplayer_doit.model.Playlist;
import org.example.musikplayer_doit.model.Song;
import org.w3c.dom.ls.LSOutput;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class Controller {

    @FXML
    TreeView<File> folderTreeView;
    @FXML
    TableView<Song> centerTableView;
    @FXML
    TableView<Song> playingTableView;

    @FXML
    TableColumn<Song, String> albumColumn;
    @FXML
    TableColumn<Song, String> titleColumn; //TableColumn<S, T> S=Datentyp von TableView, T=Datentyp in Spalte
    @FXML
    TableColumn<Song, String> pathColumn;
    @FXML
    TableColumn<Song, String> artistColumn;

    @FXML
    TableColumn<Song, String> lengthColumn;
    @FXML
    TableColumn<Song, String> queueTitleColumn;
    @FXML
    TableColumn<Song, String> queueLengthColumn;
    @FXML
    ProgressBar progressBar;
    @FXML
    Label progressBarLabel;

    @FXML
    Label barTimer;
    @FXML
    Slider volumeSlider;
    @FXML
    Button playButton;
    @FXML
    Label playButtonLabel;
    @FXML
    BorderPane borderPane;
    @FXML
    ToggleButton autoPlayButton;
    @FXML
    ToggleButton repeatQueueButton;

    private Node previousFocus;
    private int loopCount;
    private long startTime;
    private int countingIndex;
    private Song selectedSong;
    private Song currentSong;
    private boolean autoPlay = true;
    private boolean repeatQueue;

    private MediaPlayer player;
    Playlist queue;
    MP3FileMetadataExtractor mp3FileMetadataExtractor;

    public ObservableList<Song> centerList = FXCollections.observableArrayList();
    private ObservableList<Song> currentQueue;


    //@FXML
    //private ToggleButton repeatButton;
    //
    //private enum RepeatState {
    //    UNCLICKED, REPEAT_QUEUE, REPEAT_SINGLE
    //}
    //
    //private RepeatState currentState = RepeatState.UNCLICKED;
    //
    //@FXML
    //private void initialze() {
    //    repeatButton.setOnAction(event -> toggleRepeatState());
    //    updateButtonAppearance(); // Initiales Aussehen setzen
    //}
    //
    //private void toggleRepeatState() {
    //    // Zustand wechseln
    //    switch (currentState) {
    //        case UNCLICKED -> currentState = RepeatState.REPEAT_QUEUE;
    //        case REPEAT_QUEUE -> currentState = RepeatState.REPEAT_SINGLE;
    //        case REPEAT_SINGLE -> currentState = RepeatState.UNCLICKED;
    //    }
    //    updateButtonAppearance();
    //}
    //
    //private void updateButtonAppearance() {
    //    // Aussehen des Buttons basierend auf dem Zustand ändern
    //    switch (currentState) {
    //        case UNCLICKED -> {
    //            repeatButton.setText("Off");
    //            repeatButton.setStyle("-fx-background-color: lightgray;");
    //        }
    //        case REPEAT_QUEUE -> {
    //            repeatButton.setText("Repeat Queue");
    //            repeatButton.setStyle("-fx-background-color: lightblue;");
    //        }
    //        case REPEAT_SINGLE -> {
    //            repeatButton.setText("Repeat Single");
    //            repeatButton.setStyle("-fx-background-color: lightgreen;");
    //        }
    //    }
    //}

    // - - - - - - - - - - - - - - - - - - - - - initialize Start - - - - - - - - - - - - - - - - - - - - - - - - -

    public void initialize() {
        initializeTreeView();
        applyCellFactory();
        trackBorderPaneFocus();
        centerTableViewClearHandler();
        playingTableViewClearHandler();
        initializeVolumeSlider();
        handleVolumeSlider();
        initializeCenterTableViewListener();
        initializePlayingTableViewListener();
        initializeButtonListener();

        centerTableView.requestFocus();

        // Initialize Queue
        queue = new Playlist();

        //initializeTableHeaders();
        //private void initializeTableHeaders(){}
        queueTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        queueLengthColumn.setCellValueFactory(new PropertyValueFactory<>("length"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
        albumColumn.setCellValueFactory(new PropertyValueFactory<>("album"));
        lengthColumn.setCellValueFactory(new PropertyValueFactory<>("length"));
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
    }

    private void initializeTreeView() {
        //folderTreeView.setMouseTransparent(true);
        //folderTreeView.setOpacity(0.5);
        File rootFile = new File("My Computer");
        TreeItem<File> rootItem = new TreeItem<>(rootFile);
        //TreeItem<File> rootItem = new TreeItem<>(null);
        //rootItem.setValue(new File("My Computer"));
        folderTreeView.setRoot(rootItem);

        //rootItem.setExpanded(true);
        //folderTreeView.setShowRoot(true);

        File[] drives = File.listRoots();

        for (var d : drives) {
            TreeItem<File> driveItem = new TreeItem<>(d);
            rootItem.getChildren().add(driveItem);
            driveItem.setValue(new File(d.getAbsolutePath()));

            File[] files = d.listFiles();

            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    for (var f : files) {
                        if(f.isDirectory()){
                            startTime = System.currentTimeMillis();
                            createTree(f, driveItem, 0, 10);
                        }
                    }
                    return null;
                }
            };
            task.setOnSucceeded(event -> System.out.println("Task abgeschlossen: Alle Verzeichnisse gescannt.\nDauer des Scans: "+(int)(System.currentTimeMillis()-startTime)+" Sekunden."));
            Thread thread = new Thread(task);
            thread.setDaemon(true); // Beendet den Thread, wenn die Anwendung geschlossen wird
            thread.start();
        }
    }

    private void createTree(File file, TreeItem<File> parentItem, int depth, int maxDepth) {
        try {
            File[] files = file.listFiles();

            if (files == null) {
                System.out.println("No files found in directory"+file+", returning...");
                return;
            }
            if (depth > maxDepth){
                System.out.println("Endlosschleife oder zu tiefe Ordnerverzweigung: Maximale Rekursionstiefe erreicht.");
                return;
            }

            for (var f : files) {
                if (f.isDirectory()) {
                    if (containsMP3Files(f)) {
                        TreeItem<File> treeItem = new TreeItem<>(f);
                        parentItem.getChildren().add(treeItem);
                        createTree(f, treeItem, depth + 1, maxDepth);
                        loopCount++;
                        System.out.println("Scanned: " + loopCount + " of x");
                    }
                }
            }

        } catch (SecurityException e) {
            System.err.println("Cannot read File: " + e.getMessage());
        }
    }

    private boolean containsMP3Files(File directory) {
        if (directory.isDirectory()) {
            File[] checkMP3File = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));
            //File[] files = folder.listFiles(new java.io.FilenameFilter() {
            //    @Override
            //    public boolean accept(File _file, String str) {
            //        return str.toLowerCase().endsWith(".mp3");
            //    }
            //});
            return checkMP3File != null && checkMP3File.length > 0;
        }
        return false;
    }

    //Cellfactory customizes rendering of cells in a ListView, TableView, TreeView
    private void applyCellFactory() {
        folderTreeView.setCellFactory(new Callback<TreeView<File>, TreeCell<File>>() {
            @Override
            public TreeCell<File> call(TreeView<File> fileTreeView) {
                return new TreeCell<>() {
                    @Override
                    protected void updateItem(File item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            setText(item.getName());
                        }
                        if (item != null) {
                            if (item.getName().isEmpty()) {
                                setText(item.getAbsolutePath());
                                System.err.println("Setting path instead of name for " + item);
                            }
                        }
                        if (item == null && empty) {
                            setText("");
                            setGraphic(null);
                        }
                    }
                };
            }
        });
    }

    //Fokus auf Auswahl behalten (insb. nach Klick auf VolumeSlider)
    private void trackBorderPaneFocus() {
        borderPane.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.focusOwnerProperty().addListener((obs, oldFocus, newFocus) -> {
                    if (newFocus != volumeSlider) { // Speichere den Fokus, wenn es nicht der Slider ist
                        previousFocus = newFocus;
                    }
                });
            }
        });
    }

    private void centerTableViewClearHandler() {
        centerTableView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Node clickedNode = event.getPickResult().getIntersectedNode();
                System.out.println(">c.TableV.ClearH,> Clicked node: " + clickedNode);

                // Traverse the node hierarchy to find the TableRow
                while (clickedNode != null && !(clickedNode instanceof TableRow)) {
                    clickedNode = clickedNode.getParent();
                }

                if (clickedNode instanceof TableRow) {
                    TableRow<?> row = (TableRow<?>) clickedNode;
                    Object rowItem = row.getItem();

                    if (rowItem instanceof Song) {
                        Song clickedSong = (Song) rowItem;
                        System.out.println("ceTaViClHa: A valid Song was clicked: " + clickedSong.getTitle());
                    } else {
                        System.out.println("ceTaViClHa: Clearing selectioN: No valid Song object in the clicked row.");
                        centerTableView.getSelectionModel().clearSelection();
                    }
                }
            }
        });
    }

    private void playingTableViewClearHandler() {
        playingTableView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Node clickedNode = event.getPickResult().getIntersectedNode();

                System.out.println(">p.TableView Clicked node: " + clickedNode);
                while (clickedNode != null && !(clickedNode instanceof TableRow)) {
                    clickedNode = clickedNode.getParent();
                }

                if (clickedNode instanceof TableRow) {
                    TableRow<?> row = (TableRow<?>) clickedNode;
                    Object rowItem = row.getItem();

                    if (rowItem instanceof Song) {
                        Song clickedSong = (Song) rowItem;
                        System.out.println("A Song was clicked: " + clickedSong.getTitle());
                    } else {
                        System.out.println("No valid Song object in the clicked row.");
                        playingTableView.getSelectionModel().clearSelection();
                    }
                }
            }
        });
    }

    private void initializeVolumeSlider() {
        volumeSlider.setMin(0);
        volumeSlider.setMax(1);
        volumeSlider.setValue(0.2);
    }

    //todo: Volume Slider stylen ein Stück weit wie ProgressBar.
    @FXML
    private void handleVolumeSlider() {
        //volumeSlider.minProperty().setValue(0);
        //volumeSlider.maxProperty().setValue(100);

        if (player == null) {
            System.out.println("No player instance found, cannot set volume. Will be updated on playback within 0.05 margin of deviation.");
        }
        volumeSlider.addEventFilter(ScrollEvent.SCROLL, scrollEvent -> {
            double delta = scrollEvent.getDeltaY() > 0 ? 0.05 : -0.05;
//                 Kurzschreibweise für:
//                 double delta = scrollEvent.getDeltaY();
//                 if (delta > 0) {
//                     delta = 0.05;
//                 } else {
//                     delta = -0.05;
//                 }
            double previousVolume = volumeSlider.getValue();
            double newVolume = previousVolume + delta;
            Platform.runLater(() -> {
                volumeSlider.setValue(newVolume);
            });
            System.out.println("New Volume set by mouse wheel to: " + newVolume);

        });

        volumeSlider.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            if (previousFocus != null) {
                previousFocus.requestFocus(); // Fokus zurücksetzen
            }

        });

        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldNunmber, Number newNumber) {
                System.out.println("initial volume level: " + volumeSlider.getValue());
                double volumeSet = newNumber.doubleValue();


                if (player != null) {
                    System.out.println("player volume: " + player.getVolume());
                    System.out.println("set volume level to: " + volumeSet);
                    Platform.runLater(() -> {
                        player.setVolume(volumeSet);
                    });
                }
            }
        });
    }

    private void initializeCenterTableViewListener() {
        //----------MouseEvents----------
        centerTableView.setOnMouseClicked(event -> {
//optional click delay:
//            PauseTransition clickDelay = new PauseTransition(Duration.seconds(0.5));
//            clickDelay.setOnFinished(event -> {
//                // code
//            });
//finally add check if (!clickDelay.getStatus().equals(PauseTransition.Status.RUNNING)) {
            Node clickedNode = event.getPickResult().getIntersectedNode();

            while (clickedNode != null && !(clickedNode instanceof TableRow)) {
                clickedNode = clickedNode.getParent();
                System.out.println("Each clicked Node (prevent play if no song was clicked): " + clickedNode);
            }

            if (clickedNode instanceof TableRow<?> row) {
                System.out.println("Clicked Node (prevent play if no song was clicked): " + clickedNode);
                Object item = row.getItem();

                if (item instanceof Song) {
                    if (event.isAltDown() && event.getClickCount() == 2) {
                        singlePlay();
                    } else if (event.isControlDown() && event.getClickCount() == 2) {
                        addToQueue();
                    } else if (event.getClickCount() == 2) {
                        handleEnterOrDoubleClickCenterTableView();
                    }
                }
            }
        });

        //----------KeyEvents----------
        centerTableView.setOnKeyPressed(keyEvent -> {

            if (centerTableView.getSelectionModel().getSelectedItem() == null) {
                System.out.println(">centerTableView: no song selected, exiting Eventhandler...");
                return;
            }
            if (keyEvent.isAltDown()) {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    selectedSong = centerTableView.getSelectionModel().getSelectedItem();
                    singlePlay();
                }
            } else if (keyEvent.isControlDown()) {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    selectedSong = centerTableView.getSelectionModel().getSelectedItem();
                    addToQueue();
                }
            } else {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    selectedSong = centerTableView.getSelectionModel().getSelectedItem();
                    handleEnterOrDoubleClickCenterTableView();
                }
            }

            if (keyEvent.getCode() == KeyCode.SPACE) {
                if (player != null) {
                    if (player.getStatus() == MediaPlayer.Status.PLAYING) {
                        System.out.println("(Spacebar) Playback paused from " + player.getStatus());
                        player.pause();
                    }

                    if (player.getStatus() == MediaPlayer.Status.PAUSED || player.getStatus() == MediaPlayer.Status.HALTED || player.getStatus() == MediaPlayer.Status.STOPPED) {
                        System.out.println("(Spacebar) Playback resumed from " + player.getStatus());
                        player.play();
                    }
                }
            }
        });
    }

    private void initializePlayingTableViewListener() {
        playingTableView.setOnMouseClicked(event -> {
            Node clickedNode = event.getPickResult().getIntersectedNode();


            while (clickedNode != null && !(clickedNode instanceof TableRow)) {
                clickedNode = clickedNode.getParent();
                System.out.println(">playingTableView: Clicked node: " + clickedNode);
            }
            if (clickedNode == null) {
                System.err.println("playingTableView header clicked, returning...");
                return;
            }

            if (playingTableView.getSelectionModel().getSelectedItem() == null) {
                System.out.println(">playingTableView: No song selected, exiting Eventhandler...");
                return;
            }
            if (event.getClickCount() == 2) {
                selectedSong = playingTableView.getSelectionModel().getSelectedItem();
                handleEnterOrDoubleClickPlayingTableView();
            }
        });

        playingTableView.setOnKeyPressed(keyEvent -> {
            if (playingTableView.getSelectionModel().getSelectedItem() == null) {
                System.out.println(">playingTableView: No song selected, exiting Eventhandler...");
                return;
            }
            if (keyEvent.getCode() == KeyCode.ENTER) {
                selectedSong = playingTableView.getSelectionModel().getSelectedItem();
                handleEnterOrDoubleClickPlayingTableView();
            } else if (keyEvent.getCode() == KeyCode.SPACE) {
                if (player != null) {
                    if (player.getStatus() == MediaPlayer.Status.PLAYING) {
                        System.out.println("(Spacebar) Playback paused from " + player.getStatus());
                        player.pause();
                    }

                    if (player.getStatus() == MediaPlayer.Status.PAUSED || player.getStatus() == MediaPlayer.Status.HALTED || player.getStatus() == MediaPlayer.Status.STOPPED) {
                        System.out.println("(Spacebar) Playback resumed from " + player.getStatus());
                        player.play();
                    }
                }
            } else if (keyEvent.getCode() == KeyCode.DELETE) {
                deleteSongFromQueue();
            }
        });
    }

    private void initializeButtonListener() {
        autoPlayButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                if (newValue) { // Button ist gedrückt
                    autoPlay = false;
                    System.out.println("Autoplay button pressed: " + autoPlay);
                } else { // Button ist nicht gedrückt
                    autoPlay = true;
                    System.out.println("Autoplay button released: " + autoPlay);
                }
            }
        });
        repeatQueueButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                if (newValue) { // Button ist gedrückt
                    repeatQueue = true;
                    System.out.println("Repeating Queue: " + repeatQueue);
                } else { // Button ist nicht gedrückt
                    repeatQueue = false;
                    System.out.println("Repeating Queue: " + repeatQueue);
                }
            }
        });
    }

// - - - - - - - - - - - - - - - - - - - - - - Initialize Ende - - - - - - - - - - - - - - - - - - - - - -

// - - - - - - - - - - - - - - - - - - - - - - TableView Start - - - - - - - - - - - - - - - - - - - - - -
    //Select TreeView Item Logic
        @FXML
        private void selectTreeItem(MouseEvent event) {

        //Determine if the click was on a TreeCell
        Node clickedNode = event.getPickResult().getIntersectedNode();
        while (clickedNode != null && !(clickedNode instanceof TreeCell<?>)) {
            clickedNode = clickedNode.getParent();
        }

        TreeItem<File> selectedItem = folderTreeView.getSelectionModel().getSelectedItem();
        centerList.clear();

        if (selectedItem != null) {
            File folder = selectedItem.getValue();
            System.out.println("Selected: " + folder);

            if (folder.isDirectory()) {
                File[] files = folder.listFiles((_file, str) -> str.toLowerCase().endsWith(".mp3"));
                //File[] files = folder.listFiles(new java.io.FilenameFilter() {
                //    @Override
                //    public boolean accept(File _file, String str) {
                //        return str.toLowerCase().endsWith(".mp3");
                //    }
                //});
                mp3FileMetadataExtractor = new MP3FileMetadataExtractor();
                if (files != null) {
                    centerList.clear();
                    Song[] songArray = new Song[files.length];
                    for (int i = 0; i < files.length; i++) {
                        songArray[i] = new Song(files[i].getAbsolutePath(), new HashMap<>());
                        System.out.println("Absolute Path used in Song construction=" + files[i].getAbsolutePath() + ", along with empty HashMap, to be filled later");
                    }
                    mp3FileMetadataExtractor.extractTagFromMp3(songArray);

                    for (var s : songArray) {
                        centerList.add(s);
                        System.out.println("Added songs to centerList: " + s.getTitle());
                    }
                }
            } else {
                System.out.println("Not a directory");
            }
        } else {
            System.out.println("Invalid selection");
        }

        // Update TableView with Song list.
        centerTableView.setItems(centerList);
        //System.out.println("Added to TableView centerList: "+centerList);;
    }

    private void addToQueue() {
        selectedSong = centerTableView.getSelectionModel().getSelectedItem();
        queue.addSong(selectedSong);
        System.out.println("Song successfully added to queue.");
    }

    private void deleteSongFromQueue() {
        int selectedSongIndex = currentQueue.indexOf(selectedSong);

        if (selectedSongIndex < currentQueue.indexOf(currentQueue.getLast())) {
            countingIndex -= 1;
            System.out.println("countingIndex = " + countingIndex);
        } else {
            countingIndex = currentQueue.indexOf(currentQueue.getLast());
            System.out.println("countingIndex set to last and only song in queue " + countingIndex);
        }

        if (selectedSong == currentSong) {
            System.err.println("Warning: currentSong deleted, initializing countingIndex variable. countingIndex == " + countingIndex);
        }

        queue.removeSong(selectedSong);
        System.out.println("Song successfully deleted from queue, countingIndex = " + countingIndex);
    }

// - - - - - - - - - - - - - - - - - - - - - - TableView Ende - - - - - - - - - - - - - - - - - - - - - -

// - - - - - - - - - - - - - - - - - - - - - Player Behavior Start - - - - - - - - - - - - - - - - - - - - - - - - -

    private void initiatePlay() {
        System.out.println("[[initiatePlay]]: Method called.");
        currentQueue = queue.getQueue();
        setCurrentSong();
        setProgressBar();
        player.setVolume(volumeSlider.getValue());
        transformPlayButton();
        setProgressBarLabel();
        styleCurrentSong();
        playerBehavior();
    }

    //etwas veraltet und inkonsistent in der Anwendung
    private void setCurrentSong() {
        System.out.println(">setCurrentSong: Method called");
        if (player == null || player.getMedia() == null) {
            System.err.println("[[setCurrentSong]]: Kein Player oder keine Media-Quelle gefunden.");
            currentSong = null;
            return;
        }

        if (currentSong != selectedSong) {
            currentSong = currentQueue.get(countingIndex);
        }

        System.out.println("[[setCurrentSong]]: currentSong erfolgreich gesetzt: " + currentSong.getTitle());

        if (currentSong == null) {
            System.err.println("Unable to set currentSong: " + currentSong);
        }
    }

    //todo: add time remaining on right-hand label
    private void setProgressBar() {
        player.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            double offset = 0.03; // 3%
            double currentTime = newValue.toSeconds();
            double totalDuration = player.getTotalDuration().toSeconds();

            //double progress = currentTime / totalDuration;
            double progress = (currentTime / totalDuration) * (1 - offset) + offset;

            progressBar.setProgress(progress);

//hours erstellt 1 3600stel, zählt im Laufe einer Stunde hoch bis 3600
//minutes und seconds arbeiten mit Modulo, um Breakpoints zu gewährleisten.
// Bei genau 60 Sekunden stehen seconds auf 0 und es ist stattdessen 1 minute.
// Ebenso ist es bei genau 3600 Sekunden (60 Minuten) eine Stunde, und Minuten stehen auf 0
            int hours = (int) currentTime / 3600;
            int minutes = (int) (currentTime % 3600) / 60;
            int seconds = (int) currentTime % 60;

//String.format method: % specifies start of format; d decimal integer.
//02: 0 means to pad with leading zeroes if necessary. 2 is the width of the number
            String timeString = (hours > 0)
                    ? String.format("%d:%02d:%02d", hours, minutes, seconds)
                    : String.format("%02d:%02d", minutes, seconds);
            Platform.runLater(() -> {
                barTimer.setText(timeString);
            });
        });
    }

    //morph play symbol into pause symbol during playback
    private void transformPlayButton() {
        if (player == null) {
            return;
        }

        player.statusProperty().addListener((observable, oldStatus, newStatus) -> {
            if (newStatus == MediaPlayer.Status.PLAYING) {
                playButtonLabel.setText("⏸"); // \u23F8
            }
            if (newStatus == MediaPlayer.Status.STOPPED || newStatus == MediaPlayer.Status.PAUSED) {
                playButtonLabel.setText("▶"); // \u25B6
            }
        });
    }

    private void setProgressBarLabel() {
        progressBarLabel.setText(currentQueue.get(countingIndex).getTitle());
    }

    private void styleCurrentSong() {
        Song styleSong = currentQueue.get(countingIndex);
        centerTableView.setRowFactory(tview -> new TableRow<>() {
            @Override
            protected void updateItem(Song item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (item.equals(styleSong)) {
                    setStyle("-fx-font-weight: bold;");
                } else {
                    setStyle("");
                }
            }
        });

        playingTableView.setRowFactory(tview -> new TableRow<>() {
            @Override
            protected void updateItem(Song item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (item.equals(styleSong)) {
                    setStyle("-fx-font-weight: bold;");
                } else {
                    setStyle("");
                }
                //...
            }
        });


        //item und empty werden von JavaFX vorgegeben
        //setRowFactory nimmt Callback an, welcher eine TableRow erstellt.
        // -> TableView Eingabe, TableRow Ausgabe => new TableRow.
        // heißt: tview -> (wird zu) new TableRow<>() { ... (Körperinhalt der anonymen Klasse = was überschrieben wird.

        //Mit anderen Worten: Man möchte sich die Methode updateItem zunutze machen, um die Zelle mit neuen Eigenschaften zu aktualisieren.
        // Damit möchte man aber nicht alle Basiseigenschaften überschreiben, sondern nur die einzelnen Aspekte, die man im Körper der
        // neuen anonymen Klasse definiert. Zum Beispiel hier möchte man nur den Style setzen, und den Rest der Eigenschaften unangetastet
        // lassen. Darum wird die nächste valide Implementierung der Methode in einer Elternklasse aufgerufen, um diese Kernfunktionen zu
        // gewährleisten.

    }

    private void playerBehavior() {
        if (player == null) {
            System.out.println(">playerBehavior: No player instance found, returning.");
            return;
        }

        player.setOnEndOfMedia(() -> {
            System.out.println("endOfMedia triggered by method playerBehavior();");
            progressBar.setProgress(0);
            playNextOrStop();
        });
    }

// - - - - - - - - - - - - - - - - - - - - - Player Behavior Ende - - - - - - - - - - - - - - - - - - - - - - - - -

// - - - - - - - - - - - - - - - - - - - - - Playback Start - - - - - - - - - - - - - - - - - - - - - - - - -

    @FXML
    private void clickPlay() {
        if (queue == null) {
            System.out.println("No song in Playlist.");
            return;
        }
        if (player == null) {
            System.out.println("No player found.");
            return;
        }
        if (player.getStatus() == MediaPlayer.Status.PLAYING) {
            player.pause();
            System.out.println("Paused playback of " + currentSong);
        } else {
            player.play();
            System.out.println("Playing: " + currentSong);
        }
    }

    @FXML
    private void clickStop() {
        if (player != null) {
            player.stop();
            System.out.println("Stopped playback");
        }
    }

    @FXML
    private void clickNext() {
        playNextOrStop();
    }

    @FXML
    private void clickPrevious() {
        playPrevious();
    }

    private void playNextOrStop() {
        System.out.println("[[playNextOrStop]]: Method call: playNextOrStop.");
        if (player == null) {
            System.out.println("[[playNextOrStop]]: No player found.");
            return;
        }
        if (!repeatQueue || currentQueue == null) {
            player.stop();
        }

        int nextSongIndex;
        Song firstSong = currentQueue.getFirst();
        Media firstSongMedia = songToMedia(firstSong);
        if (queue.isLastIndex(countingIndex)) {
            if (autoPlay && repeatQueue) {
                player.stop();
                player.dispose();
                countingIndex = currentQueue.indexOf(firstSong);
                player = new MediaPlayer(firstSongMedia);
                initiatePlay();
                player.play();
                System.out.println("End of queue reached, playing from the beginning.");
            }
            if (autoPlay && !repeatQueue) {
                player.stop();
                System.out.println("End of queue reached, stopping media.");
            }
            if (!autoPlay && !repeatQueue) {
                player.stop();
                System.out.println("(Incidentally) End of queue reached, stopping media.");
            }
            if (!autoPlay && repeatQueue) {
                player.stop();
                player.dispose();
                countingIndex = currentQueue.indexOf(firstSong);
                player = new MediaPlayer(firstSongMedia);
                System.out.println("End of queue reached, initializing next media from the beginning.");
                initiatePlay();
                player.play();
                player.stop();
            }
        } else {
            nextSongIndex = queue.getNext(countingIndex);
            Media nextSongMedia = songToMedia(currentQueue.get(nextSongIndex));
            if (autoPlay) {
                player.stop();
                player.dispose();
                countingIndex = nextSongIndex;
                player = new MediaPlayer(nextSongMedia);
                initiatePlay();
                player.play();
                System.out.println("End of Media: Playing next Song in queue");
            }
            if (!autoPlay) {
                player.stop();
                player.dispose();
                countingIndex = nextSongIndex;
                player = new MediaPlayer(nextSongMedia);
                initiatePlay();
                player.play();
                player.stop();
                System.out.println("End of Media: Setting next Song in queue and stopping.");
            }
        }
    }

    private void playPrevious() {
        System.out.println("current queue contains: " + currentQueue);

        if (player == null) {
            System.out.println("No player instance found.");
            return;
        }

        int previousSongIndex = queue.getPreviousIfExists(countingIndex);
        Song previousSong;

        if (previousSongIndex > -1) {
            previousSong = currentQueue.get(previousSongIndex);
            player.stop();
            player.dispose();
            player = new MediaPlayer(songToMedia(previousSong));
            countingIndex -= 1;
            initiatePlay();
            player.play();
            System.out.println("Playing: " + previousSong.getTitle());
        } else {
            player.seek(Duration.ZERO);
            System.err.println("Setting playback to 0.");
        }
    }

    private void playSelection() {
        if (player != null) {
            player.stop();
            player.dispose();
            System.out.println("Player instance detected and disposed, create new instance...");
        }
        if (selectedSong == null) {
            System.err.println("[[playSelection]]: selectedSong is null.");
            return;
        }
        Media newPlayback = songToMedia(selectedSong);

        player = new MediaPlayer(newPlayback);
        currentQueue = queue.getQueue();
        System.out.println("Playing after double click or enter press newPlayBack: " + newPlayback + ", which is the same as selectedSong.getTitle(): " + selectedSong.getTitle());
        currentSong = selectedSong;

        initiatePlay();
        player.play();
    }

    //todo: ProgressBar anklickbar machen und Lied zur Stelle vorspulen
    //todo: kleine Popups bei Mouseover: ProgressBar Minute:Sekunde, über TableColumn gesamter Inhalt, ebenso über TreeItem.

    /// A Scene in JavaFX represents the content of a stage (window). It is a container for all the visual elements

    private void handleEnterOrDoubleClickCenterTableView() {
        selectedSong = centerTableView.getSelectionModel().getSelectedItem();
        if (currentQueue != null) {
            currentQueue.clear();
            System.out.println("currentQueue cleared.");
        }
        currentQueue = FXCollections.observableArrayList(centerTableView.getItems());
        queue.setQueue(currentQueue);
        playingTableView.setItems(currentQueue);
        if (queue.getQueue() != null) {
            for (var song : currentQueue) {
                System.out.println("Added: " + song.getTitle());
            }
        }

        if (player != null) {
            player.stop();
            player.dispose();
        }

        selectedSong = centerTableView.getSelectionModel().getSelectedItem();
        System.out.println("[DoubleClick or Enter PlayingTableView]: Selected Song: " + centerTableView.getSelectionModel().getSelectedItem());
        Media newPlayback = songToMedia(selectedSong);
        System.out.println("[DoubleClick or Enter PlayingTableView]: Assigning new Media to play: " + newPlayback);
        player = new MediaPlayer(newPlayback);
        currentSong = selectedSong;

        if (currentQueue.contains(selectedSong)) {
            countingIndex = currentQueue.indexOf(selectedSong);
            System.out.println("countingIndex: " + countingIndex);
        } else {
            System.err.println("Queue does not contain selectedSong, countingIndex may be desynchronized");
        }
        player.play();
        initiatePlay();

        if (currentQueue.contains(currentSong)) {
            countingIndex = currentQueue.indexOf(currentSong);
        } else {
            System.err.println("Queue does not contain selectedSong, countingIndex may be desynchronized");
        }
    }

    private void handleEnterOrDoubleClickPlayingTableView() {
        if (player != null) {
            player.stop();
            player.dispose();
        }

        selectedSong = playingTableView.getSelectionModel().getSelectedItem();
        System.out.println("[DoubleClick or Enter PlayingTableView]: Selected Song: " + playingTableView.getSelectionModel().getSelectedItem());
        Media newPlayback = songToMedia(selectedSong);
        System.out.println("[DoubleClick or Enter PlayingTableView]: Assigning new Media to play: " + newPlayback);
        player = new MediaPlayer(newPlayback);
        currentSong = selectedSong;

        if (currentQueue.contains(selectedSong)) {
            countingIndex = currentQueue.indexOf(selectedSong);
            System.out.println("countingIndex: " + countingIndex);
        } else {
            System.err.println("Queue does not contain selectedSong, countingIndex may be desynchronized");
        }
        player.play();
        initiatePlay();
    }

    private void singlePlay() {
        if (player != null) {
            player.stop();
            player.dispose();
            System.out.println("Disposed player instance for singlePlayback.");
        }

        selectedSong = centerTableView.getSelectionModel().getSelectedItem();
        queue.clearQueue();
        queue.addSong(selectedSong);
        currentQueue = queue.getQueue();
        playingTableView.setItems(currentQueue);
        Media assignSinglePlay = songToMedia(selectedSong);
        player = new MediaPlayer(assignSinglePlay);
        currentSong = selectedSong;

        if (currentQueue.contains(selectedSong)) {
            countingIndex = currentQueue.indexOf(selectedSong);
            System.out.println("countingIndex:" + countingIndex);
        } else {
            System.err.println("Queue does not contain selectedSong, countingIndex may be desynchronized");
        }

        playSelection();
        initiatePlay();
        //todo: allow multiple selection during ctrl-click, probably redesign method with array for selection
        // außerdem strg+A für alle auswählen
    }

    //todo: idee: während drag auf ProgressBar per Tastendruck (oder drag out of bounds?) Tonspur anzeigen lassen

// - - - - - - - - - - - - - - - - - - - - - Playback Ende - - - - - - - - - - - - - - - - - - - - - - - - -

// - - - - - - - - - - - - - - - - - - - - - Rest - - - - - - - - - - - - - - - - - - - - - - - - -
    //Scheduling: When Platform.runLater is called, it schedules the Runnable to be executed on the JavaFX Application Thread. This is the thread responsible for handling all JavaFX UI updates.
    //Execution: The JavaFX runtime maintains a queue of tasks to be executed on the JavaFX Application Thread. The Runnable provided to Platform.runLater is added to this queue.
    //Thread Safety: By ensuring that the Runnable is executed on the JavaFX Application Thread, Platform.runLater guarantees that any UI updates within the run method are performed in a thread-safe manner, avoiding concurrency issues.


    private void cleanup() {
        loopCount = 0;
        startTime = 0;
    }

    @FXML
    private void handleRefresh() {
        //...
    }

    //Parser-Methode
    private Media songToMedia(Song song) {
        if (song == null) {
            return null;
        }
        return new Media(new File(song.getPath()).toURI().toString());
    }

}