//Nächste Schritte:
//todo: Controller weiter auslagern,
//todo: restliche TreeView-Methoden checken und verbessern,
//todo: alles von File auf Path umstellen,
//todo: TableView-Methoden ähnlich wie TreeViewBuilder a) auslagern und b) verbessern - es lädt mitunter recht lange,
//todo: Metadaten bearbeitbar machen,
//todo: Datenbankanbindung.
//todo: TableView ObservableLists konsolidieren

//todo: Button für Sleep Timer: Mit oder ohne Volumeverringerung über Zeit.
//todo: drag & drop songs
//todo: mute togglebutton left of Slider
//todo: public private nochmal genau anschauen, was und warum setzt man sie?
//todo: Volume Slider stylen ein Stück weit wie ProgressBar.
//todo: ProgressBar anklickbar machen und Lied zur Stelle vorspulen
//todo: kleine Popups bei Mouseover: ProgressBar Minute:Sekunde, über TableColumn gesamter Inhalt, ebenso über TreeItem.
//todo: allow multiple selection during ctrl-click, probably redesign method with array for selection
//todo: idee: während drag auf ProgressBar per Tastendruck (oder drag out of bounds?) Tonspur anzeigen lassen

package org.example.musikplayer_doit.controller;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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
import org.example.musikplayer_doit.services.MP3FileMetadataExtractor;
import org.example.musikplayer_doit.model.Playlist;
import org.example.musikplayer_doit.model.Song;
import org.example.musikplayer_doit.services.TreeViewBuilder;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;

public class Controller {

    /// Vorsicht mit Updates, irgendeine JavaFX Bibliothek ist veraltet und darf nicht geupdated werden.

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
    TableColumn<Song, String> genreColumn;

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

    //Dient dazu den Fokus von VolumeSlider wegzunehmen und wiederherzustellen
    private Node previousFocus;
    //Als Ausgabe während TreeView-Erstellung
    private int loopCount;
    //Dient Synchronisierung zwischen Player und Liste
    private int countingIndex;
    //Dient unmittelbarer Auswahl von Medien
    private Song selectedSong;
    //Mehr oder weniger veraltet. Restnutzungen können größtenteils durch countingIndex ersetzt werden
    private Song currentSong;
    //Eingestellte Werte durch die ToggleButtons
    private boolean autoPlay = true;
    private boolean repeatQueue;

    private MediaPlayer player;
    Playlist queue;
    MP3FileMetadataExtractor mp3FileMetadataExtractor;

    public ObservableList<Song> centerList = FXCollections.observableArrayList();
    private ObservableList<Song> currentQueue;

    // - - - - - - - - - - - - - - - - - - - - - initialize Start - - - - - - - - - - - - - - - - - - - - - - - - -

