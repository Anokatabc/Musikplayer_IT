package org.example.musikplayer_doit.model;

import java.io.File;

public class Song {

//Interpol Projekt anscheuan für ObservableList

    private String title;
    private File file;

public Song (String title){
    this.title = title;
//    this.file = file;
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

    public void setTitle(String title) {
        this.title = title;
    }

}
