//todo: Button für Sleep Timer: Mit oder ohne Volumeverringerung über Zeit.
//todo: drag & drop songs
//todo: mute togglebutton left of Slider
//todo: public private nochmal genau anschauen, was und warum setzt man sie?
// falls mit Datenbank, Dump im Zip mitschicken.
//note: JFoenixSlider / JFXSlider für .filled-track / .colored-track Eigenschaft
//debugging thread: Thread.currentThread().getName()
//// oder Platform.isFxApplicationThread()

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
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.HashMap;

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
    Label barRemainingTimer;
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

    // - - - - - - - - - - - - - - - - - - - - - initialize Start - - - - - - - - - - - - - - - - - - - - - - - - -

    public void initialize() {
        System.setProperty("javafx.css.debug", "true");
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

        queueTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        queueLengthColumn.setCellValueFactory(new PropertyValueFactory<>("length"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
        albumColumn.setCellValueFactory(new PropertyValueFactory<>("album"));
        lengthColumn.setCellValueFactory(new PropertyValueFactory<>("length"));
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
    }

    private void initializeTreeView() {
        folderTreeView.setMouseTransparent(true);
        folderTreeView.setOpacity(0.5);
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

            //File[] files = d.listFiles();

            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    startTime = System.currentTimeMillis();
                    for (var f : drives) {
                        if (f.isDirectory()) {
                            createTree(f, driveItem, 0, 10);
                        }
                    }
                    return null;
                }
            };
            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    System.out.println("Task abgeschlossen: Alle Verzeichnisse gescannt für Festplatten: " + Arrays.toString(File.listRoots()));
                    folderTreeView.setMouseTransparent(false);
                    folderTreeView.setOpacity(1);
                }
            });
            Thread thread = new Thread(task);
            thread.setDaemon(true); // Beendet den Thread, wenn die Anwendung geschlossen wird
            thread.start();
        }
    }

    //bricht aktuell Scan ab, wenn Überordner keine mp3-Dateien enthält, auch wenn Unterordner welche enthält.


  //  private boolean containsMP3Files(File directory) {
