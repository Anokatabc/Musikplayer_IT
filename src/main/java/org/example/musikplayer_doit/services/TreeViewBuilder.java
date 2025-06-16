package org.example.musikplayer_doit.services;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class TreeViewBuilder {

    private int loopCount;
    private long startTime;
    private boolean isFirstRound;
    private final TreeView<File> folderTreeView;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public TreeViewBuilder (TreeView<File> folderTreeView){
        this.folderTreeView = folderTreeView;
    }

    public void initializeTreeView() {
        folderTreeView.setMouseTransparent(true);
        folderTreeView.setOpacity(0.5);
        File rootFile = new File("My Computer");
        TreeItem<File> rootItem = new TreeItem<>(rootFile);
        folderTreeView.setRoot(rootItem);

        //rootItem.setExpanded(true);
        //folderTreeView.setShowRoot(true);

        File[] drives = File.listRoots();

        //File -> Path
        //listFiles() -> Files.newDirectoryStream(path) bzw. Files.walk(path, depth)
        //Filter: PathMatcher oder Lambda
        //File::isDirectory -> Files.isDirectory(path)
        //viele IOExceptions catchen
        //unchecked AccessDeniedException
        //Beispiel:
        //import java.nio.file.*;
        //import java.io.IOException;
        //
        //Queue<Path> queue = new LinkedList<>();
        //Set<Path> searched = new HashSet<>();
        //queue.add(startPath);
        //
        //while (!queue.isEmpty()) {
        //    Path folder = queue.poll();
        //    if (!Files.isDirectory(folder) || !searched.add(folder)) continue;
        //    try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
        //        for (Path entry : stream) {
        //            if (Files.isDirectory(entry)) {
        //                queue.add(entry);
        //            }
        //            if (entry.toString().toLowerCase().endsWith(".mp3")) {
        //                // MP3 gefunden
        //            }
        //        }
        //    } catch (IOException e) {
        //        // Fehlerbehandlung
        //    }
        //}
        for (var d : drives){

            TreeItem<File> treeItem = new TreeItem<>(d);
            rootItem.getChildren().add(treeItem);
        }

        Set<Path> allSortedPaths = Mp3Scanner.prepareMp3Folders(drives);
//        for (var e : allSortedPaths){
//            System.out.println("Sortierter Eintrag: "+e);
//        }
//        for (int i=0;i<allSortedPaths.backslashCount;i++){
//            //create file array for each level? think
//        }
    }

    public static void buildTree(File[] paths){


    }

//    private List<Path> prepareMp3Folderssss(File[] drives) {
//        Set<Path> allMp3Folders = new ConcurrentSkipListSet<>();
//        Set<Path> allPaths = new ConcurrentSkipListSet<>();
//
//        List<File> allSubFolders = new ArrayList<>();
//        Path d = new File("asd").toPath();

//        for (File dr : drives){
//            //try (var stream = Files.newDirectoryStream(Path.of(d.toURI()))) {
//            try (var stream = Files.newDirectoryStream(dr.)) {
//                for (Path path : stream) {
//                    if (Files.isDirectory(path)) {
//                        allSubFolders.add(path.toFile());
//                        System.out.println("zu subFolders hinzugefügt: " + path.toAbsolutePath());
//                    }
//                }
//            } catch (IOException e) {
//                System.err.println(e.getMessage());
//            }
//        }
        //Ordner mit Festplattenunterordnern. Next: jeden durchlaufen und Musikordner finden

//        int size = allSubFolders.size(); //60, C:, D: und E: Folder
//        CountDownLatch latch = new CountDownLatch(size);
//        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//
//        System.out.println("Anzahl der zu scannenden Unterordner: " + size);
//
//        for (var subFolder : allSubFolders) {
//            executor.submit(() -> {
//                System.err.println("Pfad des bearbeiteten subFolders"+subFolder.getAbsolutePath()+". Listenindex von 60="+allSubFolders.indexOf(subFolder));
//                try {
//                    //Set<String> result = FolderScanner.checkAllMp3Set(subFolder.listFiles(File::isDirectory), 10);
//                    Set<Path> result = FolderScanner.checkAllMp3Set(allSubFolders, 10);
//                    allMp3Folders.addAll(result);
//                    for (var i : result){
//                        System.err.println("result content: "+i);
//                    }
//                    for (var i : allMp3Folders){
//                        System.err.println("allMp3Folders content: "+i);
//                    }
//                    Set<String> parentPaths = FolderScanner.getUniqueParentsSet(result);
//                    allPaths.addAll(parentPaths);
//                } finally {
//                    latch.countDown();
//                    System.err.println("[" + LocalTime.now().format(formatter) + "] "+"Latch="+(int) latch.getCount());
//                    System.err.println("Latch="+latch.toString());
//                }
//            });
//        }
//        executor.shutdown();
//        try {
//            latch.await();
//            System.err.println("Latch wartet: "+latch.getCount());
//        } catch (InterruptedException e) {
//            System.err.println("Thread wurde unterbrochen, ehe alle Tasks beendet werden konnten.");
//            Thread.currentThread().interrupt();
//        }
//        System.err.println("Executor successfully shutdown");
//
//        allPaths.addAll(allMp3Folders);
//        return sortPerLengthAndName(allPaths);
    //}

    private List<String> prepareMp3Folderss(File[] drives) {
        Set<String> allMp3Folders = new ConcurrentSkipListSet<>();
        Set<String> allPaths = new ConcurrentSkipListSet<>();

        List<File> allSubFolders = new ArrayList<>();
        for (var d : drives){
            File[] subFolders = d.listFiles(File::isDirectory);
            if (subFolders != null) Collections.addAll(allSubFolders, subFolders);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        int size = allSubFolders.size();
        CountDownLatch latch = new CountDownLatch(size);
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        System.out.println("Anzahl der zu scannenden Unterordner: " + size);

        for (var subFolder : allSubFolders) {
            executor.submit(() -> {
                try {
                    //Set<String> result = FolderScanner.checkAllMp3Set(subFolder.listFiles(File::isDirectory), 10);
                    Set<String> result = FolderScanner.checkAllMp3Set(allSubFolders, 10);
                    allMp3Folders.addAll(result);
                    Set<String> parentPaths = FolderScanner.getUniqueParentsSet(result);
                    allPaths.addAll(parentPaths);
                } finally {
                    latch.countDown();
                    System.err.println("[" + LocalTime.now().format(formatter) + "] "+"Latch="+(int) latch.getCount());
                    System.err.println("Latch="+latch.toString());
                }
            });
        }
        executor.shutdown();
        try {
            latch.await();
            System.err.println("Latch wartet: "+latch.getCount());
        } catch (InterruptedException e) {
            System.err.println("Thread wurde unterbrochen, ehe alle Tasks beendet werden konnten.");
            Thread.currentThread().interrupt();
        }
        System.err.println("Executor successfully shutdown");

        allPaths.addAll(allMp3Folders);
        return sortPerLengthAndName(allPaths);
    }

    private List<String> prepareMp3Foldersss(File[] drives) {
        //alle Ordner mit MP3-Dateien
        Set<String> allMp3Folders = new ConcurrentSkipListSet<>();
        //alle Pfade inkl. sämtlichen Elternverzeichnissen der Musikordner
        Set<String> allPaths = new ConcurrentSkipListSet<>();

        //Size um die ~60
        int size = 0;
        int count = 0;
        for (var d : drives) {
            count++;
            File[] subFolders = d.listFiles(File::isDirectory);
            if (subFolders != null) {
                size += subFolders.length;
                for (var folder : subFolders){
                    System.out.println(count+") Gefundener Ordner: " + folder.getAbsolutePath());
                }
            }
        }
        count=0;

        CountDownLatch latch = new CountDownLatch(size);
        ExecutorService executor = Executors.newFixedThreadPool(size);
        System.out.println("Anzahl generierter Threads für die Bearbeitung=" + size);
        List<File> allSubFolders = new ArrayList<>();
        for (var d : drives){
            File[] sub = d.listFiles(File::isDirectory);
            if (sub != null) Collections.addAll(allSubFolders, sub);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        for (var d : allSubFolders) {
            //ThreadMonitor.startMonitoring();
            executor.submit(() -> {
                try {
                    Set<String> result = FolderScanner.checkAllMp3Set(allSubFolders, 10);
                    synchronized (allMp3Folders) {
                        allMp3Folders.addAll(result);
                    }
                    Set<String> parentPaths = FolderScanner.getUniqueParentsSet(result);
                    synchronized (allPaths) {
                        allPaths.addAll(parentPaths);
                    }
                } finally {
                    latch.countDown();
                    System.err.println("[" + LocalTime.now().format(formatter) + "] "+"Latch="+(int) latch.getCount());
                    System.err.println("Latch="+latch.toString());
                }
            });
        }
        try {
            latch.await();
            System.err.println("Latch wartet: "+latch.getCount());
        } catch (InterruptedException e) {
            System.err.println("Thread wurde unterbrochen, ehe alle Tasks beendet werden konnten.");
            Thread.currentThread().interrupt();
        }
        System.err.println("Executor successfully shutdown");
        executor.shutdown();
        allPaths.addAll(allMp3Folders);
        return sortPerLengthAndName(allPaths);
    }

    //
    //        for (var d : drives) {
    //            File[] subDirectories = d.listFiles(File::isDirectory);
    //            if (subDirectories == null) {
    //                System.err.println("Subdirectories of drive " + d + " empty.");
    //                latch.countDown();
    //                continue;
    //            }
    //            for (var f : subDirectories) {
    //                //HashMap<File, Boolean> check = new HashMap<>();
    //                //check.put(f, false);
    //                remainingTasks.incrementAndGet();
    //                Task<Set<String>> ta = new Task<>() {
    //                    @Override
    //                    protected Set<String> call() {
    //                        return FolderScanner.checkAllMp3Set(new File[]{f}, 10);
    //                    }
    //                };
    //                ta.setOnSucceeded((WorkerStateEvent workerStateEvent) -> {
    //                    //List<String> sortedList = new ArrayList<>();
    //                    //check.replace(f, false, true);
    //                    //Set<File> set = check.keySet();
    //                    //for ( var key : set ){
    //                    //File folder = key;
    //                    //boolean value = check.get(key);
    //                    //if (value){
    //                    //System.out.println(folder+" has been fully checked");
    //                    //}
    //                    System.out.println("Tasks running: "+remainingTasks.get());
    //                    try{
    //                        allMp3Files.addAll(ta.getValue());
    //                    } finally {
    //                        if (remainingTasks.decrementAndGet() == 0) {
    //                            latch.countDown();
    //                            System.out.println(latch.toString());
    //                        }
    //                    }
    //                });
    //                ta.setOnFailed((WorkerStateEvent workerStateEvent) -> {
    //                    try {
    //                        System.err.println("Task failed for " + f.getAbsolutePath());
    //                    } finally {
    //                        if (remainingTasks.decrementAndGet() == 0) {
    //                            latch.countDown();
    //                        }
    //                    }
    //                });
    //                Thread thread = new Thread(ta);
    //                thread.setUncaughtExceptionHandler((t, e) -> {
    //                    System.err.println("Exception in FolderScanner thread " + t.getName() + ": " + e.getMessage());
    //                    e.printStackTrace();
    //                });
    //                thread.start();
    //                try{
    //                    thread.join(10000); // Timeout von 10 Sekunden
    //                } catch (InterruptedException e){
    //                    System.err.println("Thread "+thread.getName()+" wurde unterbrochen - "+e.getMessage());
    //                }
    //                if (thread.isAlive()) {
    //                    System.err.println("Thread " + thread.getName() + " hängt und wird abgebrochen.");
    //                    thread.interrupt();
    //                }
    //            }
    //        }
    //
    //        try {
    //            latch.await(); // Warten, bis alle Tasks abgeschlossen sind
    //        } catch (InterruptedException e) {
    //            System.err.println("Thread interrupted while waiting for tasks to complete.");
    //            Thread.currentThread().interrupt();
    //        }
    //
    //        // Sortieren und Rückgabe der Liste
    //        Set<String> allMp3Paths = FolderScanner.getUniqueParentsSet(allMp3Files);
    //        return sortPerLengthAndName(allMp3Paths);
    //

    private void createTree(Collection<String> list, TreeItem<File> rootItem){
        if (!isSorted(list, new SortBackslashThenAlphabetical(), 25)){
            System.err.println("Liste ist nicht korrekt sortiert!");
        } else {
            System.out.println("Liste ist korrekt sortiert");
        }
        for (var item : list){
            System.out.println("Eintrag in vollständiger und sortierter Pfadliste: "+item);
        }



                //Jeden Listeneintrag als File verpacken und Bezeichnung auf eigenen Ordnernamen einschränken,
                // dann daraus ein TreeItem erstellen, um es geregelt dem TreeView hinzuzufügen.
//                File file = new File(e);
//                File parentFile = new File(file.getParent());
//                TreeItem<File> parentItem = new TreeItem<>(new File(file.getName()));
//                TreeItem<File> treeItem = new TreeItem<>(new File(file.getName()));
//                Platform.runLater(() -> {
//                    if (rootItem.getChildren().contains(treeItem))
//                        rootItem.getChildren().add(treeItem);
//                });

                //parentItem = new TreeItem<>(new File(parentFile.getName()));
                //if (!parentItem.getChildren().contains(treeItem) && parentItem.getChildren().contains(parentItem)){
                //    parentItem.getChildren().add(treeItem);
                //    System.out.println("added to TreeView (presumably) "+treeItem);
                //}

//            folderTreeView.setMouseTransparent(false);
//            folderTreeView.setOpacity(1);

    }
    private void createTree(TreeItem<File> rootItem){
        //TreeItem<File> treeItem = new TreeItem<>(f);
        //parentItem.getChildren().add(treeItem);
        //createTree(f, treeItem);
    }

    private boolean isSorted(Collection<String> list, Comparator<String> comparator, int limit){
        int loopCount = 0;
        String previous = null;
        for (var e : list){
            if (previous != null){
                if (comparator.compare(previous, e) > 0){
                    return false;
                }
            }
            previous = e;
            loopCount++;
            if (loopCount >= limit) break;
        }
        return true;
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

    //ggf. Umschreiben um mit drives zu arbeiten. listFiles nur in FolderScanner laufen lassen.
    private void createTree2(File[] drives){

    }

    private List<String> sortPerLengthAndName(Collection<String> unsortedList){
        List<String> sortedList = new ArrayList<>(unsortedList);
        sortedList.sort(new SortBackslashThenAlphabetical());
        for (var c : sortedList){
            System.out.println("sortedList: "+c);
        }
        return sortedList;
    }

    private void createTreeOld(File file, TreeItem<File> parentItem, int currentDepth, int maxDepth) {
        if (currentDepth > maxDepth){
            System.out.println("Endlosschleife oder zu tiefe Ordnerverzweigung: Maximale Rekursionstiefe erreicht.");
            return;
        }
        File[] files = file.listFiles(File::isDirectory);
        for (var f : files) {
            if (containsMP3Files(f)) {
                TreeItem<File> treeItem = new TreeItem<>(f);
                parentItem.getChildren().add(treeItem);
                createTreeOld(f, treeItem, currentDepth + 1, maxDepth);
                loopCount++;
                System.out.println("Scanned: " + loopCount);
            }
        }

        if (isFirstRound) {
//            for (var f : files) {
//                loopCount++;
//                System.out.println("Scanned: " + loopCount);
//                File[] folders = checkAllMp3(f);
//            }

            //verstehe ich deine Logik richtig, dass anfänglich eine Queue aus allen startFolders erstellt wird, in der While-Schleife wird die Size der Queue als Integer gespeichert. Anschließend wird entsprechend der Länge der Queue eine Anzahl von Schleifen durchlaufen. In jeder Schleife wird der erste EIntrag der Queue gelöscht, auf Directory und MP3-Dateien geprüft. Jeder Ordner und Wahrheitswert wird in der Map gespeichert. Abschließend werden der leeren Queue sämtliche Unterordner hinzugefügt, und das Spiel wiederholt sich.
            //Dies ist eine Art Rekursion, die keine Methode erfordert, sondern einfach innerhalb einer Schleife stattfindet.
            //List<File> folders = checkAllMp3(files);
            Set<String> folders = checkAllMp3(files, 0, 10);
            //folders.add(;
            isFirstRound = false;
            
        };

        //aktuell: Ordner werden auf mp3 geprüft, wenn ja: hinzufügen.
        //ziel: jeder Unterordner soll auf mp3 geprüft werden
        //Set<String>
    }

    private Set<String> checkAllMp3(File[] files, int currentDepth, int maxDepth){
        for (var file : files){
            boolean containsMp3 = containsMP3Files(file);
            File[] folders = file.listFiles();
            currentDepth++;
        }
        //while
        //map
        //
        Map<String, Boolean> mp3FolderMap = new HashMap<>(); //public static, eigene Klasse
        //Queue<File> startFolders = new java.util.LinkedList<>(Arrays.stream(files).toList());
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
            Set<String> pathList;
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
    public void applyCellFactory() {
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

    //Markierung nicht mehr gebrauchter Werte für den Garbage Collector
    private void cleanup() {
        loopCount = 0;
        startTime = 0;
    }
}
