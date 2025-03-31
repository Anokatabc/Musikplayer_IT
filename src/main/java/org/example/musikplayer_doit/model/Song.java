package org.example.musikplayer_doit.model;

import java.io.File;

public class Song {

//Interpol Projekt anscheuan für ObservableList

    private String title;
    private File file;
    private String path;



    public Song (String filePath){
        this.file = new File(filePath);
    this.title = file.getName();
    this.path = filePath;
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


}
