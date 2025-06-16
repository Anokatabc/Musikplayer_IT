//done: Titel des aktuellen Liedes in ein Label? über / in dem Wiedergabebalken laden.
//todo: für Abgabe unbedingt mit My Computer machen, sonst wird es nicht bei jedem Tester funktionieren.
//todo: default sortierfunktion nur zwischen a-z und z-a wechseln lassen, keine dritte Stufe.
//done: komisches Verhalten bei Klick auf Spaltennamen in playingTableView aufklären
//public private nochmal genau anschauen, was und warum setzt man sie?
//falls mit Datenbank, Dump im Zip mitschicken.
//todo: interface erstellen? play.play() muss mit initializePlay() verwendet werden, z. B.
//debugging thread: Thread.currentThread().getName()
// oder Platform.isFxApplicationThread()

//done: delete listener -> delete song from queue
//done: implementation jaudiotagger, read metadata
// -store metadata in Map (file? archive?)
// -comprehend TableColumn method - TableRowCellFactory w/e?
// -apply metadata to TableColumns
// -choose a handful of relevant metadata to finish out the base player
// do: File object or Path to song file to read
// AudioFileIO.read(file)
// error handling
// audioFile.getTag().getFirst(FieldKey.TITLE) to get specific fields
// use absolute file path. Alternatively use hash to identify file uniquely
// -refine and enhance implementation: design efficiency and safety, call/update/synchronization

// import org.jaudiotagger.audio.AudioFile;
//import org.jaudiotagger.audio.AudioFileIO;
//import org.jaudiotagger.audio.exceptions.CannotReadException;
//import org.jaudiotagger.audio.exceptions.CannotWriteException;
//import org.jaudiotagger.tag.Tag;
//import org.jaudiotagger.tag.id3.ID3v1Tag;
//import org.jaudiotagger.tag.id3.ID3v24Tag;
//
//import java.io.File;
//

package org.example.musikplayer_doit.controller;
//test 13
// import javafx.application.Platform;

import java.net.URI;

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
import javafx.scene.control.skin.TableHeaderRow;
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

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//todo: user option to set root folder

public class Controller {

    private MediaPlayer player;

    @FXML
    private TreeView<File> folderTreeView;
    @FXML
    private TableView<Song> centerTableView;
    @FXML
    private TableView<Song> playingTableView;

    @FXML
    private TableColumn<Song, String> albumColumn;
    @FXML
    private TableColumn<Song, String> titleColumn; //TableColumn<S, T> S=Datentyp von TableView, T=Datentyp in Spalte
    @FXML
    private TableColumn<Song, String> pathColumn;
    @FXML
    private TableColumn<Song, String> artistColumn;



    @FXML
    private TableColumn<Song, String> lengthColumn;
    @FXML
    private TableColumn<Song, String> queueTitleColumn;
    @FXML
    private TableColumn<Song, String> queueLengthColumn;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label progressBarLabel;
    @FXML
    private TableColumn<Song, String> noColumn;

    @FXML
    private Label barTimer;
    @FXML
    Slider volumeSlider;
    @FXML
    Button playButton;
    @FXML
    Label playButtonLabel;
    @FXML
    BorderPane borderPane;

    private Node previousFocus;
    private int loopCount;
    private long startTime;
    private int countingIndex;
    private Song selectedSong;
    private Song currentSong;
    MP3FileMetadataExtractor mp3FileMetadataExtractor;

    public ObservableList<Song> centerList = FXCollections.observableArrayList();
    Playlist queue;
    private ObservableList<Song> currentQueue;

    @FXML
    ToggleButton autoPlayButton;
    @FXML
    ToggleButton repeatQueueButton;

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
    //private void initialize() {
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

    //default player behavior variables:
    private boolean autoPlay = true;
    private boolean repeatQueue;

//mostly done: initialize() abspecken (auslagern)


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

        //MouseEventService mouseEventService;
        centerTableView.requestFocus();
        //barTimer.setText("00:00");

        // Initialize the Playlist
        queue = new Playlist();


        // Set the items of the playingTableView to the Playlist's queue
        //centerTableViewClickOrKeyPressEventHandler();
        //playingTableViewClickOrKeyPressEventHandler();
        //private void centerTableViewClickOrKeyPressEventHandler();
        //private void playingTableViewClickOrKeyPressEventHandler();


        queueTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        queueLengthColumn.setCellValueFactory(new PropertyValueFactory<>("length"));
        //queueLengthColumn.setCellValueFactory(new PropertyValueFactory<>("songLength"));
//        playingTableView.setOnMouseClicked(event -> {
//            if (event.getClickCount() == 2) {
//                handleDoubleClick(event);
//            }
//        });

//        Platform.runLater(new Runnable() {
//            @Override public void run() {
//                progressBar.setProgress(counter/1000000.0);
//            }
//
//        });

    }


private void initializeButtonListener(){
        //done: still make player skip to next song, then stop. (or regardless of stopping)
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


    // - - - - - - - - - - - - - - - - - - - - - TreeViewController - - - - - - - - - - - - - - - - - - - - - - - - -

    // Multithreading: https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ForkJoinPool.html


    //    // Recursive method to create TreeItems up to given depth
    //    private TreeItem<File> createNode(File file, int currentDepth) {
    //        TreeItem<File> treeItem = new TreeItem<>(file);
    //
    //        if (file.isDirectory() && currentDepth < MAX_DEPTH) {
    //            File[] files = file.listFiles(pathname -> pathname.isDirectory());
    //            if (files != null) {
    //                for (File child : files) {
    //                    treeItem.getChildren().add(createNode(child, currentDepth + 1));
    //                }
    //            }
    //        }
    //        return treeItem;
    //    }

    //💡 Optional: Lazy Loading (Performance Tip)
    //To avoid long loading times and memory usage, load child folders only when a node is expanded:
    //
    //
    //treeItem.addEventHandler(TreeItem.branchExpandedEvent(), event -> {
    //    TreeItem<File> item = event.getSource();
    //    if (item.getChildren().isEmpty()) {
    //        loadChildren(item, currentDepth + 1);
    //    }
    //});

    //Lazy Loading (Optimization - optional but highly recommended): Instead of loading the entire file system at once, implement lazy loading to improve performance and responsiveness:
    //Initially, only load the immediate children (folders) of a folder when the user expands its TreeItem in the TreeView.
    //Use a mechanism (e.g., a boolean flag on the TreeItem) to indicate whether the children of a TreeItem have already been loaded.
    //When the user expands a TreeItem:
    //Check the "loaded" flag.
    //If not loaded, call the recursive function to scan subfolders up to the maxDepth. This part is the same as described above, but only triggered on demand.
    //Set the "loaded" flag to true.

            //Lazy Loading (Optimization - optional but highly recommended): Instead of loading the entire file system at once, implement lazy loading to improve performance and responsiveness:
    //Initially, only load the immediate children (folders) of a folder when the user expands its TreeItem in the TreeView.
    //Use a mechanism (e.g., a boolean flag on the TreeItem) to indicate whether the children of a TreeItem have already been loaded.
    //When the user expands a TreeItem:
    //Check the "loaded" flag.
    //If not loaded, call the recursive function to scan subfolders up to the maxDepth. This part is the same as described above, but only triggered on demand.
    //Set the "loaded" flag to true.


    public void initializeTreeView(){
        folderTreeView.setMouseTransparent(true);
        folderTreeView.setOpacity(0.5);
        File rootFile = new File("D:/");
        if(rootFile.listFiles() != null){
            System.out.println("Loading directories from D:/...");
        } else {
            System.out.println("no directories found");
            return;
        }
        TreeItem<File> rootItem = new TreeItem<>(rootFile);
        folderTreeView.setRoot(rootItem);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                startTime = System.currentTimeMillis();
                createTree(rootFile, rootItem);
                return null;
            }
        };
        //Kurzschreibweise:
        //        Task<Void> task = new Task<>(() -> {
        //           createTree(rootFile, rootItem);
        //            return null;
        //        });
        task.setOnSucceeded(_ -> {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
                System.out.println("Finished loading directories! \n-> Total processing time: "+duration/1000+" seconds.");
            folderTreeView.setMouseTransparent(false);
            folderTreeView.setOpacity(1);
            cleanup();
        });
        new Thread(task).start();
    }

    private boolean containsMP3Files (File directory) {
            if (directory.isDirectory()){
                File[] checkMP3File = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));
                return checkMP3File != null && checkMP3File.length > 0;
            }
            return false;
    }

    //loopCount als Instanzvariable deklariert verhindert (versehentliche) Überschreibung durch Methoden
    //Dies hat zu tun mit Object State.
    //Instanzvariablen sind deklariert auf Klassenebene. Initialisierung erfolgt mit Instanziierung der Klasse.
    //Sie bestehen während der gesamten Laufzeit des Objektes-
    //Methodenvariablen oder lokale Variablen auf Methodenebene
    //Sie werden bei Erstellung der Methode erstellt und nach Ausführung zerstört
    //Erstellung cleanup()-Methode für Garbage Collector, da Variable nie wieder relevant


    //TreeView::getExpandedItemCount() // folderTreeView.getExpandedItemCount()