    //Initialisiert alle grundlegenden Werte des Programms
    public void initialize() {
        TreeViewBuilder treeViewBuilder = new TreeViewBuilder(folderTreeView);
        treeViewBuilder.initializeTreeView();
        treeViewBuilder.applyCellFactory();
        treeViewBuilder.buildTree(File.listRoots());
        trackBorderPaneFocus();
        centerTableViewClearHandler();
        playingTableViewClearHandler();
        initializeVolumeSlider();
        handleVolumeSlider();
        initializeCenterTableViewListener();
        initializePlayingTableViewListener();
        initializeButtonListener();

        //Vermutlich nicht notwendig
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
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
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
                            if (newFocus != volumeSlider) {
                                previousFocus = newFocus;
                            }
                        }
                    });
                }
            }
        });
    }

    //Leert die aktuelle Auswahl, wenn außerhalb einer Song-Zeile geklickt wid
    private void centerTableViewClearHandler() {
        centerTableView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Node clickedNode = event.getPickResult().getIntersectedNode();
                System.out.println(">c.TableV.ClearH,> Clicked node: " + clickedNode);

                // Traversing node hierarchy to find the TableRow
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

    //Leert die aktuelle Auswahl, wenn außerhalb einer Song-Zeile geklickt wird
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

    //Grundwerte für den Volume Slider
    private void initializeVolumeSlider() {
        volumeSlider.setMin(0);
        volumeSlider.setMax(1);
        volumeSlider.setValue(0.2);
    }

    //Vermittelt zwischen Volume Slider und Player
    @FXML
    private void handleVolumeSlider() {
        //volumeSlider.minProperty().setValue(0);
        //volumeSlider.maxProperty().setValue(100);

        if (player == null) {
            System.out.println("No player instance found, cannot set volume. Will be updated on playback within 0.05 margin of deviation.");
        }
        volumeSlider.addEventFilter(ScrollEvent.SCROLL, scrollEvent -> {
            //System.out.println("handleVolumeSlider is JavaFX thread="+Platform.isFxApplicationThread());
            //Bestimmt Richtung des Volume Sliders basierend auf Mausrad auf/ab
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
            //Steuert von einem Hintergrundthread den JavaFX Main Thread an
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
                    //Steuert von einem Hintergrundthread den JavaFX Main Thread an
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

    //Initialisiert Listener für die TableView
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

    //Initialisiert Listener für die Queue (TableView)
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

    //Initialisiert Listener für die Buttons
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
//Füllt TableView nach Klick auf TreeItem
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
                            mp3FileMetadataExtractor = new MP3FileMetadataExtractor();
                            Song[] songArray = new Song[files.length];

                            for (int i = 0; i < files.length; i++) {
                                songArray[i] = new Song(files[i].getAbsolutePath(), new HashMap<>());
                            }
                            mp3FileMetadataExtractor.extractTagFromMp3(songArray);

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

    //Listen-Methoden
    private void addToQueue() {
        selectedSong = centerTableView.getSelectionModel().getSelectedItem();
        queue.addSong(selectedSong);
        System.out.println("Song successfully added to queue.");
    }

    private void deleteSongFromQueue() {
        selectedSong = playingTableView.getSelectionModel().getSelectedItem();

        if (selectedSong == null){
            System.err.println("delete: No selected song detected");
            return;
        }

        int selectedSongIndex = currentQueue.indexOf(selectedSong);
        queue.removeSong(selectedSong);

        if (countingIndex == selectedSongIndex && countingIndex != 0){
            countingIndex -= 1;
        }
        else if (countingIndex >= currentQueue.size()) {
            countingIndex = currentQueue.size() - 1;
        } else if (selectedSongIndex < countingIndex && countingIndex > 0) {
            countingIndex--;
        }

        System.out.println("Song successfully deleted from queue, countingIndex = " + countingIndex);
    }

// - - - - - - - - - - - - - - - - - - - - - - TableView Ende - - - - - - - - - - - - - - - - - - - - - -

// - - - - - - - - - - - - - - - - - - - - - Player Behavior Start - - - - - - - - - - - - - - - - - - - - - - - - -

    //"Interface"-Methode für Player-Synchronisierung. Muss mit jedem player.play() aufgerufen werden
    private void initiatePlay() {
        System.out.println("[[initiatePlay]]: Method called.");
        System.out.println("currentQueue update: FX thread?"+Platform.isFxApplicationThread());
        currentQueue = queue.getQueue();
        setCurrentSong();
        setProgressBar();
        player.setVolume(volumeSlider.getValue());
        transformPlayButton();
        setProgressBarLabel();
        styleCurrentSong();
        playerBehavior();
    }

    //etwas veraltet und inkonsistent in der Anwendung. Soll Listensynchronisierung dienen
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

    //Reguliert Progress Bar-Fortschritt. In der Zukunft optimierbar mit Hintergrundprozess und Timeline
    private void setProgressBar() {
        player.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observableValue, Duration oldValue, Duration newValue) {
                //System.out.println("setProgressBar is JavaFX thread="+Platform.isFxApplicationThread());

                //Damit dem Nutzer direkt ein Progress angezeigt wird
                double offset = 0.01; // 1%
                double currentTime = newValue.toSeconds();
                double totalDuration = player.getTotalDuration().toSeconds();

                double progress = (currentTime / totalDuration) * (1 - offset) + offset;

                progressBar.setProgress(progress);

//hours erstellt 1 3600stel, zählt im Laufe einer Stunde hoch bis 3600
//minutes und seconds arbeiten mit Modulo, um Breakpoints zu gewährleisten.
// Bei genau 60 Sekunden stehen seconds auf 0 und es ist stattdessen 1 minute.
// Ebenso ist es bei genau 3600 Sekunden (60 Minuten) eine Stunde, und Minuten stehen auf 0
                //Linkes Label für verstrichene Zeit
                int hours = (int) currentTime / 3600;
                int minutes = (int) (currentTime % 3600) / 60;
                int seconds = (int) currentTime % 60;

                //Rechtes Label für verbleibende Zeit
                int remainingTime = (int) totalDuration - (int) currentTime;
                int hours2 = remainingTime / 3600;
                int minutes2 = (remainingTime % 3600) / 60;
                int seconds2 = remainingTime % 60;

//String.format Methode: % gibt den Start eines Formats an; d Dezimalzahl.
//02: 0 füllt eine führende 0 auf falls nötig. 2 bestimmt die Gesamtlänge der Zahl
                String timeString = (hours > 0)
                        ? String.format("%d:%02d:%02d", hours, minutes, seconds)
                        : String.format("%02d:%02d", minutes, seconds);
                String timeRemainingString = (hours2 > 0)
                        ? String.format("%d:%02d:%02d", hours2, minutes2, seconds2)
                        : String.format("%02d:%02d", minutes2, seconds2);

                //Steuert von einem Hintergrundthread den JavaFX Main Thread an
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

    //Lädt aktuellen Song als Text in die ProgressBar
    private void setProgressBarLabel() {
        progressBarLabel.setText(currentQueue.get(countingIndex).getTitle());
    }

    //Druckt den aktuell spielenden Song fett in TableViews
    private void styleCurrentSong() {
        System.out.println("style: counting="+countingIndex);
        Song styleSong = currentQueue.get(countingIndex);
        centerTableView.refresh();
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
                            System.out.println("styleSong: Style current Song bold");
                        } else {
                            setStyle("");
                        }
                    }
                };
            }
        });

        playingTableView.refresh();
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
                            System.out.println("styleSong: Style current Song bold");
                        } else {
                            setStyle("");
                        }
                    }
                };
            }
        });
    }

    //Setzt Default-Verhalten nach Wiedergabeende
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

    //Einfache Play-Methode
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

    //Einfache Stop-Methode
    @FXML
    private void clickStop() {
        if (player != null) {
            player.stop();
            System.out.println("Stopped playback");
        }
    }

    //Methodenerklärung bei der eigentlichen Methide
    @FXML
    private void clickNext() {
        playNextOrStop();
    }

    //Methodenerklärung bei der eigentlichen Methide
    @FXML
    private void clickPrevious() {
        playPrevious();
    }

    //Konditionales Next: Abhängig von Playlist Ende / nicht Ende, RepeatQueue / Autoplay an / aus.
    private void playNextOrStop() {
        System.out.println("[[playNextOrStop]]: Method call: playNextOrStop. CountingIndex:"+countingIndex+"repeat queue true?"+repeatQueue);
        if (player == null) {
            System.out.println("[[playNextOrStop]]: No player found.");
            return;
        }
        if(currentQueue == null){
            player.stop();
            System.out.println("No queue found, stopping player.");
            return;
        }
        if (countingIndex == currentQueue.indexOf(currentQueue.getLast()) && !repeatQueue) {
            player.stop();
            System.out.println("Player stop  end of queue");
            return;
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

    //Konditionales Previous: Abhängig von Playlist Anfang / nicht Anfang.
    private void playPrevious() {
        if (player == null) {
            System.out.println("No player instance found.");
            return;
        }

        int previousSongIndex = queue.getPreviousIfExists(countingIndex);
        Song previousSong;

        if (previousSongIndex != -1) {
            previousSong = currentQueue.get(previousSongIndex);
            player.stop();
            player.dispose();
            player = new MediaPlayer(songToMedia(previousSong));
            countingIndex -= 1;
            player.play();
            initiatePlay();
            System.out.println("Playing previous Index: " + previousSong.getTitle());
        } else {
            player.seek(Duration.ZERO);
            System.err.println("No prior index found, setting playback to 0. Song on 1st index: "+currentQueue.getFirst().getTitle());
        }
    }

    //Methode sollte überholt oder ersetzt werden
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
        System.out.println("currentQueue update: FX thread?"+Platform.isFxApplicationThread());
        currentQueue = queue.getQueue();
        System.out.println("Playing after double click or enter press newPlayBack: " + newPlayback + ", which is the same as selectedSong.getTitle(): " + selectedSong.getTitle());
        currentSong = selectedSong;

        initiatePlay();
        player.play();
    }

    //Listener für Enter und Doppelklick in TableView
    private void handleEnterOrDoubleClickCenterTableView() {
        selectedSong = centerTableView.getSelectionModel().getSelectedItem();
        if (currentQueue != null) {
            currentQueue.clear();
            System.out.println("currentQueue update: FX thread?"+Platform.isFxApplicationThread());
            System.out.println("currentQueue cleared.");
        }
        currentQueue = FXCollections.observableArrayList(centerTableView.getItems());
        System.out.println("currentQueue update: FX thread?"+Platform.isFxApplicationThread());
        queue.setQueue(currentQueue);
        System.out.println("currentQueue update: FX thread?"+Platform.isFxApplicationThread());
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

    //Listener für Enter und Doppelklick in Queue
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

    //Listener für Alt-Enter/Doppelklick
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
        System.out.println("currentQueue update: FX thread?"+Platform.isFxApplicationThread());
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
    }

// - - - - - - - - - - - - - - - - - - - - - Playback Ende - - - - - - - - - - - - - - - - - - - - - - - - -

// - - - - - - - - - - - - - - - - - - - - - Rest - - - - - - - - - - - - - - - - - - - - - - - - -
//Markierung nicht mehr gebrauchter Werte für den Garbage Collector
private void cleanup() {
    loopCount = 0;
}
    //Veraltete Methode
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
}




