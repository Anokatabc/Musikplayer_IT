package org.example.musikplayer_doit.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;



public class Controller {

@FXML
private TreeView<File> folderTreeView;

    public void clickPlay() {
        String uriString = new File("D:\\Musikmainaug2019\\Aku no Hana\\\uD83D\uDC40 Zankyou no Hana.mp3").toURI().toString();
        MediaPlayer player = new MediaPlayer(new Media(uriString));
        player.play();
        System.out.println("Playing: " + uriString);

    }

//    @FXML
//    public void initialize() {
//        TreeItem<File> rootItem = new TreeItem<>(new File ("My Computer"));
//        //rootItem.setExpanded(true);
//        folderTreeView.setRoot(rootItem);
//        File[] rootDrives = File.listRoots();
//        if (rootDrives != null) {
//            for (var rootDrive : rootDrives) {
//                TreeItem<File> driveItem = new TreeItem<>(rootDrive);
//                rootItem.getChildren().add(driveItem);
//                createTree(rootDrive, driveItem);
//            }
//        }
//    }

@FXML
public void initialize() {
    File rootFile = new File("D:\\");
    TreeItem<File> rootItem = new TreeItem<>(rootFile);
    //rootItem.setExpanded(true);
    folderTreeView.setRoot(rootItem);
    createTree(rootFile, rootItem);
}

    //alternativ: populateTree
    private void createTree(File file, TreeItem<File> parentItem) {
        File[] files = file.listFiles();
        if (files != null) {
            for (var f : files) {
                TreeItem<File> item = new TreeItem<>(f);
                parentItem.getChildren().add(item);
                if (f.isDirectory()) {
                    createTree(f, item);
                }
            }
        }
    }


}
