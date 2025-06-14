package org.example.musikplayer_doit.services;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

public class FolderScanner {
    //Queue<File> startFolders = new java.util.LinkedList<>(Arrays.stream(files).toList());


    public static Map<String, FolderInfo> checkAllMp3Map(File[] files, int maxDepth) {

        Map<String, FolderInfo> mp3FolderMap = new ConcurrentHashMap<>(); //falls Multithread besser ConcurrentHashMap
        Queue<File> queue = new ConcurrentLinkedQueue<>(Arrays.asList(files));
        int currentDepth = 0;
        //Deque<String> allMp3Folders = new LinkedList<>();
        Set<String> searched = ConcurrentHashMap.newKeySet();

        //Queue = Festplatten
        while (!queue.isEmpty() && currentDepth <= maxDepth) {
            int queueSize = queue.size();
            for (int i = 0; i < queueSize; i++) {
                String canonicalPath;
                File folder = queue.poll();
                if (folder == null) continue;
                try {
                    canonicalPath = folder.getCanonicalPath();
                } catch (Exception e){
                    System.err.println("Could not get canonicalPath, potential missing paths");
                    continue;
                }
                String lowerPath = canonicalPath.toLowerCase();
                if (lowerPath.contains("\\windows") ||
                    lowerPath.contains("program files") ||
                    lowerPath.contains("programme") ||
                    lowerPath.contains("$recycle.bin")) {
                    System.out.println("Skipping Windows or System Directory " + canonicalPath);
                    continue;
                }
                if (!searched.add(canonicalPath)){
                    System.out.println(LocalDateTime.now()+"> Path already searched, skipping directory "+canonicalPath);
                    continue;
                }
                System.out.println("added to set: "+canonicalPath);
                mp3FolderMap.put(folder.getAbsolutePath(), new FolderInfo(folder.getParent(), containsMP3Files(folder)));
                File[] subFolders = folder.listFiles(File::isDirectory);
                if (subFolders != null) {
                    queue.addAll(Arrays.asList(subFolders));
                }
            }
            currentDepth++;
        }
        return mp3FolderMap;
    }


    //public class PathFilter {
    //
    //    private static final List<String> CONTAINS_KEYWORDS = List.of(
    //        "windows", "\\program", "intellij", "xampp", "c:\\perflogs", "c:\\system volume information",
    //        "appdata", "vs code", "vscode", ".code", "system volume information", "sicherung", "gradle",
    //        "shader", "web_", "$windows", "nvidia", "plugin", "extension", ".m2", "com.", "org.",
    //        "\\build", "repositor", "features", ".git", "\\git\\", "\\src\\", "licences", "savedata",
    //        "save data", "mitschriften", "$recycle.bin"
    //    );
    //
    //    private static final Set<String> EXCLUDED_FOLDERS = Set.of(
    //        "efi", "temp", "tmp", "$recycle.bin", "log", "logs", "intel", "tools", "boot", "support"
    //    );
    //
    //    public static boolean shouldSkip(String canonicalPath, String folderName) {
    //        String lowerPath = canonicalPath.toLowerCase();
    //        // Prüfen, ob der Pfad ein Keyword enthält
    //        for (String keyword : CONTAINS_KEYWORDS) {
    //            if (lowerPath.contains(keyword)) {
    //                return true;
    //            }
    //        }
    //        // Prüfen, ob der Ordnername exakt übereinstimmt
    //        return EXCLUDED_FOLDERS.contains(folderName.toLowerCase());
    //    }
    //}


