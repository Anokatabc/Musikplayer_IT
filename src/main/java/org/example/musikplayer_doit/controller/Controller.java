package org.example.musikplayer_doit.controller;
//test 123456
// import javafx.application.Platform;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.util.Callback;
import javafx.util.Duration;
import org.example.musikplayer_doit.model.Song;
import org.example.musikplayer_doit.services.ContextMenuService;

import org.example.musikplayer_doit.services.TreeViewBuilderService;

import java.io.File;

public class Controller {

    MediaPlayer player;


    @FXML
    private TreeView<File> folderTreeView;
    @FXML
    private TableView<Song> centerTableView;
    @FXML
    private TableView<Song> playingTableView;
    @FXML
    private TableColumn<File, String> titleColumn; //TableColumn<S, T> S=Datentyp von TableView, T=Datentyp in Spalte
    @FXML
    private TableColumn<File, String> pathColumn;
    @FXML
    private TableColumn<File, String> lengthColumn;
    @FXML
    private TableColumn<Song, String> queueTitleColumn;
    @FXML
    private TableColumn<Song, String> queueLengthColumn;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label barTimer;
    @FXML
    private Tooltip barTooltip;
    @FXML
    Slider volumeSlider;

    public Song selectedSong;
    public MediaPlayer currentSong;
    ContextMenuService contextMenuService;
    TreeViewBuilderService treeViewBuilderService;


    public ObservableList<Song> centerList = FXCollections.observableArrayList();
    public ObservableList<Song> playingList = FXCollections.observableArrayList();

//done: initialize() abspecken (auslagern)
    public void initialize() {
        initializeTreeView();
        applyCellFactory();
        //MouseEventService mouseEventService;
        centerTableView.requestFocus();
        //barTimer.setText("00:00");
        volumeSlider.setMin(0);
        volumeSlider.setMax(1);
        volumeSlider.setValue(0.2);




//        Platform.runLater(new Runnable() {
//            @Override public void run() {
//                progressBar.setProgress(counter/1000000.0);
//            }
//
//        });

    }

    // - - - - - - - - - - - - - - - - - - - - - TreeViewController - - - - - - - - - - - - - - - - - - - - - - - - -

    // Multithreading: https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ForkJoinPool.html

    public void initializeTreeView(){
        folderTreeView.setMouseTransparent(true);
        File rootFile = new File("D:/");
        TreeItem<File> rootItem = new TreeItem<>(rootFile);
        folderTreeView.setRoot(rootItem);

        //Anonyme Klasse von Task wird erstellt und instanziiert
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                createTree(rootFile, rootItem);
                return null;
            }
        };
        //Kurzschreibweise:
        //        Task<Void> task = new Task<>(() -> {
        //            createTree(rootFile, rootItem);
        //            return null;
        //        });
        task.setOnSucceeded(_ -> {
                System.out.println("Finished loading directories!");
            folderTreeView.setMouseTransparent(false);
        });
        new Thread(task).start();
    }

    private void createTree (File file, TreeItem<File> parentItem){
        File[] files = file.listFiles(File::isDirectory);
        if (files != null) {
            for (var f : files) {
                //infer filetype <>
                TreeItem<File> item = new TreeItem<>(f);
                parentItem.getChildren().add(item);
                createTree(f, item);
            }
            //folderTreeView.setCellFactory(new Callback<TreeView<File>, TreeCell<File>>() {
        }
    }

    //done: TreeItems sollen nur nach Ordnern, nicht komplettem Pfad benannt sein.
    public void applyCellFactory() {
        //normal                                                                                       setCellFactory (...everything below
        folderTreeView.setCellFactory(new Callback<TreeView<File>, TreeCell<File>>() { //Callback<>() ((functional interface)) {...
            @Override
            public TreeCell<File> call(TreeView<File> treeView) { //public TreeCell<File> call (TreeView<File> treeView) {...
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
                        //oder
//    if (empty || item == null) {
//        setText(null);
//    } else {
//        TreeItem<File> currentItem = getTreeItem();
//        if (currentItem.getParent() == null) { // This condition is only checked once for the root node
//            setText("D:/");
//        } else {
//            setText(item.getName());
//
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




    //done: Volume Slider responsiv machen. Problem: Scheint nur zwischen 0 und 100 zu wechseln. Lösung: MediaPlayer hat Volume zwischen 0 - 1, Slider 0 - 100.
    //volumeSlider Error Handling mit Null Check
    //todo: Volume Slider stylen ein Stück weit wie ProgressBar.
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
            System.out.println("No player instance found, cannot set volume. Will be updated on playback.");
            return;
        }
        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldNunmber, Number newNumber) {
                System.out.println("initial volume level: " + volumeSlider.getValue());
                double volumeSet = newNumber.doubleValue();

                    System.out.println("player volume: " + player.getVolume());
                    System.out.println("set volume level to: " + volumeSet);
                Platform.runLater(() -> {
                    player.setVolume(volumeSet);
                });
            }

        });

    }

