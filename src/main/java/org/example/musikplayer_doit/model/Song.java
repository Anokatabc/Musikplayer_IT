package org.example.musikplayer_doit.model;

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
