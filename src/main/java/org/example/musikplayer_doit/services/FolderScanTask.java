package org.example.musikplayer_doit.services;

import javafx.scene.control.TreeItem;
import java.io.File;
import java.util.concurrent.RecursiveAction;

public class FolderScanTask extends RecursiveAction {
    private final File directory;
    private final TreeItem<File> parentItem;

    public FolderScanTask(File directory, TreeItem<File> parentItem) {
        this.directory = directory;
        this.parentItem = parentItem;
    }
private long startTime = 0;

    @Override
    protected void compute() {
        File[] files = directory.listFiles(File::isDirectory);

        if (files != null) {
            startTime = System.currentTimeMillis();
            for (File file : files) {
                TreeItem<File> childItem = new TreeItem<>(file);
                parentItem.getChildren().add(childItem);

                // Erstelle eine neue Aufgabe für jedes Unterverzeichnis
                FolderScanTask task = new FolderScanTask(file, childItem);
                task.fork(); // Aufgabe parallel ausführen
            }
        }


    }
}