//alternative createTree method:
    private void createTree(File file, TreeItem<File> parentItem){

        File[] files = file.listFiles(File::isDirectory);

        if (files == null){
            System.out.println("No files found, cannot build Tree");
            return;
        }
            for (var f : files){
                if (containsMP3Files(f) && f.isDirectory()){
                    TreeItem<File> treeItem = new TreeItem<>(f);
                    parentItem.getChildren().add(treeItem);
                    createTree(f, treeItem);
                    loopCount++;
                    System.out.println("Scanned: "+loopCount+" of x");
                }
            }

    }

//    private void createTree(File file, TreeItem<File> parentItem) {
//        File[] files = file.listFiles(File::isDirectory);
//        int maxDepth = 50;
//        int depth = 0;
//        if (files != null) {
//            List<TreeItem<File>> batch = new ArrayList<>();
//            for (var f : files) {
//                if (depth > maxDepth){
//                    break;
//                }
//                depth++;
//                if (!f.canRead()){
//                    //if clause to skip unreadable files
//                    System.out.println("File not readable: "+f.getAbsolutePath());
//                    continue;
//                }
//                //infer filetype <>
//                loopCount += 1;
//                System.out.println("Scanned: "+loopCount+" of x");
//                TreeItem<File> item = new TreeItem<>(f);
//                batch.add(item);
//                createTree(f, item);
//            }
//
//            //Platform.runLater(() -> {
//                parentItem.getChildren().addAll(batch);
//                //folderTreeView.layout();
//           // }); // Batch-Update
//            //folderTreeView.setCellFactory(new Callback<TreeView<File>, TreeCell<File>>() {
//        }
//    }

//    private void createTree(File file, TreeItem<File> parentItem) {
//            File[] files = file.listFiles(File::isDirectory);
//
//            if (files != null && files.length > 0) {
//                int batchSize = 200;
//
//                // Process files in batches
//                for (int i = 0; i < files.length; i += batchSize) {
//                    int end = Math.min(i + batchSize, files.length); // Don't go out of bounds
//
//                    for (int j = i; j < end; j++) {
//                        File f = files[j];
//                        loopCount++;
//                        System.out.println("Scanned: " + loopCount + " of x");
//
//                        TreeItem<File> item = new TreeItem<>(f);
//                        parentItem.getChildren().add(item);
//
//                        // Recursive call — this will also process children in batches
//                        createTree(f, item);
//                    }
//
//                    // Optionally, insert a pause or yield control to avoid UI freezing
//                    // You could implement a background task and yield here if needed
//                }
//            }
//        }


    //    private void createTree(File file, TreeItem<File> parentItem) {
    //        File[] files = file.listFiles(File::isDirectory);
    //        if (files != null) {
    //            int batchSize = 100; // Adjust batch size as needed
    //            for (int i = 0; i < files.length; i += batchSize) {
    //                int end = Math.min(i + batchSize, files.length);
    //                for (int j = i; j < end; j++) {
    //                    var f = files[j];
    //                    TreeItem<File> item = new TreeItem<>(f);
    //                    parentItem.getChildren().add(item);
    //                    loopCount++;
    //                    System.out.println("Loops: " + loopCount);
    //                }
    //                // Optionally, add a delay or yield to allow UI updates
    //                try {
    //                    Thread.sleep(10); // Adjust delay as needed
    //                } catch (InterruptedException e) {
    //                    Thread.currentThread().interrupt();
    //                }
    //            }
    //        }
    //    }

//    private int getTotalItemCount(File[] files) {
//         totalLength = 1; // Count the root itself
//
//            for (var f : files) {
//                totalLength += 1;
//            }
//
//        return totalLength;
//    }

//    private int countTotalFolders(File[] files) {
//
//        int folderCount = 0;
//        if (files != null) {
//            folderCount += files.length;
//            for (var f : files) {
//                folderCount += f.length();
//            }
//        }
//        return folderCount;
//    }

//        int[] folderCount = new int[0];
//        if (files != null){
//            folderCount += files.length;
//            for (var f : files){
//                folderCount += countTotalFolders(f);
//            }
//        }
//        int totalFolderCount = 0;
//        for (var i : folderCount){
//            totalFolderCount += i;
//        }
//        return totalFolderCount;
//    }

    //done: TreeItems sollen nur nach Ordnern, nicht komplettem Pfad benannt sein.
    public void applyCellFactory() {
        //normal                                                                                       setCellFactory (...everything below
        folderTreeView.setCellFactory(new Callback<TreeView<File>, TreeCell<File>>() { // (functional interface)
            //Callback (P, R){R (output) call (P p(input));}
            // TreeView<File> is input, TreeCell<File> is output
            @Override
            public TreeCell<File> call(TreeView<File> treeView) { // call () {...
                return new TreeCell<>() {   //return new TreeCell<>{...
                    @Override
                    protected void updateItem(File item, boolean empty) { //protected void updateItem(File item, boolean empty) {...
                        super.updateItem(item, empty);

//                        if (empty || item == null) {
//                            setText(null);
//                        } else if (getTreeItem().getParent() == null) { //done: Check if getParent only displays root, and much improved code. Saved many thousands of unneeded iterations / checks
//                            setText("D:/");
//                        } else {
//                            setText(item.getName());
//                        }
                        // Even with mouseTransparent, rendering continues during:
                        //Platform.runLater(() -> parentItem.getChildren().addAll(batch));
                        // Multiple rapid runLater calls can queue overlapping renders
//    if (empty || item == null) {
//        setText(null);
//    } else {
//        TreeItem<File> currentItem = getTreeItem();
//        if (currentItem.getParent() == null) { // This condition is only checked once for the root node
//            setText("D:/");
//        } else {
//            setText(item.getName());
//
                        if (empty || item == null) {
                            setText(null); // Clear the cell if it's empty
                            setGraphic(null);
                        }
                        if (!empty && item != null){
                            if (getTreeItem().getParent() == null){
                                setText("D:/");
                            } else {
                                setText(item.getName());
                            }
                        }
                    }
                };
            }
        });
    }

    //Cellfactory customizes rendering of cells in a ListView, TableView, TreeView

//lambda
//        folderTreeView.setCellFactory(treeView -> new TreeCell<>() {
//            @Override
//            protected void updateItem(File item, boolean empty) {
//                super.updateItem(item, empty);
//                if (empty || item == null) {
//                    setText(null);
//                } else {
//                    setText(item.getName());
//                }
//            }
//        });




// - - - - - - - - - - - Initialisierung TreeView, Task Erstellung - - - - - - - - - - - - - - -

