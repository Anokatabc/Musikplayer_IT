package org.example.musikplayer_doit.controller;
//test 12356
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Window;
import javafx.util.Callback;
import org.example.musikplayer_doit.model.Song;
import org.jaudiotagger.audio.AudioFile;

import java.io.File;
import java.io.FilenameFilter;


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

    public Song selectedSong;
    public MediaPlayer currentSong;


    public ObservableList<Song> centerList = FXCollections.observableArrayList();
    public ObservableList<Song> playingList = FXCollections.observableArrayList();

    // - - - - - - - - - - - - - - - - - - - - - TreeViewController - - - - - - - - - - - - - - - - - - - - - - - - -

    // Multithreading: https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ForkJoinPool.html

    public void initialize() {
// - - - - - - - - - - - Initialisierung TreeView, Task Erstellung - - - - - - - - - - - - - - -
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
        task.setOnSucceeded(_ -> System.out.println("Finished loading directories!"));
        new Thread(task).start();
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

//normal                                                                                       setCellFactory (...everything below
        folderTreeView.setCellFactory(new Callback<TreeView<File>, TreeCell<File>>() { //Callback<>() ((functional interface)) {...
            @Override
            public TreeCell<File> call(TreeView<File> treeView) { //public TreeCell<File> call (TreeView<File> treeView) {...
                return new TreeCell<>() {   //return new TreeCell<>{...
                    @Override
                    protected void updateItem(File item, boolean empty) { //protected void updateItem(File item, boolean empty) {...
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else if (getTreeItem().getParent() == null) { //Todo: Check if getParent only displays root
                            setText("D:/");
                        } else {
                            setText(item.getName());
                        }
                    }
                };
            }
        });


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
    //Cellfactory customizes rendering of cells in a ListView, TableView, TreeView

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

// - - - - - - - - - - - - - - - - - - - - - PlaybackController - - - - - - - - - - - - - - - - - - - - - - - - -

    @FXML
    private void clickPlay() {
        //String uriString = new File("D:\\Musikmainaug2019\\Aku no Hana\\\uD83D\uDC40 Zankyou no Hana.mp3").toURI().toString();
        if (selectedSong == null) {
            System.out.println("No song selected.");
            return;
        }

        //todo: morph play symbol into pause symbol during playback
//currentSong = player;
//player = new MediaPlayer(new Media(new File (selectedSong.getPath()).toURI().toString()));
        if (player == null) {

            player = new MediaPlayer(new Media(new File (selectedSong.getPath()).toURI().toString()));
            if(selectedSong != null) {
                player.play();
                playingList.add(selectedSong);
                System.out.println("Playing: " + selectedSong.getTitle());
            }
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

    @FXML void clickPrevious(){

    }

    @FXML void clickNext(){

    }


// - - - - - - - - - - - - - - - - - - - - - TreeViewSelectionController - - - - - - - - - - - - - - - - - - - - - - - - -
//done: Nach mp3 Dateien filtern, möglichst vor dem ersten Ladeprozess
//done: Dateipfade im TreeView auf Ordnernamen reduzieren Cellfactory?
//done: Dateipfade in Titelspalte auf Dateinamen reduzieren Cellfactory? Cut/Trim Methode?
    @FXML
    private void selectTreeItem() {
        TreeItem<File> item = folderTreeView.getSelectionModel().getSelectedItem();
        //Song song = centerTableView.getSelectionModel().getSelectedItem();
        System.out.println();
//        if (folderTreeView.getSelectionModel().getSelectedItem() != item){
            centerList.clear();
//        }
        if (item != null) {
            File data = item.getValue();
            System.out.println("Selected: "+data);
            if (data.isDirectory()){
                //listFiles akzeptiert in () einen FileFilter mit der Signatur (File, String)
                File[] files = data.listFiles(
                        (_, str) -> str.toLowerCase().endsWith(".mp3")
                        //new FilenameFilter(){
                        //@Override
                        //public boolean accept(File dir, String name) {
                        //  return name.toLowerCase().endsWith(".mp3");
                        //}});
                );
                //assert files != null;
                if (files != null) {
                    for (var f : files) {
                        Song song = new Song(f.getAbsolutePath(), null);
                        centerList.add(song);
                    }
                }

            }
            centerTableView.setItems(centerList);
            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
        }
    }


    // - - - - - - - - - - - - - - - - - - - - - TableViewSelectionController - - - - - - - - - - - - - - - - - - - - - - - - -

    //TreeItem<File> item = folderTreeView.getSelectionModel().getSelectedItem();


// Song song = new Song(f.getAbsolutePath());
//                        centerList.add(song);
    @FXML
    private void selectTableItem() {
        Song song = centerTableView.getSelectionModel().getSelectedItem();
        System.out.println("Selected: "+song.getTitle());
        selectedSong = centerTableView.getSelectionModel().getSelectedItem();
        playingList.add(song);
        playingTableView.setItems(playingList);
        queueTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));



    }

    @FXML
    private void showCenterContextMenu() {
        centerTableView.setOnContextMenuRequested(e -> {
        ContextMenu contextmenu = new ContextMenu();
                });


                //e ->
                //.show(e.getScreenX(), e.getScreenY()));
    }





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

