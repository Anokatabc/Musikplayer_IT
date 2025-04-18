package org.example.musikplayer_doit.model;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Song {

//Interpol Projekt anscheuan für ObservableList

    //private Map<String, File/Song?> metadataCache = new HashMap<>();
    //todo: class Cache oder im Model?
    //todo: Sqlite auch für persistente Config Einstellungen nutzen
    //public void addToCache(){}
    //public void getFromCache(){}
    //public boolean isInCache(){}
    private String title;
    private File file;
    private String path;
    private String album;
    private Map<String, Object> metadata;


    public Song (String filePath, Map<String, Object> metadata){
        this.file = new File(filePath);
    this.title = file.getName();
    this.path = filePath;
    //this.album = (album != null) ? album : "null";
    //&& !album.isEmpty()

}

    public void setAlbum(String album) {
        this.album = album;
        System.out.println("Successfully set album: "+album);
    }

//    public String getAlbum() {
//        if (album != null){
//            System.out.println("Return album: "+album);
//            return album;
//        } else {
//            System.out.println("Album is null");
//            return null;
//        }
//
//    }

//    public File getFile() {
//        return file;
//    }
//
//    public void setFile(File file) {
//        this.file = file;
//    }

//    public void loadMetadata(Song song){
//        try {
//            AudioFile audioFile = AudioFileIO.read(file);
//            Tag tag = audioFile.getTag();
//            if (tag != null){
//
//
//                metadata.put("Title", tag.getFirst(FieldKey.TITLE));
//                metadata.put("Artist", tag.getFirst(FieldKey.ARTIST));
//                metadata.put("Album", tag.getFirst(FieldKey.ALBUM));
//                metadata.put("Year", tag.getFirst(FieldKey.YEAR));
//                metadata.put("Genre", tag.getFirst(FieldKey.GENRE));
//            }
//        } catch (Exception e) {
//            System.err.println("Error reading metadata for file: "+e.getMessage());
//        }
//    }

    public String getTitle() {
        return title;
    }

    public String getPath() {
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

        return "Unbekanntes Album";
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

        return "Unbekanntes Album";
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
//todo: (much later) use getName() + hash value for song construction. If hash already exists, update file path. if not, create new song file with name and hash value. Prevents duplicate entries
// alternatively: Audio fingerprinting (create additional hash on audio editing(?))
}