    public static Set<String> checkAllMp3Set(Collection<File> driveSubFolders, int maxDepth) {
        Map<String, FolderInfo> mp3FolderMap = null;
        mp3FolderMap = new ConcurrentHashMap<>();
        Queue<File> queue = new ConcurrentLinkedQueue<>(driveSubFolders);
        int currentDepth = 0;
        //Deque<String> allMp3Folders = new LinkedList<>();
        Set<String> searched = ConcurrentHashMap.newKeySet();
        //Queue = FestplattenUnterordner
        while (!queue.isEmpty() && currentDepth <= maxDepth) {
            int queueSize = queue.size();
            System.err.println("queue size="+queueSize+queue.size());
            int loop = 0;
            for (int i = 0; i < queueSize; i++) {
                String canonicalPath;
                File folder = queue.poll();
                System.err.println("Gepollter Folder="+folder.getAbsolutePath());
                if (folder == null) {
                    System.err.println("Folder "+folder+" ist null und Schlaufenlauf wird übersprungen");
                    continue;
                }
                try {
                    canonicalPath = folder.getCanonicalPath();
                } catch (Exception e) {
                    System.err.println("Could not get canonicalPath of "+folder+", potential missing paths");
                    continue;
                }


                String lowerPath = canonicalPath.toLowerCase();
                System.out.println("Vergleiche Pfad mit Ausschlussliste: "+lowerPath);
                if (lowerPath.contains("windows") ||
                        lowerPath.contains("\\program") ||
                        lowerPath.contains("intellij") ||
                        lowerPath.contains("xampp") ||
                        lowerPath.contains("c:\\perflogs") ||
                        lowerPath.contains("c:\\system volume information") ||
                        lowerPath.contains("appdata") ||
                        lowerPath.contains("vs code") ||
                        lowerPath.contains("vscode") ||
                        lowerPath.contains(".code") ||
                        lowerPath.matches(".*visual\\s*studio.*") ||
                        lowerPath.contains("system volume information") ||
                        lowerPath.contains("sicherung") ||
                        lowerPath.contains("gradle") ||
                        lowerPath.contains("shader") ||
                        lowerPath.contains("web_") ||
                        lowerPath.contains("$windows") ||
                        lowerPath.contains("nvidia") ||
                        lowerPath.contains("plugin") ||
                        lowerPath.contains("extension") ||
                        lowerPath.contains(".m2") ||
                        lowerPath.contains("com.") ||
                        lowerPath.contains("org.") ||
                        lowerPath.contains("\\build") ||
                        lowerPath.contains("repositor") ||
                        lowerPath.contains("features") ||
                        lowerPath.contains(".git") ||
                        lowerPath.contains("\\git\\") ||
                        lowerPath.contains("\\src\\") ||
                        lowerPath.contains("licences") ||
                        lowerPath.contains("savedata") ||
                        lowerPath.contains("save data") ||
                        lowerPath.contains("mitschriften") ||
                        lowerPath.contains("recycle") ||
                        lowerPath.contains("$w") ||
                        lowerPath.contains("$recycle.bin")) {
                    System.err.println("Skipping Windows or System Directory " + canonicalPath + "aktueller Thread=" +Thread.currentThread().getName());
                    continue;
                }

                Set<String> excludedFolders = Set.of("tmp", "recycle", "logs", "intellij", "\\boot", "\\support", "windows", "\\program", "intellij", "xampp", "c:\\perflogs", "c:\\system volume information", "appdata", "vs code", "vscode", ".code", "visual studio", "visualstudio", "system volume information", "sicherung", "gradle", "shader", "web_", "$windows", "nvidia", "plugin", "extensions", ".m2", "com.", "org.", "\\build", "repositor", "features", ".git", "\\git\\", "\\src", "licences", "savedata", "save data", "mitschriften", "$w");
                if (excludedFolders.contains(folder.getName().toLowerCase())) {
                    System.err.println("Skipping exact match for folder: " + folder.getAbsolutePath()+ "aktueller Thread=" +Thread.currentThread().getName());
                    continue;
                }
                if (!searched.add(canonicalPath)) {
                    System.err.println("Path already searched, skipping directory " + canonicalPath+ "aktueller Thread=" +Thread.currentThread().getName());
                    continue;
                }
                if (containsMP3Files(folder)) {
                    mp3FolderMap.put(folder.getAbsolutePath(), new FolderInfo(folder.getParent(), true));
                }
                File[] subfolders = folder.listFiles(File::isDirectory);
                System.err.println("Durchsuche Inhalte von Ordner "+folder.getAbsolutePath());
                if (subfolders != null) {
                    System.out.println("füge alle Inhalte zur Queue hinzu: ");
                    for (var f : subfolders) {
                        System.out.println("Inhalt subfolders: "+f);
                        String canonicalSubfolder;
                        try {
                            canonicalSubfolder = f.getCanonicalPath();
                        } catch (Exception e) {
                            continue;
                        }
                        if (searched.contains(canonicalSubfolder)) {
                            continue;
                        }
                        System.out.println("added to queue: "+f);
                        queue.add(f);
                    }
                    //queue.addAll(Arrays.asList(subfolders));


                }
                loop++;
                System.out.println("Loop: "+loop);
                System.out.println(loop+"von->Thread " + Thread.currentThread().getName() + " scanning: " + canonicalPath + " at depth " + currentDepth);
            }
            System.out.println("currentDepth=" + currentDepth+" in Thread="+Thread.currentThread().getName()+", active Count: "+Thread.activeCount()+". Thread State: "+Thread.currentThread().getState());
            currentDepth++;
        }
//        for (var s : searched){
//            System.out.println("Final searched list: "+s);
//        }


        for (var e : mp3FolderMap.keySet()){
            System.err.println("contents set: "+e+"\n");
        }
        return mp3FolderMap.keySet();
    }





//        for (var f : drives){
//            File[] subFolders = f.listFiles(File::isDirectory);
//            if (subFolders != null){
//                queue.addAll(Arrays.asList(subFolders));
//            }
//        }
//        //Queue enthält nun obere Verzeichnisebene unter Festplatten
//        while (!queue.isEmpty()){
//            File[] arrayToScan = queue.poll().listFiles(File::isDirectory);
//            if (arrayToScan != null) {
//                for (var f : arrayToScan){
//                    queue.addAll(Arrays.asList(Objects.requireNonNull(f.listFiles(File::isDirectory))));
//                    if (containsMP3Files(Objects.requireNonNull(f))) {
//                        mp3FolderMap.put(f.getAbsolutePath(), new FolderInfo(f.getParent(), true));
//                    }
//                }
//            }
//        }
//        return mp3FolderMap;
//}

//        int currentDepth = 0;
//        while (!queue.isEmpty() && currentDepth<maxDepth) {
//            int queueSize = queue.size();
//            for (int i = 0; i < queueSize; i++){
//                File currentFolder = queue.poll();
//                if (currentFolder != null && currentFolder.isDirectory()){
//                    boolean hasMp3 = containsMP3Files(currentFolder);
//                    String path = currentFolder.getAbsolutePath();
//                    mp3FolderMap.put(path, hasMp3);
//                    File[] subFolders = currentFolder.listFiles();
//                    if (subFolders != null){
//                        queue.addAll(Arrays.asList(subFolders));
//                        //alternativ via for-each
//                    }
//                }
//            }
//            currentDepth++;
//        }
//        Set<String> pathList;
//        return mp3FolderMap;



//Mein Gedanke ist nun folgender:
//1) Ich erstelle für jeden bestätigten MP3-Ordner in einer rekursiven Schleife eine Deque mit allen Elternverzeichnissen.
//2) Ich konsolidiere danach alle Deques in ein einziges Set, welches duplikate Pfade entfernen sollte.
//3) Ich erstelle erneut eine angebrachte Datenstruktur (nicht unbedingt Deque) mit sämtlichen Elternverzeichnissen aller Musikordner
    public static Set<String> getUniqueParentsSet (Collection<String> paths){
        Set<String> uniqueParents = new ConcurrentSkipListSet<>();
        for (var p : paths){
            System.err.println("Scanner-paths content: "+p);
            File file = new File(p);
            File parent = file.getParentFile();
            while (parent != null){
                uniqueParents.add(parent.getAbsolutePath());
                parent = parent.getParentFile();
            }
        }
        for (var e : uniqueParents){
            System.out.println("- - - All Unique Parents: "+e+"\n");
        }
        return uniqueParents;
    }

//    public static <C extends Collection<String>> Set<String> getUniqueParentsSet(C paths) {
//        Set<String> parents = new HashSet<>();
//        for (String path : paths) {
//            File file = new File(path);
//            File parent = file.getParentFile();
//            while (parent != null) {
//                parents.add(parent.getAbsolutePath());
//                parent = parent.getParentFile();
//            }
//        }
//        return parents;
//    }

//4) Ich füge diese erneut in eine Set hinzu, um duplikate Elternverzeichnisse zu entfernen. In deinen beispielhaften Verzeichnissen
// wäre z. B. zweimal das Verzeichnis Musik\Pop\Madonna vorhanden, oder wahrscheinlich 5x oder mehr das Verzeichnis Musik\Pop und 10x das Verzeichnis Musik\
//5) Dieses Set sortiere ich dann nach Länge der Einträge. Da Ordner verschieden lange Dateinamen haben können, sollte ich
// vermutlich nach eindeutigen Anzeigern der Länge suchen, wie z.B. die Anzahl der Schrägstriche im Dateipfad.
    /**
     * the array 'files' represents the first layer of *subdirectories* underneath the system drives
     * @param files
     */
    public static Map<String, FolderInfo> checkMp3(File[] files){
        Queue<File> queue = new LinkedList<>();
        Map<String, FolderInfo> allMp3Folders = new ConcurrentHashMap<>();
        for (var f : files){
            queue.add(f);
        }
        while (!queue.isEmpty()){
            File currentFile = queue.poll();
            queue.addAll(Arrays.asList(Objects.requireNonNull(currentFile.listFiles(File::isDirectory))));
            boolean containsMp3 = containsMP3Files(currentFile);
            FolderInfo folderInfo = new FolderInfo(currentFile.getParent(), containsMp3);
            allMp3Folders.put(currentFile.getPath(), folderInfo);
        }

        return allMp3Folders;
    }

    private static boolean containsMP3Files(File directory) {
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

}
