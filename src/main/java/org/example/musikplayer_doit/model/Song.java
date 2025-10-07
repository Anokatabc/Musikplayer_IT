//todo: class Cache oder im Model?
//todo: Sqlite auch für persistente Config Einstellungen nutzen
//todo: (much later) use getName() + hash value for song construction. If hash already exists, update file path. if not, create new song file with name and hash value. Prevents duplicate entries
package org.example.musikplayer_doit.model;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Song {

    //
    private Path title;
    private Path path;
    private String genre;
    private String album;
    private String artist;
    private Map<String, Object> metadata;

    /**
     *
     * @param filePath
     * @param metadata
     */
    public Song (Path filePath, Map<String, Object> metadata){
        this.title = filePath.getFileName();
        this.path = filePath;

    }

    public Path getTitle() {
        return title;
    }

    public Path getPath() {
        return path;
    }
    public String getAlbum() {
        if (metadata != null && metadata.containsKey("Album")) {
            // Ermitteln der aufrufenden Methode
            //StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace()
            //String callerMethod = stackTrace.length > 2 ? stackTrace[2].getMethodName() : "Unknown"

            // Ausgabe der aufrufenden Methode
            //System.out.println("Method getAlbum() called by: " + callerMethod)

            return metadata.get("Album").toString();
        }

        return "Unbekanntes Album";
    }

    public String getArtist() {
        if (metadata != null && metadata.containsKey("Artist")) {
            // Ermitteln der aufrufenden Methode
            //StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace()
            //String callerMethod = stackTrace.length > 2 ? stackTrace[2].getMethodName() : "Unknown"

            // Ausgabe der aufrufenden Methode
            //System.out.println("Method getAlbum() called by: " + callerMethod)

            return metadata.get("Artist").toString();
        }
        else {
            return "Unknown Artist";
        }
    }
    public String getGenre() {
        if (metadata != null && metadata.containsKey("Genre")) {
            // Ermitteln der aufrufenden Methode
            //StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace()
            //String callerMethod = stackTrace.length > 2 ? stackTrace[2].getMethodName() : "Unknown"

            // Ausgabe der aufrufenden Methode
            //System.out.println("Method getGenre() called by: " + callerMethod)

            return metadata.get("Genre").toString();
        }

        return "Unbekanntes Genre";
    }

    public String getLength() {
        if (metadata != null && metadata.containsKey("Length")) {
            Object lengthObj = metadata.get("Length");
            try {
                int length = Integer.parseInt(lengthObj.toString());

                int hours = length / 3600;
                int minutes = (length % 3600) / 60;
                int seconds = length % 60;

                String lengthString = (hours > 0)
                        ? String.format("%d:%02d:%02d", hours, minutes, seconds)
                        : String.format("%02d:%02d", minutes, seconds);

                return lengthString;
            } catch (NumberFormatException e) {
                System.out.println("Error getting length");
                return lengthObj.toString();
            }
        }
        return "Unknown";
    }



    public void addMetadata(String key, Object value) {
//        Runtime runtime = Runtime.getRuntime();
//        long before = runtime.totalMemory() - runtime.freeMemory();
//        long after = runtime.totalMemory() - runtime.freeMemory();
//        System.out.println("Speicherverbrauch der Map: " + (after - before) + " Bytes");
        if (metadata == null) {
            metadata = new HashMap<>();
            System.out.println("new HashMap created");
        }
        metadata.put(key, value);
// Map befüllen oder Operation ausführen

    }

    public void removeMetadata(String key, Object value) {
        metadata.remove(key, value);
    }

    public void printMetadata() {
        System.out.println("Metadata for "+path+": "+metadata);
    }
}
