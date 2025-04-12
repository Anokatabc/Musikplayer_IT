package org.example.musikplayer_doit.model;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

//gelernt: innerhalb eines Packages können alle Klassen auf "einanders" Membervariablen zugreifen.
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
    public boolean contains (Song song){
        for (var s : queue){
            if (s == song){
                return true;
            }
        }
        return false;
    }
    public int getIndexOf(Song song){
        int index = queue.indexOf(song);
        return index;
    }
    public Song getSongAtFirstIndex(int index){
        for (var s : queue){
            if (queue.indexOf(s) == index){
                return s;
            }
        }
        System.err.println(">getSongAtFirstIndex in Model: no song found");
        return null;
    }
//    public boolean isEmpty(){
//        if (this.getQueue() == null){
//            return true;
//        }
//        return false;
//    }

}
