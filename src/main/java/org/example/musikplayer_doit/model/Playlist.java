package org.example.musikplayer_doit.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Playlist  {

    private ObservableList<Song> queue;

    public Playlist (){
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

    public int getNext(int currentSongIndex){
        if (currentSongIndex < queue.size()){
            return currentSongIndex+1;
        } else {
            return -1;
        }
    }

    public int getPreviousIfExists (int currentSongIndex){
        if (queue.size() > 1 && currentSongIndex > 0){
            return currentSongIndex-1;
        } else {
            return -1;
        }
    }

    public boolean isLastIndex (int countingIndex){
        if (countingIndex == queue.indexOf(queue.getLast())){
            return true;
        }
        return false;
    }
}
