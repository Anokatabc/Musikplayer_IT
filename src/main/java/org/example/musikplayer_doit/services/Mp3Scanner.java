package org.example.musikplayer_doit.services;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class Mp3Scanner {

//    public static List<Path> prepareMp3Folders2(File[] drives) {
//        for (File d : drives) {
//            try (Stream<Path> stream = Files.walk(d.toPath())) {
//                stream.filter(Files::isDirectory)
//
//            } catch (IOException e){
//                System.err.println("Error scanning >"+e.getMessage());
//            }
//        }
//
//        return null;
//    }
    private static boolean isExcludedPath(Path path, Collection<String> filteredNames){
        String lowerPath = path.toString().toLowerCase();
        for (String filter : filteredNames) {
            if (lowerPath.contains(filter)) {
                //filterApplications.computeIfAbsent(filter, k -> new ArrayList<>()).add(path);
                return true;
            }
        }
        return false;
        //return filteredNames.stream().anyMatch(lowerPath::contains);
    }

    public static Set<Path> prepareMp3Folders(File[] drives) {
        Set<Path> allMp3Paths = new ConcurrentSkipListSet<>();
        Map<Path, Integer> mp3CountPerDir = new ConcurrentHashMap<>();
        AtomicInteger fileCount = new AtomicInteger(0);
        Set<String> excludedFolders = Set.of("tmp","recycle","logs","\\boot","\\support","windows","program files","programdata","intellij","xampp","perflogs","system volume information","appdata","recovery","nte und einst","sicherung","gradle","shader","web_","$windows",".m2","com.","org.","\\build","repositor","features",".git","\\git\\","src","licences","savedata","save data","$w");
        for (File d : drives) {
            try {
                Path result = Files.walkFileTree(d.toPath(), new FileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        if (!Files.isReadable(dir)) {
                            System.err.println("Unreadable - Skipping folder: " + dir);
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                        if (dir.getFileName() != null){
                            if (isExcludedPath(dir, excludedFolders)) {
                                System.out.println("No likely music folder - Skipping (filtering) folder: " + dir);
                                return FileVisitResult.SKIP_SUBTREE;
                            }
                        }

                        //prüfen ob Ordner zugänglich ist bzw. überspringen
                        //Invoked for a directory before entries in the directory are visited.
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                        if (allMp3Paths.contains(file.getParent())){
//                            System.err.println("Path "+file.getParent().toString()+" already stored");
//                            return FileVisitResult.SKIP_SIBLINGS;
//                        }

                        //if (file.toString().toLowerCase().equals(excludedFolders.))
                        fileCount.incrementAndGet();

                        if (file.toString().endsWith(".mp3")) {
                            Path parent = file.getParent();
                            if (parent != null){
                                try {
                                    allMp3Paths.add(parent);
                                    mp3CountPerDir.merge(file.getParent(), 1, Integer::sum);
                                } catch (RuntimeException e) {
                                    System.err.println("Could not add >" + file + "< to allMp3Paths. Error code: " + e.getMessage());
                                }
                            }
                        }
                        return FileVisitResult.CONTINUE;
                        //Prüfen ob mp3 enthalten und Ordner speichern
                        //Invoked for a file in a directory.
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        if (exc != null) {
                            System.err.println("Fehler beim Verarbeiten von " + file + ": " + exc.getMessage() + "().");
                            throw new IOException(exc);
                        }
                        return FileVisitResult.CONTINUE;
                        //Fehler protokollieren
                        //Invoked for a file that could not be visited.
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {

//                        if (mp3CountPerDir.containsKey(dir)) {
//                            allMp3Paths.add(dir);
//                        }


                        return FileVisitResult.CONTINUE;
                        //return null;
                        //Ressourcen schließen, Nachbearbeitung, Fehlerbehandlung
                        //Invoked for a directory after entries in the directory, and all of their descendants, have been visited.

                    }
                });
            } catch (IOException e) {
                System.err.println("Failed to scan drive "+d+", "+e.getMessage());
            }
            System.out.println("Total length of recursion of drive "+d+": "+fileCount+" // "+fileCount.toString());
        }
        return allMp3Paths;
    }

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