// _  ist event - Langschreibweise:
//        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
//            @Override
//            public void handle(WorkerStateEvent workerStateEvent) {
//                System.out.println("Finished loading directories no Lambda!");
//            }
//        });

            /*
             * public returnType:TreeCell<Type> call(TreeView<Type> parameterName) {
             * return new TreeCell<>() { anonymous class
             * @Override from TreeCell superclass
             * protected void updateItem(Type item, boolean empty) { protected accessible package-wide and subclasses
             * super.updateItem(item, empty); call superclass method and pass new parameters
             *
             * */
//        public | TreeCell<File> | call | (TreeView<File> treeView) | { // beginning of code block
//        // Create and return a new TreeCell with a custom updateItem method
//        return new TreeCell<>() {
//            @Override
//            protected void updateItem(File item, boolean empty) {
//                // Call the superclass's updateItem method
//                super.updateItem(item, empty);
//                // Set the text of the cell based on the item
//                if (empty || item == null) {
//                    setText(null);
//                } else {
//                    setText(item.getName());
//                }
//            }
//        };
//} // end of code block

    /*
    public void initialize() {
//        TreeItem<File> rootItem = new TreeItem<File> "C://";
//        folderTreeView.setRoot(rootItem);

        File rootFile = new File("D:\\");
        TreeItem<File> rootItem = new TreeItem<>(rootFile);
        folderTreeView.setRoot(rootItem);

        new Thread(() -> createTree(rootFile, rootItem)).start();
        System.out.println("Finished loading directories!");
    }
    */
        /*
        File[] roots = File.listRoots();
        TreeItem<File> rootItem = new TreeItem<>(new File("My Computer"));
        folderTreeView.setRoot(rootItem);

        if (roots != null){
            for (var r : roots){
                TreeItem<File> item = new TreeItem<>(r);
                rootItem.getChildren().add(item);
                new Thread (() -> createTree(r, rootItem)).start();
            }
        }
    }
         */
// - - - - - - - - - - - - - - - - - - - - - - Slider and ProgressBar - - - - - - - - - - - - - - - - - - - - - -
    @FXML
    private void updateProgressBar () {
//        Button button = new Button("test");
//        button.setTooltip();
    }

//Hintergrund: Fokus auf Auswahl behalten ...
    private void trackBorderPaneFocus(){
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

    //done: Volume Slider responsiv machen. Problem: Scheint nur zwischen 0 und 100 zu wechseln. Lösung: MediaPlayer hat Volume zwischen 0 - 1, Slider 0 - 100.
    //volumeSlider Error Handling mit Null Check
    //todo: Volume Slider stylen ein Stück weit wie ProgressBar.
    //done: sicherstellen dass volumeSlider und playerVolume stets synchronisiert sind.
    @FXML
    private void handleVolumeSlider () {
        //volumeSlider.minProperty().setValue(0);
        //volumeSlider.maxProperty().setValue(100);

//        volumeSlider.valueProperty().addListener((observable, oldValue, newValue -> {
//                    System.out.println("Initial volume level: "+volumeSlider.getValue());
//            double volumeSet = volumeSlider.setValue(newValue.doubleValue());
//            player.volumeProperty().setValue(volumeSet);
//                    System.out.println("New volume level: "+volumeSet);
        //}));

        if (player == null){
            System.out.println("No player instance found, cannot set volume. Will be updated on playback within 0.05 margin of deviation.");
        }
        volumeSlider.addEventFilter(ScrollEvent.SCROLL, scrollEvent -> {
            double delta = scrollEvent.getDeltaY() > 0 ? 0.05 : -0.05;
//                 double delta = scrollEvent.getDeltaY();
//                 if (delta > 0) {
//                     delta = 0.05;
//                 } else {
//                     delta = -0.05;
//                 }
            double previousVolume = volumeSlider.getValue();
            double newVolume = previousVolume+delta;
            Platform.runLater(() -> {
                volumeSlider.setValue(newVolume);
            });
            System.out.println("New Volume set by mouse wheel to: "+newVolume);

        });
//        volumeSlider.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
//
//        });
        //done: idealerweise gar nicht erst Fokus auf VolumeSlider erlauben, und all dies sparen
            volumeSlider.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            if (previousFocus != null) {
                previousFocus.requestFocus(); // Setze den Fokus zurück
            }

        });

        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldNunmber, Number newNumber) {
                System.out.println("initial volume level: " + volumeSlider.getValue());
                double volumeSet = newNumber.doubleValue();



                if (player != null){
                    System.out.println("player volume: " + player.getVolume());
                    System.out.println("set volume level to: " + volumeSet);
                    Platform.runLater(() -> {
                        player.setVolume(volumeSet);
                    });
                }
            }
        });
    }

    private void setProgressBar() {
        player.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            double offset =  0.03; // 3%
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
            int minutes = (int) (currentTime%3600) / 60;
            int seconds = (int) currentTime%60;


            //cool shorthand for an if-else statement. condition ?ifTrue :ifFalse
            String timeString = (hours > 0)
//String.format method: % specifies start of format; d decimal integer.
//02: 0 means to pad with leading zeroes if necessary. 2 is the width of the number
                    ? String.format("%d:%02d:%02d", hours, minutes, seconds)
                    : String.format("%02d:%02d", minutes, seconds);
            Platform.runLater(() -> {
                barTimer.setText(timeString);
            });
            //barTimer.setText(String.format("%.00f", newValue.toSeconds()));

        });
    }

// - - - - - - - - - - - - - - - - - - - - - PlaybackController - - - - - - - - - - - - - - - - - - - - - - - - -

    private void playSelection() {
        if (player != null){
            player.stop();
            player.dispose();
            System.out.println("Player instance detected and disposed, create new instance...");
        }
        if (selectedSong == null){
            System.err.println("[[playSelection]]: selectedSong is null, returning...");
            //return;
        }
        Media newPlayback = songToMedia(selectedSong);

        player = new MediaPlayer(newPlayback);
        currentQueue = queue.getQueue();
        System.out.println("Playing after double click or enter press newPlayBack: "+newPlayback+", which is the same as selectedSong.getTitle(): "+selectedSong.getTitle());
        currentSong = selectedSong;

        initiatePlay();
        player.play();
    }

    private void initiatePlay () {
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

    //done: Styling funktioniert, aber nicht (nun) richtig
    private void styleCurrentSong(){
        //done: style ( problem: duplicate songs )
//       playingTableView.setRowFactory(rv -> new TableRow<> () {
//           @Override
//           protected void updateItem(Song item, boolean empty) {
//               super.updateItem(item, empty);
//               if (item != null && item.equals(currentSong)) {
//                   setStyle("-fx-font-weight: bold;");
//               } else {
//                   setStyle(""); // Reset style for other rows
//               }
//           }
//       });
        Song styleSong = currentQueue.get(countingIndex);
        centerTableView.setRowFactory(tview -> new TableRow<>() {
            @Override
            protected void updateItem(Song item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle(""); // Reset style for leere oder ungültige Zeilen
                } else if (item.equals(styleSong)) {
                    setStyle("-fx-font-weight: bold;"); // Stil für die aktuelle Zeile setzen
                } else {
                    setStyle(""); // Stil für andere Zeilen zurücksetzen
                }
            }
        });
        playingTableView.setRowFactory(tview -> new TableRow<>() {
            @Override
            protected void updateItem(Song item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle(""); // Reset style for leere oder ungültige Zeilen
                } else if (item.equals(styleSong)) {
                    setStyle("-fx-font-weight: bold;"); // Stil für die aktuelle Zeile setzen
                } else {
                    setStyle(""); // Stil für andere Zeilen zurücksetzen
                }
            }
        });
//                if (item != null && item.equals(currentSong)) {
//                    setStyle("-fx-font-weight: bold;");
//                } else {
//                    setStyle(""); // Reset style for other rows
//                }


