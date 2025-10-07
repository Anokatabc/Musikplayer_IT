package org.example.musikplayer_doit.services;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;
import org.example.musikplayer_doit.model.Mp3FolderInfo;

import java.io.*;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

public class TreeViewBuilder {
    private final TreeView<Path> folderTreeView;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public TreeViewBuilder (TreeView<Path> folderTreeView){
        this.folderTreeView = folderTreeView;
    }

    public void initializeTreeView() {
        folderTreeView.setMouseTransparent(true);
        folderTreeView.setOpacity(0.5);
        //folderTreeView.setShowRoot(false);
    }

    public void receive(Mp3FolderInfo mp3FolderInfo) {
        Map<Path, Integer> map = mp3FolderInfo.folderMp3Count();
        Set<Path> set = mp3FolderInfo.mp3Paths();
        int count = mp3FolderInfo.mp3Count();
        System.out.println("Total number of mp3 files found: "+count);
        build(set);

    }

        public void buildTree(File[] directoryArray){
        Mp3Scanner.collectMp3Folders(directoryArray, this::receive);
    }

    private void build(Set<Path> uniquePaths){
        Task<TreeItem<Path>> task = new Task<>() {
            @Override
            protected TreeItem<Path> call() throws Exception {
                Set<Path> allUniquePaths = Mp3Scanner.getUniqueParentsSet(uniquePaths);
                List<Path> allSortedPaths = sortPerLengthAndName(allUniquePaths);

                Map<Path, TreeItem<Path>> treeMap = new HashMap<>();
                Path rootPath = new File("My Computer").toPath();
                TreeItem<Path> rootItem = new TreeItem<>(rootPath);
                treeMap.put(rootPath, rootItem);

                for (Path path : allSortedPaths){
                    //todo: an einem späteren Punkt mit Paths statt Paths weiterarbeiten, und *alles* auf Paths aktualisieren
                    //todo: Elternpfade und Einzigartigkeit werden später durch Map geregelt. Ggf. vereinfachen
                    TreeItem<Path> treeItem = treeMap.computeIfAbsent(path, p -> new TreeItem<>(p));
                    Path parentPath = path.getParent();
                    TreeItem<Path> parentItem;
                    if (parentPath != null){
                        parentItem = treeMap.computeIfAbsent(parentPath, p -> new TreeItem<>());
                    } else {
                        parentItem = rootItem;
                    }
                    if (!parentItem.getChildren().contains(treeItem)) {
                        parentItem.getChildren().add(treeItem);
                    }
                }
                return rootItem;
            }
        };
        task.setOnSucceeded((worker) -> {
            Set<Path> expandedPaths = new HashSet<>();
            collectExpandedItems(folderTreeView.getRoot(), expandedPaths);
            TreeItem<Path> rootItem = task.getValue();
            folderTreeView.setRoot(rootItem);

            System.out.println(">>>>>-----TreeView erfolgreich übergeben-----<<<<<");
            restoreExpandedItems(rootItem, expandedPaths);
            folderTreeView.setMouseTransparent(false);
            folderTreeView.setOpacity(1);

            //rootItem.setExpanded(false);
            //todo: verschiedene Grafiken (oder zB. opacity?) für treeItems mit / ohne Musikordnern anzeigen
        });

        Thread thread = new Thread(task);
        thread.start();
    }

    private void collectExpandedItems(TreeItem<Path> item, Set<Path> expanded) {
        if (item != null) {
            if (item.isExpanded() && item.getValue() != null) {
                expanded.add(item.getValue());
            }
            for (var child : item.getChildren()) {
                collectExpandedItems(child, expanded);
                System.out.println("Collect expanded children");
            }
        }
    }

    private void restoreExpandedItems(TreeItem<Path> item, Set<Path> expanded) {
        if (item != null){
            if (item.getValue() != null && expanded.contains(item.getValue())) {
                item.setExpanded(true);
            }
            for (TreeItem<Path> child : item.getChildren()) {
                restoreExpandedItems(child, expanded);
            }
        }
    }

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

    private List<Path> sortPerLengthAndName(Collection<Path> unsortedList){
        List<Path> sortedList = new ArrayList<>(unsortedList);
        sortedList.sort(new SortBackslashThenAlphabetical());
        return sortedList;
    }

    private Set<String> checkAllMp3(File[] files, int currentDepth, int maxDepth){
        for (var file : files){
            boolean containsMp3 = containsMP3Files(file);
            File[] folders = file.listFiles();
            currentDepth++;
        }

        Map<String, Boolean> mp3FolderMap = new HashMap<>(); //ggf. zur Darstellung von nicht-Musikordnern?
        Queue<File> queue = new LinkedList<>(Arrays.asList(files));

            while (!queue.isEmpty() && currentDepth<maxDepth) {
                int queueSize = queue.size();
                for (int i = 0; i < queueSize; i++){
                    File currentFolder = queue.poll();
                    if (currentFolder != null && currentFolder.isDirectory()){
                        boolean hasMp3 = containsMP3Files(currentFolder);
                        String path = currentFolder.getAbsolutePath();
                        mp3FolderMap.put(path, hasMp3);
                        File[] subFolders = currentFolder.listFiles();
                        if (subFolders != null){
                            queue.addAll(Arrays.asList(subFolders));
                        }
                    }
                }
                currentDepth++;
            }
        return null;
    }

    //Cellfactory customizes default rendering of cells in a ListView, TableView, TreeView
    //item und empty werden von JavaFX vorgegeben
    //setRowFactory nimmt Callback an, welcher eine TableRow erstellt.
    // -> TableView Eingabe, TableRow Ausgabe => new TableRow.
    // heißt: tview -> (wird zu) new TableRow<>() { ... (Körperinhalt der anonymen Klasse = was überschrieben wird.

    //Mit anderen Worten: Man möchte sich die Methode updateItem zunutze machen, um die Zelle mit neuen Eigenschaften zu aktualisieren.
    // Damit möchte man aber nicht alle Basiseigenschaften überschreiben, sondern nur die einzelnen Aspekte, die man im Körper der
    // neuen anonymen Klasse definiert. Zum Beispiel hier möchte man nur den Style setzen, und den Rest der Eigenschaften unangetastet
    // lassen. Darum wird die nächste valide Implementierung der Methode in einer Elternklasse aufgerufen, um diese Kernfunktionen zu
    // gewährleisten.

    public void applyCellFactory(){
        folderTreeView.setCellFactory(new Callback<TreeView<Path>, TreeCell<Path>>() {
            @Override
            public TreeCell<Path> call(TreeView<Path> folderTreevie) {
                return new TreeCell<>() {
                    @Override
                    protected void updateItem(Path item, boolean empty){
                        super.updateItem(item, empty);
                        if (empty || item == null){
                            setText(null);
                            setGraphic(null);
                        } else if (item.getFileName() == null){
                            setText(item.toString());
                        } else {
                            setText(item.getFileName().toString());
                        }
                    }
                };
            }
        });
    }

}