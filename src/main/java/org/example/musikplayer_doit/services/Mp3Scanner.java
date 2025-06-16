package org.example.musikplayer_doit.services;

import javafx.concurrent.Task;
import org.example.musikplayer_doit.model.Mp3FolderInfo;

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
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Mp3Scanner {
private static Mp3FolderInfo mp3FolderInfo;

    public static void collectMp3Folders(File[] drives, Consumer<Mp3FolderInfo> callback) {

        Set<String> excludedFolders = Set.of("tmp","recycle","logs","\\boot","\\support","windows","program files","programdata","intellij","xampp","perflogs","system volume information","appdata","recovery","nte und einst","sicherung","gradle","shader","web_","$windows",".m2","com.","org.","\\build","repositor","features",".git","\\git\\","src","licences","savedata","save data","$w");
        Set<Path> allMp3Paths = new ConcurrentSkipListSet<>();
        Map<Path, Integer> mp3CountPerDir = new ConcurrentHashMap<>();

        for (File d : drives) {
            Task<Mp3FolderInfo> task = new Task<>() {
                @Override
                protected Mp3FolderInfo call() throws Exception {

                    return scanFolders(d, excludedFolders, allMp3Paths, mp3CountPerDir);
                }
            };
            task.setOnSucceeded((worker) -> {
                Mp3FolderInfo info = task.getValue();
                //alle Pfade enthalten

                callback.accept(info);

                int count = info.mp3Count();
                Map<Path, Integer> map = info.folderMp3Count();
                Set<Path> paths = info.mp3Paths();
            });
            Thread thread = new Thread(task);
            thread.start();
        }
    }

        private static Mp3FolderInfo scanFolders(File drive, Collection<String> excludedFolders, Set<Path> allMp3Paths, Map<Path, Integer> mp3CountPerDir){
            AtomicInteger fileCount = new AtomicInteger(0);
            AtomicInteger mp3AtomicCount = new AtomicInteger(0);
            try {
                Files.walkFileTree(drive.toPath(), new FileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        if (!Files.isReadable(dir) || !Files.isExecutable(dir)) {
                            System.err.println("Unreadable or inaccessible - Skipping folder: " + dir);
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                        if (!Files.exists(dir)) {
                            System.err.println("Existiert nicht: " + dir);
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                        if (dir.getFileName() != null){
                            if (isExcludedPath(dir, excludedFolders)) {
                                //System.out.println("No likely music folder - Skipping (filtering) folder: " + dir);
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
                        fileCount.incrementAndGet();

                        if (file.toString().endsWith(".mp3")) {
                            mp3AtomicCount.incrementAndGet();
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
                            System.err.println("Fehler beim Zugriff auf " + file + ": " + exc.getClass().getSimpleName() +": "+exc.getMessage());
                            throw new IOException(exc);
                        }
                        exc.printStackTrace();
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
                System.err.println("Failed to scan drive "+drive+", "+e.getMessage());
            }
            System.out.println("Total length of recursion of drive "+drive+": "+fileCount);
            int mp3Count = mp3AtomicCount.get();
            mp3FolderInfo = new Mp3FolderInfo(allMp3Paths, mp3CountPerDir, mp3Count);
            System.out.println("Count of mp3 files: "+mp3FolderInfo.mp3Count());
            return mp3FolderInfo;
        }

    public static Mp3FolderInfo getMp3FolderInfo(String mapOrSet){
        return mp3FolderInfo;
    }

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

    public static Set<Path> getUniqueParentsSet (Set<Path> paths){
        Set<Path> uniqueParents = new ConcurrentSkipListSet<>(paths);
        for (var p : paths){
            //System.err.println("unique parents: "+p);
            Path parent = p.getParent();
            while (parent != null){
                uniqueParents.add(parent);
                parent = parent.getParent();
            }
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