//        playingTableView.setRowFactory(new Callback<TableView<Song>, TableRow<Song>>() {
//            @Override
//            public TableRow<Song> call(TableView<Song> songTableView) {
//                return new TableRow<>();
//            }
//        });

        //item und empty werden von JavaFX vorgegeben
        //setRowFactory nimmt Callback an, welcher eine TableRow erstellt.
        // -> TableView Eingabe, TableRow Ausgabe => new TableRow.
        // heißt: tview -> (wird zu) new TableRow<>() { ... (Körperinhalt der anonymen Klasse = was überschrieben wird.

        //Mit anderen Worten: Man möchte sich die Methode updateItem zunutze machen, um die Zelle mit neuen Eigenschaften zu aktualisieren.
        // Damit möchte man aber nicht alle Basiseigenschaften überschreiben, sondern nur die einzelnen Aspekte, die man im Körper der
        // neuen anonymen Klasse definiert. Zum Beispiel hier möchte man nur den Style setzen, und den Rest der Eigenschaften unangetastet
        // lassen. Darum wird die nächste valide Implementierung der Methode in einer Elternklasse aufgerufen, um diese Kernfunktionen zu
        // gewährleisten.
//        centerTableView.setRowFactory(new Callback<TableView<Song>, TableRow<Song>>() {
//            @Override
//            public TableRow<Song> call(TableView<Song> tview) {
//                return new TableRow<>() {
//                    @Override
//                    protected void updateItem(Song item, boolean empty) {
//                        super.updateItem(item, empty);
//                        if (item != null && item.equals(currentSong)) {
//                            setStyle("-fx-font-weight: bold;");
//                        } else {
//                            setStyle(""); // Reset style for other rows
//                        }
//                    }
//                };
//            }
//        });

    }

    //done: play method respond to focus instead of selection
    //done: play method respond to double click and enter press on selection

    private void setProgressBarLabel(){
        progressBarLabel.setText(currentQueue.get(countingIndex).getTitle());
    }

    private void playerBehavior() {
        //player.setOnEndOfMedia(this::playNextOrStop);
//        player.setOnEndOfMedia(() -> {
//            playNextOrStop();
//                });


        if (player == null) {
            System.out.println(">playerBehavior: No player instance found, returning.");
            return;
        }
//        player.setOnEndOfMedia(() -> {
//            ...
//        });
        // =
        //Autoplay: on
        player.setOnEndOfMedia(() -> {
            System.out.println("endOfMedia triggered by method playerBehavior();");
            progressBar.setProgress(0);
            playNextOrStop();
        });




    }
    //mostly done: clickPlay auslagern / abspecken
    //done: Timer updaten
    //done: Timer formatieren
    //todo: ProgressBar anklickbar machen und Lied zur Stelle vorspulen
    //todo: kleine Popups bei Mouseover: ProgressBar Minute:Sekunde, über TableColumn gesamter Inhalt, ebenso über TreeItem.

    ///A Scene in JavaFX represents the content of a stage (window). It is a container for all the visual elements
    //done: morph play symbol into pause symbol during playback
    private void transformPlayButton(){
        if (player == null){
            return;

        }
        player.statusProperty().addListener((observable, oldStatus, newStatus) -> {
            if (newStatus == MediaPlayer.Status.PLAYING){
                playButtonLabel.setText("⏸"); // \u23F8
            }
            if (newStatus == MediaPlayer.Status.STOPPED || newStatus == MediaPlayer.Status.PAUSED){
                playButtonLabel.setText("▶"); // \u25B6
            }
        });

        // \u25B6 for play (▶) and \u23F8 for pause (⏸).
    }

    private void handleEnterOrDoubleClickPlayingTableView() {
        if (player != null){
            player.stop();
            player.dispose();
        }
        selectedSong = playingTableView.getSelectionModel().getSelectedItem();
        System.out.println("[DoubleClick or Enter PlayingTableView]: Selected Song: "+playingTableView.getSelectionModel().getSelectedItem());
        Media newPlayback = songToMedia(selectedSong);
        System.out.println("[DoubleClick or Enter PlayingTableView]: Assigning new Media to play: "+newPlayback);
        player = new MediaPlayer(newPlayback);
        currentSong = selectedSong;
        if (currentQueue.contains(selectedSong)){
            countingIndex = currentQueue.indexOf(selectedSong);
            System.out.println("countingIndex: "+countingIndex);
        } else {
            System.err.println("Queue does not contain selectedSong, countingIndex may be desynchronized");
        }
        player.play();
        initiatePlay();
    }



    private void handleEnterOrDoubleClickCenterTableView() {
        selectedSong = centerTableView.getSelectionModel().getSelectedItem();
//        for (var s : centerList){
//            System.out.println("lookup Metadata: "+s.getAlbum());
//            System.out.println("lookup Metadata: "+s.getTitle());
//            System.out.println("lookup Metadata: "+s.getPath());
//        }
        if (currentQueue != null){
            currentQueue.clear();
            System.out.println("currentQueue cleared.");
        }
        currentQueue = FXCollections.observableArrayList(centerTableView.getItems());
        queue.setQueue(currentQueue);
        playingTableView.setItems(queue.getQueue());
        if (queue.getQueue() != null){
            for (var song : queue.getQueue()){
                System.out.println("Added: "+song.getTitle());
            }
        }

        if (player != null){
            player.stop();
            player.dispose();
        }

        selectedSong = centerTableView.getSelectionModel().getSelectedItem();
        System.out.println("[DoubleClick or Enter PlayingTableView]: Selected Song: "+centerTableView.getSelectionModel().getSelectedItem());
        Media newPlayback = songToMedia(selectedSong);
        System.out.println("[DoubleClick or Enter PlayingTableView]: Assigning new Media to play: "+newPlayback);
        player = new MediaPlayer(newPlayback);
        currentSong = selectedSong;
        if (currentQueue.contains(selectedSong)){
            countingIndex = currentQueue.indexOf(selectedSong);
            System.out.println("countingIndex: "+countingIndex);
        } else {
            System.err.println("Queue does not contain selectedSong, countingIndex may be desynchronized");
        }
        player.play();
        initiatePlay();

        if (currentQueue.contains(currentSong)){
            countingIndex = currentQueue.indexOf(currentSong);
        } else {
            System.err.println("Queue does not contain selectedSong, countingIndex may be desynchronized");
        }
//        System.out.println("Metadata: "+currentSong.getMetadata());
        //        if (player == null||player.getStatus() == MediaPlayer.Status.STOPPED) {
//
//            player = new MediaPlayer(new Media(new File(selectedSong.getPath()).toURI().toString()));
//            if (volumeSlider.getValue() != player.getVolume()){
//                player.setVolume(volumeSlider.getValue());
//            }
//
//            if(selectedSong != null) {
//                player.play();
//                queue.addSong(selectedSong);
//                System.out.println("Playing: " + selectedSong.getTitle());
//            }
//        } else {
//            MediaPlayer.Status status = player.getStatus();
//            if (status == MediaPlayer.Status.PAUSED) {
//                player.play();
//                System.out.println("Resuming playback");
//            } else if (status == MediaPlayer.Status.PLAYING) {
//                player.pause();
//                System.out.println("Paused playback");
//            }
//        }
//        setProgressBar();
    }