// - - - - - - - - - - - - - - - - - - - - - PlaybackController - - - - - - - - - - - - - - - - - - - - - - - - -

    //todo: play method respond to focus instead of selection
    //todo: play method respond to double click and enter press on selection

    private void playerBehavior() {
        player.setOnEndOfMedia(() -> {

                });
    }
    //todo: clickPlay auslagern / abspecken
    //done: Timer updaten
    //done: Timer formatieren
    //todo: ProgressBar anklickbar machen und Lied zur Stelle vorspulen
    //todo: kleine Popups bei Mouseover: ProgressBar Minute:Sekunde, über TableColumn gesamter Inhalt, ebenso über TreeItem.
    @FXML
    private void clickPlay() {
        //String uriString = new File("D:\\Musikmainaug2019\\Aku no Hana\\\uD83D\uDC40 Zankyou no Hana.mp3").toURI().toString();

        if (selectedSong == null) {
            System.out.println("No song selected.");
            return;
        }
///A Scene in JavaFX represents the content of a stage (window). It is a container for all the visual elements
        //todo: morph play symbol into pause symbol during playback
//currentSong = player;
//player = new MediaPlayer(new Media(new File (selectedSong.getPath()).toURI().toString()));
        if (player == null||player.getStatus() == MediaPlayer.Status.STOPPED) {

//if (player == null) {
//        player = new MediaPlayer(new Media(new File(selectedSong.getPath()).toURI().toString()));
//    }
//
//    MediaPlayer.Status status = player.getStatus();
//    if (status == MediaPlayer.Status.STOPPED) {
//        player = new MediaPlayer(new Media(new File(selectedSong.getPath()).toURI().toString()));
//    }

            player = new MediaPlayer(new Media(new File(selectedSong.getPath()).toURI().toString()));
            if (volumeSlider.getValue() != player.getVolume()){
                double currentVolume = volumeSlider.getValue();
                player.setVolume(currentVolume);
            }
            player.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                double currentTime = newValue.toSeconds();
                double progress = currentTime / player.getTotalDuration().toSeconds();
                progressBar.setProgress(progress);
//                player.setOnReady(() -> {
//                    progressBar.setProgress(0);
//                });



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

    //todo: idee: während drag auf ProgressBar per Tastendruck (oder drag out of bounds?) Tonspur anzeigen lassen



                    //MediaPlayer player = this.player;
//                    MediaPlayer.Status status = player.getStatus();
//                    while (status == MediaPlayer.Status.PLAYING) {
//                        double duration = player.getTotalDuration().toSeconds();
//                        while (progressBar.getProgress() < player.getTotalDuration().toSeconds()) {
//                            progress = progressBar.getProgress();
//                            barTimer.setText("00:00");
//                            status = player.getStatus();
//                            if (status != MediaPlayer.Status.PLAYING) {
//                                break;
//                            }
//                        }
//                    }

                //player.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                //    double currentTime = newValue.toSeconds();
                //    int minutes = (int) currentTime / 60;
                //    int seconds = (int) currentTime % 60;
                //    String timeString = String.format("%02d:%02d", minutes, seconds);
                //    barTimer.setText(timeString);
                //
                //    double progress = currentTime / player.getTotalDuration().toSeconds();
                //    progressBar.setProgress(progress);
                //});




            if(selectedSong != null) {
                player.play();
                playingList.add(selectedSong);
                System.out.println("Playing: " + selectedSong.getTitle());
            }
        } else {
            MediaPlayer.Status status = player.getStatus();
            if (status == MediaPlayer.Status.PAUSED) {
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


    //todo: make methods interact with / update PlayList/Queue, then skip to respective index(?) in queue
    @FXML void clickPrevious(){

    }

    @FXML void clickNext(){

    }

    //todo: make autoplay PlayList default behavior, create button and method for "stop after current track", which gets reset after playback stop

    //todo: set focus on Playlist on click

    //todo: make TableView and Queue respond to keyboard input.
    // Enter: add to playlist and play // just play.
    // Delete: Delete file (ask permission) // remove from list.
    // Set new focus on arrow up/down.

    //todo: add further playback altering methods and buttons:
    // randomize playback
    // repeat playback
    // potentially advanced functions such as transpose, playback speed, equalizer
    //todo: drag & drop in queue


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

        // Determine if the click was on a TreeCell
        Node clickedNode = event.getPickResult().getIntersectedNode();
        while (clickedNode != null && !(clickedNode instanceof TreeCell<?>)) {
            clickedNode = clickedNode.getParent();
        }

        // If no TreeCell was found, clear the selection and return.
        if (clickedNode == null) {
            folderTreeView.getSelectionModel().clearSelection();
            // Optionally, clear the TableView as well.
            centerList.clear();
            centerTableView.setItems(centerList);
            return;
        }

        // At this point, a valid TreeCell was clicked.
        TreeItem<File> item = folderTreeView.getSelectionModel().getSelectedItem();

        // Clear previous list before loading new items
        centerList.clear();

        if (item != null) {
            File data = item.getValue();
            System.out.println("Selected: " + data);

            if (data.isDirectory()) {
                // List only .mp3 files in the directory.
                File[] files = data.listFiles((_, str) -> str.toLowerCase().endsWith(".mp3"));
                if (files != null) {
                    for (File f : files) {
                        // Assume Song has a constructor that accepts the file path.
                        Song song = new Song(f.getAbsolutePath(), null);
                        centerList.add(song);
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
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
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
    //private void selectTableItem(MouseEvent event) {
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
    //        playingList.add(song);
    //        playingTableView.setItems(playingList);
    //        queueTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
    //    }
    //}


    //Scheduling: When Platform.runLater is called, it schedules the Runnable to be executed on the JavaFX Application Thread. This is the thread responsible for handling all JavaFX UI updates.
    //Execution: The JavaFX runtime maintains a queue of tasks to be executed on the JavaFX Application Thread. The Runnable provided to Platform.runLater is added to this queue.
    //Thread Safety: By ensuring that the Runnable is executed on the JavaFX Application Thread, Platform.runLater guarantees that any UI updates within the run method are performed in a thread-safe manner, avoiding concurrency issues.

    @FXML
    private void selectTableItem(MouseEvent event) {
        //actually not needed. Check if there is reason to keep it.
        //can check Platform.isFxApplicationThread(). If true, is part of JavaFX main thread, and can ignore runLater.
        //if false, platform.runlater makes code run on the main JavafX thread and enables GUI updates.
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//...
//}});
                Node clickedNode = event.getPickResult().getIntersectedNode();
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
                    centerTableView.requestFocus();
                    centerTableView.getSelectionModel().select(index);
                    centerTableView.getFocusModel().focus(index);
                    System.out.println("focused Index " + index + " in centerTableView");
                }

                centerTableView.setOnMousePressed(event2 -> {
                    if (centerTableView.getSelectionModel().getSelectedIndex() >= 0) {
                        int index = centerTableView.getSelectionModel().getSelectedIndex();
                        Song clickedItem = centerTableView.getSelectionModel().getSelectedItem();
                        String content = clickedItem.toString();
                        for (Song i : centerList) {
                            if (i.toString().equals(content)) {
                                System.out.println("i: " + i);
                                System.out.println("content: " + content);
                                break;
                            }
                        }
                    }
                });

                Song song = centerTableView.getSelectionModel().getSelectedItem();
                if (song != null) {
                    System.out.println("Selected: " + song.getTitle());
                    selectedSong = centerTableView.getSelectionModel().getSelectedItem();
                    playingTableView.setItems(playingList);
                    queueTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
                }

    }
    @FXML
    private void showCenterContextMenu(MouseEvent event) {
        contextMenuService.displayContextMenu(event);
        centerTableView.setOnContextMenuRequested(e -> {
            ContextMenu contextmenu = new ContextMenu();
        });


        //e ->
        //.show(e.getScreenX(), e.getScreenY()));
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
public class Controller {
    private MediaPlayer player;

    public Controller() {
        this.player = MediaPlayerSingleton.getInstance();
    }

    public void handleVolumeSlider(double volume) {
        if (player != null) {
            player.setVolume(volume / 100.0); // Assuming the slider value is between 0 and 100
        }
    }
}
*/