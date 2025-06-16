package org.example.musikplayer_doit.model;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class Playlist  {

    private ObservableList<Song> queue;

    public Playlist (ObservableList<Song> queue){
        this.queue = FXCollections.observableArrayList();
    }

    public ObservableList<Song> getQueue() {
        return queue;
    }

    public void setQueue(ObservableList<Song> queue) {
        this.queue = queue;
    }
    public void clearQueue(){
        queue.clear();
    }
    public void removeSong(Song song){
        queue.remove(song);
    }
    public void addSong(Song song){
        queue.add(song);
    }
}