//player = new MediaPlayer(new Media(new File (selectedSong.getPath()).toURI().toString()));
//String uriString = new File("D:\\Musikmainaug2019\\Aku no Hana\\\uD83D\uDC40 Zankyou no Hana.mp3").toURI().toString();
    private void singlePlay(){
        if (player != null){
            player.stop();
            player.dispose();
            System.out.println("Disposed player instance for singlePlayback.");
        }
        selectedSong = centerTableView.getSelectionModel().getSelectedItem();
        queue.clearQueue();
        queue.addSong(UUID.randomUUID().hashCode(), selectedSong);
        currentQueue = queue.getQueue();
        playingTableView.setItems(currentQueue);
        Media assignSinglePlay = songToMedia(selectedSong);
        player = new MediaPlayer(assignSinglePlay);
        currentSong = selectedSong;
        if (currentQueue.contains(selectedSong)){
            countingIndex = currentQueue.indexOf(selectedSong);
            System.out.println("countingIndex:"+countingIndex);
        } else {
            System.err.println("Queue does not contain selectedSong, countingIndex may be desynchronized");
        }
        playSelection();
        initiatePlay();
        //todo: allow multiple selection during ctrl-click, probably redesign method with array for selection
        // außerdem strg+A für alle auswählen

    }
    private void addToQueue(){
        selectedSong = centerTableView.getSelectionModel().getSelectedItem();
        queue.addSong(UUID.randomUUID().hashCode(), selectedSong);
        System.out.println("Song successfully added to queue.");
    }

    private void deleteSongFromQueue(){
        int selectedSongIndex = currentQueue.indexOf(selectedSong);
        if (selectedSongIndex < currentQueue.indexOf(currentQueue.getLast())) {
            countingIndex -= 1;
            System.out.println("countingIndex = " + countingIndex);
        } else {
            countingIndex = currentQueue.indexOf(currentQueue.getLast());
            System.out.println("countingIndex set to last and only song in queue " + countingIndex);
        }

        if (selectedSong == currentSong) {
            System.err.println("Warning: currentSong deleted, initializing countingIndex variable. countingIndex == "+countingIndex);
        }
        queue.removeSong(selectedSong);
        System.out.println("Song successfully deleted from queue, countingIndex = "+countingIndex);



    }

    @FXML
    private void clickPlay() {
        if (queue == null) {
            System.out.println("No song in Playlist.");
            return;
        }
        if (player == null){
            System.out.println("No player found.");
            return;
        }
        if (player.getStatus() == MediaPlayer.Status.PLAYING){
            player.pause();
            System.out.println("Paused playback of "+currentSong);
        } else {
            player.play();
            System.out.println("Playing: "+currentSong);
        }

        //    return new Media(new File(song.getPath()).toURI().toString());
//
//        ObservableList<Song> currentQueue = queue.getQueue();
//        if (player == null ||player.getStatus() == MediaPlayer.Status.STOPPED) {
//            var media = songToMedia(currentSong);
//            player = new MediaPlayer(media);
//        }
//        //player.getStatus() == MediaPlayer.Status.PAUSED||player.getStatus() == MediaPlayer.Status.STALLED
//        player.play();
//        initiatePlay();
    }

    private void handleDoubleClick (MouseEvent event){

//        for (var i : list){
//            queue.getQueue().addAll(i);
//        }
    }


    //todo: idee: während drag auf ProgressBar per Tastendruck (oder drag out of bounds?) Tonspur anzeigen lassen

    private void handlePlayback () {
        //handleVolumeSlider();

    }


//Idee für Parser-Methode: Source und Path ineinander umwandeln. Path hat \ und Leerzeichen. Source hat / und %20 statt Leerzeichen
    //So wäre Media.getSource zu Song.getPath umwandelbar.

    private String parseSourceToPath(String mediaSource){

//        String sourceToPath = mediaSource.replace("%20", " ");
//        sourceToPath = sourceToPath.replace("file:/", "");
//        sourceToPath = sourceToPath.replace("/", "\\");
        try {
               String sourceToPath =  URLDecoder.decode(mediaSource, StandardCharsets.UTF_8)
                                //.replace("File:/", "")
                                .replace("/", "\\");
                        return sourceToPath;

        } catch (Exception e) {
            System.err.println("could not handle"+mediaSource+" - - "+e.getMessage());
            return mediaSource;
        }



    }

    private Song findSongByPath(ObservableList<Song> currentQueue, String mediaSourceToPath){

        for (var s : currentQueue){
            if (s != null){
                if (s.getPath().equals(mediaSourceToPath)){
                    return s;
                }
            }
        }
        //        for (var s : songList){
//            String file = new File(s.getPath()).toURI().toString();
//            if (file.equals(player.getMedia().getSource())){
//                System.err.println("identified currentSong via URI: "+file+", transmitting info to setCurrentSong");
//                return s;
//            }
//        }
//            if (s.getPath().equals(path)){
//                System.out.println("[[findSongByPath]]: return statement of findSongByPath(): " +
//                        "\n[[findSongByPath]]: currentSong object 's' is called "+s+"," +
//                        "\n[[findSongByPath]]:  whose path is s.getPath()=       "+s.getPath()+"," +
//                        "\n[[findSongByPath]]:  which is equal to parameter path="+path);


        return null;
    }

