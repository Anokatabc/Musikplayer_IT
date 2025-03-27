package org.example.musikplayer_doit.controller;
//test 12356
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.example.musikplayer_doit.model.Song;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.*;

//ToDo: Unverständlichen Code entfernen und schrittweise rekonstruieren.


public class Controller implements Initializable {

    MediaPlayer player;

    @FXML
    private TreeView<File> folderTreeView;
    @FXML
    private TableView<Song> centerTableView;
    @FXML
    private TableView<File> listTableView;
    @FXML
    TableColumn<File, String> titleColumn; //TableColumn<S, T> S=Datentyp von TableView, T=Datentyp in Spalte

    private ExecutorService executorService;
    public ObservableList<Song> centerList = FXCollections.observableArrayList();

    // - - - - - - - - - - - - - - - - - - - - - PlaybackController - - - - - - - - - - - - - - - - - - - - - - - - -

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

    @FXML void clickPrevious(){
        if (player != null){
            //playlist.last
        }
    }

    @FXML void clickNext(){
        if (player != null){
            //playlist.next
        }
    }

    // - - - - - - - - - - - - - - - - - - - - - TreeViewController - - - - - - - - - - - - - - - - - - - - - - - - -

    // FileTreeCellFactory

    // Multithreading: https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ForkJoinPool.html
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        File[] roots = File.listRoots();
        TreeItem<File> rootItem = new TreeItem<>(new File("My Computer"));
        folderTreeView.setRoot(rootItem);

        if (roots != null) {
            for (File root : roots) {
                TreeItem<File> driveItem = new TreeItem<>(root);
                rootItem.getChildren().add(driveItem);
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() {
                        ForkJoinPool pool = new ForkJoinPool();
                        pool.invoke(new CreateTreeTask(root, driveItem));
                        return null;
                    }
                };

                task.setOnSucceeded(event -> System.out.println("Tree creation completed for drive: " + root));
                task.setOnFailed(event -> System.err.println("Tree creation failed for drive: " + root + " - " + task.getException()));

                new Thread(task).start();
            }
        }
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
                    Platform.runLater(() -> parentItem.getChildren().add(item));
                    CreateTreeTask task = new CreateTreeTask(f, item);
                    task.fork();

                }
            }
        }
    }

    // - - - - - - - - - - - - - - - - - - - - - CenterTableViewController / MediaSelectionController - - - - - - - - - - - - - - - - - - - - - - - - -
//ToDo: Nach mp3 Dateien filtern, möglichst vor dem ersten Ladeprozess
//ToDo: Dateipfade im TreeView auf Ordnernamen reduzieren Cellfactory?
//ToDo: Dateipfade in Titelspalte auf Dateinamen reduzieren Cellfactory? Cut/Trim Methode?
    @FXML
    private void selectItem() {
        TreeItem<File> item = folderTreeView.getSelectionModel().getSelectedItem();
//        if (folderTreeView.getSelectionModel().getSelectedItem() != item){
            centerList.clear();
//        }
        if (item != null) {
            //File data = item.getValue();
            File data = item.getValue();
            System.out.println("Selected: "+data);
            if (data.isDirectory()){
                File[] files = data.listFiles();
                for (var f : files){

                    Song song = new Song(f.getAbsolutePath());
                    centerList.add (song);
            }

            }
            centerTableView.setItems(centerList);
            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));


//            loadFilesFromPath(data);
//            centerTableView.setItems(centerList);
//            titleColumn.getTableView();
        }
    }

//    public ArrayList <Song> createCenterView () {
//        ArrayList <Song> asd;
//        return asd;
//    }


    private void loadFilesFromPath(File folder){
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



}





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

