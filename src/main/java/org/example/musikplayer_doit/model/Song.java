package org.example.musikplayer_doit.model;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.util.Map;

public class Song {

//Interpol Projekt anscheuan für ObservableList

    //private Map<String, File/Song?> metadataCache = new HashMap<>();
    //todo: class Cache oder im Model?
    //public void addToCache(){}
    //public void getFromCache(){}
    //public boolean isInCache(){}
    private String title;
    private File file;
    private String path;
    private Map<String, Object> metadata;
    private String songLength;



    public Song (String filePath, Map<String, Object> metadata){
        this.file = new File(filePath);
    this.title = file.getName();
    this.path = filePath;
    //this.songLength = songLength;
}

//    public File getFile() {
//        return file;
//    }
//
//    public void setFile(File file) {
//        this.file = file;
//    }

    public void loadMetadata(Song song){
        try {
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();
            if (tag != null){

                //tag.getFirst(FieldKey.TITLE) == null ? return : metadata.put("Title", tag.getFirst(FieldKey.TITLE));
                metadata.put("Title", tag.getFirst(FieldKey.TITLE));
                metadata.put("Artist", tag.getFirst(FieldKey.ARTIST));
                metadata.put("Album", tag.getFirst(FieldKey.ALBUM));
                metadata.put("Year", tag.getFirst(FieldKey.YEAR));
                metadata.put("Genre", tag.getFirst(FieldKey.GENRE));
            }
        } catch (Exception e) {
            System.err.println("Error reading metadata for file: "+e.getMessage());
        }
    }

    public String getTitle() {
        return title;
    }

    public String getPath() {
        return path;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    public void removeMetadata(String key, Object value) {
        metadata.remove(key, value);
    }

    public void printMetadata() {
        System.out.println("Metadata for "+path+": "+metadata);
    }
}