//        if (directory.isDirectory()) {
//            File[] checkMP3File = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));
//            return checkMP3File != null && checkMP3File.length > 0;
//        }
//        return false;
    private boolean containsMP3Files(File directory) {
        if (directory.isDirectory()) {
            File[] checkMP3File = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".mp3");
                }
            });
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
        borderPane.sceneProperty().addListener(new ChangeListener<Scene>() {
            @Override
            public void changed(ObservableValue<? extends Scene> observableValue, Scene oldScene, Scene newScene) {
                if (newScene != null) {
                    newScene.focusOwnerProperty().addListener(new ChangeListener<Node>() {
                        @Override
                        public void changed(ObservableValue<? extends Node> observableValue, Node oldFocus, Node newFocus) {
                            if (newFocus != volumeSlider) { // Speichere den Fokus, wenn es nicht der Slider ist
                                previousFocus = newFocus;
                            }
                        }
                    });
                }
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
            //System.out.println("handleVolumeSlider is JavaFX thread="+Platform.isFxApplicationThread());
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
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    volumeSlider.setValue(newVolume);
                }
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
            public void changed(ObservableValue<? extends Number> observableValue, Number oldNumber, Number newNumber) {
                //System.out.println("handleVolumeSlider is JavaFX thread="+Platform.isFxApplicationThread());
                System.out.println("initial volume level: " + volumeSlider.getValue());
                double volumeSet = newNumber.doubleValue();


                if (player != null) {

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            player.setVolume(volumeSet);
                            System.out.println("player volume: " + player.getVolume());
                            System.out.println("set volume level to: " + volumeSet);
                        }
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

        //todo: nachvollziehen einzige lambda
        playingTableView.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                //xyz
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
//Determine if the click was on a TreeCell

//File[] files = folder.listFiles(new java.io.FilenameFilter() {
//    @Override
//    public boolean accept(File _file, String str) {
//        return str.toLowerCase().endsWith(".mp3");
//    }
//});
    //Ich verstehe. Ich wiederhole dann meine Liste mit dem neuen Verständnis. Lass mich wissen, ob dies in jeder Hinsicht korrekt ist:
//1) You instantiate a Task object. In <> behind Task is the return type specified. If the task has no return, the type is Void.
//2) A return type is important is the process within the task results in some value which is needed outside the task
//3) An implementation of Task also always requires @overriding the call()-method. By default, it does nothing and has no return value.
//4) The implementation of the call method is given the same return type as the initial Task. So Void if it's <void>, ObservableList<file> if it's <ObservableList<file>>.</file></file></void>
//5) After this point, the individual code or logic can be entered which is to be run inside the task (or the call-method, rather)
//6) At the end of the call-method, the specified return type needs to be returned. null if it's void.
//7) Once call is closed, Task is closed as well, the Task itself is fully implemented at this point.
//8) In order to use the return value, there are two requirements:
//1. The task needs to be actually run by creating and initializing a Thread with the task instance. This creates a single thread which runs the code specified in the call() method.
//2. Whatever return value has resulted from the task, should be used within the task.setOnSucceeded(){ - code-block. This ensures that the task has fully loaded and completed its course before the code requiring the return value is executed.
//
//9) If all is correct so far, please help me understand the lambda in the last part of setOnSucceeded. Can you help me by displaying how it looks without Lambda, and show me the differences?
@FXML
private void selectTreeItem(MouseEvent event) {
    TreeItem<File> selectedItem = folderTreeView.getSelectionModel().getSelectedItem();
    centerList.clear();

    if (selectedItem != null) {
        File folder = selectedItem.getValue();
        System.out.println("Selected: " + folder);

        if (folder.isDirectory()) {
            Task<ObservableList<Song>> task = new Task<>() {
                @Override
                protected ObservableList<Song> call() {
                    ObservableList<Song> songs = FXCollections.observableArrayList();
                    File[] files = folder.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return name.toLowerCase().endsWith(".mp3");
                        }
                    });

                    if (files != null) {
                        MP3FileMetadataExtractor extractor = new MP3FileMetadataExtractor();
                        Song[] songArray = new Song[files.length];

                        for (int i = 0; i < files.length; i++) {
                            songArray[i] = new Song(files[i].getAbsolutePath(), new HashMap<>());
                        }
                        extractor.extractTagFromMp3(songArray);

                        for (var s : songArray) {
                            songs.add(s);
                        }
                    }
                    return songs;
                }
            };

            // Führt Aktionen aus, wenn der Task erfolgreich abgeschlossen wurde.
            //"WorkerStateEvents" reagieren auf den Status von Hintergrundprozessen. z.B. SUCCEEDED, FAILED, CANCELLED
            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    // Aktualisiert die `centerList` mit den geladenen Songs.
                    centerList.setAll(task.getValue());
                    // Setzt die aktualisierte Liste in die TableView.
                    centerTableView.setItems(centerList);
                    System.out.println("TableView updated with songs.");
                }
            });

            // registriert fehlerhafte Task-Ausführung.
          task.setOnFailed(new EventHandler<WorkerStateEvent>() {
              @Override
              public void handle(WorkerStateEvent workerStateEvent){
                  System.err.println("Error during background task: " + task.getException().getMessage());
              }
          });

            // Startet den Task in einem separaten Thread.
            Thread thread = new Thread(task);
            thread.setDaemon(true); // Falls Anwendung abgebrochen wird, wird Thread beendet.
            thread.start();
        } else {
            System.out.println("Not a directory");
        }
    } else {
        System.out.println("Invalid selection");
    }
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


    private void setProgressBar() {
        player.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observableValue, Duration oldValue, Duration newValue) {
                //System.out.println("setProgressBar is JavaFX thread="+Platform.isFxApplicationThread());
                //observable = player.currentTimeProperty(). oldValue vor, newValue nach der Änderung = currentTime

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

                int remainingTime = (int) totalDuration - (int) currentTime;
                int hours2 = remainingTime / 3600;
                int minutes2 = (remainingTime % 3600) / 60;
                int seconds2 = remainingTime % 60;

//String.format method: % specifies start of format; d decimal integer.
//02: 0 means to pad with leading zeroes if necessary. 2 is the width of the number
                String timeString = (hours > 0)
                        ? String.format("%d:%02d:%02d", hours, minutes, seconds)
                        : String.format("%02d:%02d", minutes, seconds);
                String timeRemainingString = (hours2 > 0)
                        ? String.format("%d:%02d:%02d", hours2, minutes2, seconds2)
                        : String.format("%02d:%02d", minutes2, seconds2);

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        barTimer.setText(timeString);
                        barRemainingTimer.setText(timeRemainingString);
                    }
                });
            }
        });
    }

    //morph play symbol into pause symbol during playback
    private void transformPlayButton() {
        if (player == null) {
            return;
        }

        player.statusProperty().addListener(new ChangeListener<MediaPlayer.Status>() {
            @Override
            public void changed(ObservableValue<? extends MediaPlayer.Status> observableValue, MediaPlayer.Status oldStatus, MediaPlayer.Status newStatus) {
                if (newStatus == MediaPlayer.Status.PLAYING) {
                        playButtonLabel.setText("⏸"); // \u23F8
                }
                if (newStatus == MediaPlayer.Status.STOPPED || newStatus == MediaPlayer.Status.PAUSED) {
                        playButtonLabel.setText("▶"); // \u25B6
                }
            }
        });
    }

    private void setProgressBarLabel() {
        progressBarLabel.setText(currentQueue.get(countingIndex).getTitle());
    }

    private void styleCurrentSong() {
        Song styleSong = currentQueue.get(countingIndex);
        centerTableView.setRowFactory(new Callback<TableView<Song>, TableRow<Song>>() {
            @Override
            public TableRow<Song> call(TableView<Song> tview) {
                return new TableRow<>() {
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
                };
            }
        });

        playingTableView.setRowFactory(new Callback<TableView<Song>, TableRow<Song>>() {
            @Override
            public TableRow<Song> call(TableView<Song> tview) {
                return new TableRow<>() {
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
                };
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

        player.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                System.out.println("endOfMedia triggered by method playerBehavior();");
                progressBar.setProgress(0);
                playNextOrStop();
            }
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
            System.out.println("Playing previous Index: " + previousSong.getTitle());
        } else {
            player.seek(Duration.ZERO);
            System.err.println("No prior index found, setting playback to 0.");
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

    }






    //Parser-Methode
    private Media songToMedia(Song song) {
        if (song == null) {
            return null;
        }
        return new Media(new File(song.getPath()).toURI().toString());
    }

    private void createTree(File file, TreeItem<File> parentItem, int depth, int maxDepth) {
        if (depth > maxDepth){
            System.out.println("Endlosschleife oder zu tiefe Ordnerverzweigung: Maximale Rekursionstiefe erreicht.");
            return;
        }
        File[] files = file.listFiles();
            for (var f : files) {
                if (containsMP3Files(f)) {
                    TreeItem<File> treeItem = new TreeItem<>(f);
                    parentItem.getChildren().add(treeItem);
                    createTree(f, treeItem, depth + 1, maxDepth);
                    loopCount++;
                    System.out.println("Scanned: " + loopCount);
                }
            }
    }
}