//done: test and fix with headphones
private void playPrevious() {
    System.out.println("current queue contains: "+currentQueue);

    if (player == null){
        System.out.println("No player instance found.");
        return;
    }

    int previousSongIndex = queue.getPreviousIfExists(countingIndex);
    Song previousSong;

    if (previousSongIndex > -1){
        previousSong = currentQueue.get(previousSongIndex);
        player.stop();
        player.dispose();
        player = new MediaPlayer(songToMedia(previousSong));
        countingIndex -= 1;
        initiatePlay();
        player.play();
        System.out.println("Playing: "+previousSong.getTitle());
    } else {
        player.seek(Duration.ZERO);
        System.err.println("Setting playback to 0.");
    }

}
    private void initializeAndPlay(Song song) {
        Media media = songToMedia(song);
        //xx update countingIndex?
        System.out.println("[[initializeAndPlay]]: Stopping playback and setting media: " + media);
        player.stop();
        player.dispose();
        player = new MediaPlayer(media);
        player.play();
        initiatePlay();
    }

    private void setCurrentSong(){
//        System.out.println("Method called: setCurrentSong (by initiatePlay())");
//
//        ObservableList<Song> currentQueue = queue.getQueue();
//        String currentMediaSource = player.getMedia().getSource();
//        System.out.println("Detected currentMediaSource: "+currentMediaSource);
//
//        //String sourceToPath = parseSourceToPath(currentMediaSource);
//
//        //String sourceToPath = Paths.get(currentMediaSource).toString();
//
//        System.out.println("sourceToPath: "+player.getMedia().getSource());
//        currentSong = findSongByPath(currentQueue, player.getMedia().getSource());
//
//
//        System.out.println("currentSong *"+currentSong+"* as determined by setCurrentSong (parseSourceToPath): "+player.getMedia().getSource()+"\ncurrentMediaSource:"+currentMediaSource);
//        if (!player.getMedia().getSource().equals(currentSong)){
//            System.err.println("Current song "+currentSong+" is not equal to media source "+currentMediaSource);
//        }
        if (player == null || player.getMedia() == null) {
            System.err.println("[[setCurrentSong]]: Kein Player oder keine Media-Quelle gefunden.");
            currentSong = null;
            return;
        }

        //Song tentativeCurrentSong = currentQueue.get(countingIndex);
        //int currentId = queue.getUniqueId(tentativeCurrentSong);
        if (currentSong != selectedSong){
            currentSong = currentQueue.get(countingIndex);
        }

        //if queue.containsDuplicate(currentSong) > break
//        String currentMediaSource = player.getMedia().getSource();
//        String sourcePath = Paths.get(URI.create(currentMediaSource)).toString();
//        Media currentMedia = player.getMedia();
//        int currentSongIndex = -1;
//        for (int i = 0; i < currentQueue.size(); i++){
//            if (currentQueue.get(i).getPath().equals(sourcePath)){
//                currentSongIndex=i;
//                break;
//            }
//        }

        //currentSong =

        System.out.println("[[setCurrentSong]]: currentSong erfolgreich gesetzt: " + currentSong.getTitle());


        if (currentSong == null) {
            System.err.println("Unable to set currentSong: "+currentSong);
            return;
        }

    }

    //done: potentiell auslagern in "private Song determineCurrentSong(ObservableList<Song> list)?"
    //done: Button für Playlist wiederholen - boolean Instanzvariable? (fragen)
    //todo: Button für Sleep Timer: Mit oder ohne Volumeverringerung über Zeit.
    //erstmalnicht: playPrevious: If playback >3?5? Sekunden: seek 0 currentSong (check mediamonkey standard)
    //done: Speichere einfach aktuellen Index von currentSong und rufe ihn ab. Falls Index > queue.size, spiel das letzte Lied.
    //done: für playprevious: selbe Logik, aber -1, und falls Index > queue.size, spiel das vorletzte Lied.
    // next: falls queue == null; stop und dispose.
    // previous: falls queue == null, aber player != null, progress auf 0 setzen. Falls player == null, return.
    //done: falls nicht schon vorhanden, automatisches Playerverhalten. onEndOfMedia: schauen ob Song in Queue, welcher nicht currentSong ist. Wenn ja, playback. Wenn nein, stop (nicht dispose).
    // fragen: "automatisches player behavior" ist nicht setzbar, oder? ist es richtig, dies mit einer Initialize-Methode bei jeder Playererstellung zu aktualisieren?
    //Methode zur Steuerung des Verhaltens bei endOfMedia
    private void playNextOrStop() {
        System.out.println("[[playNextOrStop]]: Method call: playNextOrStop.");
        if (player == null) {
            System.out.println("[[playNextOrStop]]: No player found.");
            return;
        }
        if (!repeatQueue || currentQueue == null){
            player.stop();
        }
//        if (currentQueue.contains(currentSong)){
//            countingIndex = currentQueue.indexOf(currentSong);
//            System.out.println("countingIndex updated="+countingIndex);
//        }else {
//            System.err.println("Queue does not contain selectedSong, countingIndex may be desynchronized");
//        }
        //autoPlay
        //repeatQueue
        //bedenken= size ist immer lastindex+1
        int nextSongIndex;
        Song firstSong = currentQueue.getFirst();
        Media firstSongMedia = songToMedia(firstSong);
        if (queue.isLastIndex(countingIndex)){
            if (autoPlay && repeatQueue){
                player.stop();
                player.dispose();
                countingIndex = currentQueue.indexOf(firstSong);
                player = new MediaPlayer(firstSongMedia);
                initiatePlay();
                player.play();
                System.out.println("End of queue reached, playing from the beginning.");
            }
            if (autoPlay && !repeatQueue){
                player.stop();
                System.out.println("End of queue reached, stopping media.");
            }
            if (!autoPlay && !repeatQueue){
                player.stop();
                System.out.println("(Incidentally) End of queue reached, stopping media.");
            }
            if (!autoPlay && repeatQueue){
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
            if (autoPlay){
                player.stop();
                player.dispose();
                countingIndex = nextSongIndex;
                player = new MediaPlayer(nextSongMedia);
                initiatePlay();
                player.play();
                System.out.println("End of Media: Playing next Song in queue");
            }
            if (!autoPlay){
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



        /*
        if (currentSongIndex == currentQueue.size() - 1) {
            if (repeatQueue == true){
                System.out.println("Replaying queue from first song");
                player.stop();
                player.dispose();
                Media repeatMedia = songToMedia(currentQueue.getFirst());
                player = new MediaPlayer(repeatMedia);
                player.play();
                return;
            } else {
                player.stop();
                System.out.println("[[playNextOrStop]]: No more songs in current queue, playback stopped.");
                return;
            }
        } else {
            nextSong = currentQueue.get(currentSongIndex + 1);
            System.out.println("[[playNextOrStop]]: Setting next song: " + nextSong);
        }
        if (nextSong != null) {
            if (currentSong != null) {
                player.stop();
                Media nextPlay = songToMedia(nextSong);
                player = new MediaPlayer(nextPlay);
                player.play();
                initiatePlay();
                System.out.println("[[playNextOrStop]]: Playing next song in playlist: " + nextPlay.getSource());
            }
        }
*/
    }

    private Media songToMedia(Song song){
        if (song == null){
            return null;
        }
        return new Media(new File(song.getPath()).toURI().toString());
    }
//    private Song mediaToSong(Media media){
//        return new Media(new File(song.getPath()).toURI().toString());
//    }



    @FXML
    public void clickStop() {
        if (player != null) {
            player.stop();
            System.out.println("Stopped playback");
        }
    }


    //done: use/create update playlist methods
    //done: enable skipping to parts of playlist for playback
    //partially done: required steps: instantiate ObservableList in method constructor. Instantiate Playlist in initialize method. update variable names and call Playlist methods.
    @FXML
    private void clickPrevious(){
        playPrevious();

    }

    @FXML
    private void clickNext(){
        playNextOrStop();
    }

    //done: make autoplay PlayList default behavior,
    // create button and method for "stop after current track", which gets reset after playback stop
    // optionally configurable to not turn off on playback end.

    //done: set focus on Playlist on click

    //done: make TableView and Queue respond to keyboard input.
    // Enter: clearList and add entire folder to queue and play // just play.
    // alt+Enter: clearList and add selected song to queue
    // ctrl+Enter: add selected song to queue
    // Delete: Delete file (ask permission) // remove from list.

    //todo: Set new focus on arrow up/down.

    //todo: add further playback altering methods and buttons:
    // randomize playback
    // repeat playback
    // sleep timer
    // potentially advanced functions such as transpose, playback speed, equalizer
    //todo: drag & drop in and into queue


    // - - - - - - - - - - - - - - - - - - - - - TreeViewSelectionController - - - - - - - - - - - - - - - - - - - - - - - - -
//done: Nach mp3 Dateien filtern, möglichst vor dem ersten Ladeprozess
//done: Dateipfade im TreeView auf Ordnernamen reduzieren Cellfactory?
//done: Dateipfade in Titelspalte auf Dateinamen reduzieren Cellfactory? Cut/Trim Methode?


    //todo: besseres mouse handling für TreeView, idee:
    //public void initializeTreeView() {
    //    File rootFile = new File("D:/");
    //    TreeItem<File> rootItem = new TreeItem<>(rootFile);
    //    folderTreeView.setRoot(rootItem);
    //
    //    // Add an event filter to handle mouse clicks on the expand/collapse arrows
    //    folderTreeView.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
    //        Node node = event.getPickResult().getIntersectedNode();
    //        if (node instanceof TreeView || (node != null && node.getStyleClass().contains("tree-disclosure-node"))) {
    //            event.consume(); // Prevent the event from being processed further
    //        }
    //    });
    //
    //    Task<Void> task = new Task<>() {
    //        @Override
    //        protected Void call() {
    //            createTree(rootFile, rootItem);
    //            return null;
    //        }
    //    };
    //    task.setOnSucceeded(_ -> System.out.println("Finished loading directories!"));
    //    new Thread(task).start();
    //}

    @FXML
    private void selectTreeItem(MouseEvent event) {
        //todo: comprehend

        //Determine if the click was on a TreeCell
        Node clickedNode = event.getPickResult().getIntersectedNode();
        while (clickedNode != null && !(clickedNode instanceof TreeCell<?>)) {
            clickedNode = clickedNode.getParent();
        }

        // If no TreeCell was found, clear the selection and return.
        if (clickedNode == null) {
            folderTreeView.getSelectionModel().clearSelection();
            // Optionally, clear the TableView as well.
            //todo: Deselektion funktioniert noch nicht, auch nicht im TableView
            centerList.clear();
            centerTableView.setItems(centerList);
            return;
        }


        TreeItem<File> selectedItem = folderTreeView.getSelectionModel().getSelectedItem();


        centerList.clear();

        if (selectedItem != null) {
            File folder = selectedItem.getValue();
            System.out.println("Selected: " + folder);


            //done: plan:
            // Verzeichnis scannen und Song-Objekte erstellen (eigenes Loop 1)
            // Metadaten extrahieren (eigenes Loop 2) und in Map in Song speichern
            // beide verknüpfen (ggf. in Loop 2, alternativ durch die Daten iterieren)
            // dann in centerTableView laden
            if (folder.isDirectory()) {
                // List only .mp3 files in the directory.
                //alternative?
                // FileChooser fileChooser = new FileChooser();
                //         fileChooser.setTitle("Open MP3 File");
                //         fileChooser.getExtensionFilters().addAll(
                //                 new FileChooser.ExtensionFilter("MP3 Files", "*.mp3")
                File[] files = folder.listFiles((_file, str) -> str.toLowerCase().endsWith(".mp3"));
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
                        System.out.println("Added song to centerList: " + s.getTitle());
                    }
                }
            } else {
                System.out.println("Not a directory");
            }
        } else {
            System.out.println("Invalid selection");
        }



        // Update the TableView with the new list.
        centerTableView.setItems(centerList);
        //System.out.println("Added to TableView centerList: "+centerList);;

        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
        albumColumn.setCellValueFactory(new PropertyValueFactory<>("album"));
        lengthColumn.setCellValueFactory(new PropertyValueFactory<>("length"));
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
    }

    public Map<String, Object> assembleMetadata(Song song){
        return null;
    }

    public Map<String, Object> scanMetadata(Song file){
        Map<String, Object> metadataMap = null;
        return metadataMap;
    }


    // - - - - - - - - - - - - - - - - - - - - - TableViewSelectionController - - - - - - - - - - - - - - - - - - - - - - - - -

    //TreeItem<File> item = folderTreeView.getSelectionModel().getSelectedItem();


    // Song song = new Song(f.getAbsolutePath());
//                        centerList.add(song);2

    private void hoverTooltip() {

//        Duration delayDuration = Duration.millis(10);
//        barTooltip.setShowDelay(delayDuration);
//
//        barTooltip.setText("Beispiel");

    }

    //@FXML
    //private void centerTableViewClearHandler(MouseEvent event) {
    //    Node clickedNode = event.getPickResult().getIntersectedNode();
    //    while (clickedNode != null && !(clickedNode instanceof TableRow<?>)) {
    //        clickedNode = clickedNode.getParent();
    //    }
    //
    //    // If no TableRow was found, or if the found row is empty, clear the selection and return.
    //    if (clickedNode == null || ((TableRow<?>) clickedNode).isEmpty()) {
    //        centerTableView.getSelectionModel().clearSelection();
    //        return;
    //    }
    //
    //    // At this point, a valid TableRow with a Song object was clicked.
    //    Song song = centerTableView.getSelectionModel().getSelectedItem();
    //    if (song != null) {
    //        System.out.println("Selected: " + song.getTitle());
    //        selectedSong = song;
    //        queue.add(song);
    //        playingTableView.setItems(queue);
    //        queueTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
    //    }
    //}


    //Scheduling: When Platform.runLater is called, it schedules the Runnable to be executed on the JavaFX Application Thread. This is the thread responsible for handling all JavaFX UI updates.
    //Execution: The JavaFX runtime maintains a queue of tasks to be executed on the JavaFX Application Thread. The Runnable provided to Platform.runLater is added to this queue.
    //Thread Safety: By ensuring that the Runnable is executed on the JavaFX Application Thread, Platform.runLater guarantees that any UI updates within the run method are performed in a thread-safe manner, avoiding concurrency issues.

    //done: review logic and rewrite so playingTableView selection gets detected (latter part not necessary)


    private void centerTableViewClearHandler() {
        centerTableView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Node clickedNode = event.getPickResult().getIntersectedNode();
                System.out.println("Clicked node: " + clickedNode);

                // Traverse the node hierarchy to find the TableRow
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
                        centerTableView.getSelectionModel().clearSelection();
                    }
                }
            }
        });
                /*Node clickedNode = event.getPickResult().getIntersectedNode();
                while (clickedNode != null && !(clickedNode instanceof TableRow<?>)) {
                    clickedNode = clickedNode.getParent();
                }

                // If no TableRow was found, or if the found row is empty, clear the selection and return.
                if (clickedNode == null || ((TableRow<?>) clickedNode).isEmpty()) {
                    centerTableView.getSelectionModel().clearSelection();
                    return;
                }

                // At this point, a valid TableRow with a Song object was clicked.
                if (centerTableView.getSelectionModel().getSelectedIndex() > -1) {
                    int index = centerTableView.getSelectionModel().getSelectedIndex();
                    Song clickedItem = centerTableView.getSelectionModel().getSelectedItem();
                    String content = clickedItem.toString();

                    centerTableView.requestFocus();
                    boolean centerIsFocused = centerTableView.isFocused();

                    System.out.println("centerTableView is focused: "+centerIsFocused);



                    System.out.println();
                    centerTableView.getSelectionModel().select(index);
                    centerTableView.getFocusModel().focus(index);
                    System.out.println("focused Index " + index + " in centerTableView");
                }

                //call handleCenterSelectionChange



                Song song = centerTableView.getSelectionModel().getSelectedItem();
                if (song != null) {
                    System.out.println("Selected: " + song.getTitle());
                    if (selectedSong != null){
                        selectedSong = null;
                    }
                    selectedSong = centerTableView.getSelectionModel().getSelectedItem();
                    //todo: review
                    playingTableView.setItems(queue.getQueue());
                    queueTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
                }*/
    }
    private void playingTableViewClearHandler() {
        playingTableView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Node clickedNode = event.getPickResult().getIntersectedNode();

                System.out.println("Clicked node: " + clickedNode);
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


    private void handleCenterSelectionChange() {
        int selectedIndex = centerTableView.getSelectionModel().getSelectedIndex();
        if (selectedIndex != -1) {
            // Update focus to the selected index
            centerTableView.getFocusModel().focus(selectedIndex);
            System.out.println("Selection changed. Current index: " + selectedIndex);
            // Additional logic, like updating other UI components, can go here.
        }
    }

    private void cleanup(){
        loopCount = 0;
        startTime = 0;
    }


//            TableRow<?> row = null;
//
//            // Traverse up the node hierarchy to find the TableRow
//            while (clickedNode != null) {
//                if (clickedNode instanceof TableRow) {
//                    row = (TableRow<?>) clickedNode;
//                    System.out.println(((TableRow<?>) clickedNode).getItem()+"clickedNode");
//                    System.out.println(row.getItem()+"row");
//                    break; // Exit the loop once a TableRow is found
//
//                }
//                clickedNode = clickedNode.getParent(); // Move to the parent node
//            }
//
//            // If no TableRow was found, or if the found row is empty, clear the selection
//            if (row == null || row.getItem() == null) {
//                centerTableView.getSelectionModel().clearSelection();
//            }
//            return;




//alternativ erldigt: Adapt cellfactory for TableView and ObservableList vs. TreeView and TreeItems via Konstruktor selbst getName
            /*
                folderTreeView.setCellFactory(new Callback<TreeView<File>, TreeCell<File>>() { //Callback<>() ((functional interface)) {...
        @Override
        public TreeCell<File> call(TreeView<File> treeView) { //public TreeCell<File> call (TreeView<File> treeView) {...
            return new TreeCell<>() {   //return new TreeCell<>{...
                @Override
                protected void updateItem(File item, boolean empty) { //protected void updateItem(File item, boolean empty) {...
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else if (getTreeItem().getParent() == null) {
                        setText("D:/");
                    } else {
                        setText(item.getName());
                    }
                }
            };
        }
    });
            */


//            loadFilesFromPath(data);
//            centerTableView.setItems(centerList);
//            titleColumn.getTableView();


//    public ArrayList <Song> createCenterView () {
//        ArrayList <Song> asd;
//        return asd;
//    }


//    private void loadFilesFromPath(File folder){
//        if (folder != null && folder.isDirectory()){
//            File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));
//            if (files != null) {
//                centerList.addAll(files);
//            }
//        }


    @FXML
    private void handleRefresh() {
        initialize();
    }

    private void initializeVolumeSlider(){
        volumeSlider.setMin(0);
        volumeSlider.setMax(1);
        volumeSlider.setValue(0.2);
    }

    private void initializeCenterTableViewListener(){
        //----------MouseEvents----------
        centerTableView.setOnMouseClicked(event -> {
            //click delay:
//            PauseTransition clickDelay = new PauseTransition(Duration.seconds(0.5));
//            clickDelay.setOnFinished(event -> {
//                // Aktionen, die nach der Verzögerung ausgeführt werden sollen
//            });
            //finally add check if (!clickDelay.getStatus().equals(PauseTransition.Status.RUNNING)) {
            Node clickedNode = event.getPickResult().getIntersectedNode();

            while (clickedNode != null && !(clickedNode instanceof TableRow)) {
                clickedNode = clickedNode.getParent();
                System.out.println("Clicked Node (prevent play if no song was clicked): "+clickedNode);
            }

            if (clickedNode instanceof TableRow<?> row) {
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
        //done: handle enter press as no valid item is selected
        //----------KeyEvents----------
        centerTableView.setOnKeyPressed(keyEvent -> {

            if (centerTableView.getSelectionModel().getSelectedItem() == null){
                System.out.println(">centerTableView: no song selected, return...");
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
                        System.out.println("(Spacebar) Playback paused from "+player.getStatus());
                        player.pause();
                    }

                    if (player.getStatus() == MediaPlayer.Status.PAUSED || player.getStatus() == MediaPlayer.Status.HALTED|| player.getStatus() == MediaPlayer.Status.STOPPED) {
                        System.out.println("(Spacebar) Playback resumed from "+player.getStatus());
                        player.play();
                    }
                }
            }
        });
    }
    private void initializePlayingTableViewListener(){
        playingTableView.setOnMouseClicked(event -> {
            Node clickedNode = event.getPickResult().getIntersectedNode();


            while (clickedNode != null && !(clickedNode instanceof TableRow)) {
                clickedNode = clickedNode.getParent();
                System.out.println("Clicked node: " + clickedNode);
            }
            if (clickedNode == null){
                System.err.println("playingTableView header clicked, returning...");
                return;
            }
//           Node clickedNode = event.getPickResult().getIntersectedNode();
//
//            while (clickedNode != null && !(clickedNode instanceof TableRow)) {
//                clickedNode = clickedNode.getParent();
//                System.out.println("Clicked Node (prevent play if no song was clicked): "+clickedNode);
//            }
//
//            if (clickedNode instanceof TableRow<?> row)
            if (playingTableView.getSelectionModel().getSelectedItem() == null){
                System.out.println(">playingTableView: No song selected, returning...");
                return;
            }
            if (event.getClickCount() == 2) {
                selectedSong = playingTableView.getSelectionModel().getSelectedItem();
                handleEnterOrDoubleClickPlayingTableView();
            }
        });

        playingTableView.setOnKeyPressed(keyEvent -> {
            if (playingTableView.getSelectionModel().getSelectedItem() == null){
                System.out.println(">playingTableView: No song selected, returning...");
                return;
            }
            if (keyEvent.getCode() == KeyCode.ENTER) {
                selectedSong = playingTableView.getSelectionModel().getSelectedItem();
                handleEnterOrDoubleClickPlayingTableView();
            } else if (keyEvent.getCode() == KeyCode.SPACE) {
                if (player != null) {
                    if (player.getStatus() == MediaPlayer.Status.PLAYING) {
                        System.out.println("(Spacebar) Playback paused from "+player.getStatus());
                        player.pause();
                    }

                    if (player.getStatus() == MediaPlayer.Status.PAUSED || player.getStatus() == MediaPlayer.Status.HALTED|| player.getStatus() == MediaPlayer.Status.STOPPED) {
                        System.out.println("(Spacebar) Playback resumed from "+player.getStatus());
                        player.play();
                    }
                }
            } else if (keyEvent.getCode() == KeyCode.DELETE){
                        deleteSongFromQueue();
                    }
                });
    }



//    private ArrayList<Song> filesToTableView(ArrayList<File> file){
//
//    }


//            File[] files = data.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));
//            if (files != null) {
//                centerList.addAll(files);
//            }
//            centerTableView.setItems(centerList);
//            titleColumn.getTableView();









//    private void loadView () {
//
//
//
//    }


//fxcollections.observablearralist



/*
package org.example.musikplayer_doit.controller;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.*;

//ToDo: Unverständlichen Code entfernen und schrittweise rekonstruieren.


public class Controller implements Initializable {

MediaPlayer player;

@FXML
private TreeView<File> folderTreeView;
@FXML
private TableView<File> centerTableView;
@FXML
private TableView<File> listTableView;
@FXML
TableColumn<File, String> titleColumn; //TableColumn<S, T> S=Datentyp von TableView, T=Datentyp in Spalte

private ExecutorService executorService;

    @FXML
    public void clickPlay() {
        if (player == null) {
            String uriString = new File("D:\\Musikmainaug2019\\Aku no Hana\\\uD83D\uDC40 Zankyou no Hana.mp3").toURI().toString();
            player = new MediaPlayer(new Media(uriString));
            player.play();
            System.out.println("Playing: " + uriString);
        } else {
            MediaPlayer.Status status = player.getStatus();
            if (status == MediaPlayer.Status.PAUSED || status == MediaPlayer.Status.STOPPED) {
                player.play();
                System.out.println("Resuming playback");
            } else if (status == MediaPlayer.Status.PLAYING) {
                player.pause();
                System.out.println("Paused playback");
            }
        }
    }

    @FXML
    public void clickStop() {
        if (player != null) {
            player.stop();
            System.out.println("Stopped playback");
        }
    }

    // FileTreeCellFactory

    // Multithreading: https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ForkJoinPool.html
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        File rootFile = new File("My Computer");
        TreeItem<File> rootItem = new TreeItem<>(rootFile);
        folderTreeView.setRoot(rootItem);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                ForkJoinPool pool = new ForkJoinPool();
                pool.invoke(new CreateTreeTask(rootFile, rootItem));
                return null;
            }
        };

        task.setOnSucceeded(event -> System.out.println("Tree creation completed."));
        task.setOnFailed(event -> System.err.println("Tree creation failed: " + task.getException()));

        new Thread(task).start();
    }

    private static class CreateTreeTask extends RecursiveAction {
        private final File file;
        private final TreeItem<File> parentItem;

        public CreateTreeTask(File file, TreeItem<File> parentItem) {
            this.file = file;
            this.parentItem = parentItem;
        }

        @Override
        protected void compute() {
            File[] files = file.listFiles(File::isDirectory);
            if (files != null) {
                for (var f : files) {
                    TreeItem<File> item = new TreeItem<>(f);
                    parentItem.getChildren().add(item);
                    CreateTreeTask task = new CreateTreeTask(f, item);
                    task.fork();
                }
            }
        }
    }
}




        /*

    @FXML
    private void selectItem () {

    }

    private void loadView () {



    }


//fxcollections.observablearralist

}

*/

/*
Singleton Pattern for VolumeSlider AI suggestion:
public class MediaPlayerSingleton {
    private static MediaPlayer instance;

    private MediaPlayerSingleton() {
        // Private constructor to prevent instantiation
    }

    public static synchronized MediaPlayer getInstance() {
        if (instance == null) {
            instance = new MediaPlayer();
            instance.setVolume(0.5); // Set default volume
            instance.setOnReady(() -> {
                // Additional initialization if needed
            });
        }
        return instance;
    }
}
*/



}
