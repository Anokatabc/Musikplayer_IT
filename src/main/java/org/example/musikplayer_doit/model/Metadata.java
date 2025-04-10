package org.example.musikplayer_doit.model;

import java.util.Date;

public class Metadata {
    private String title;
    private String artist;
    private int timesPlayed;
    private Date lastPlayed;
    private String album;
    private String genre;
    private String mood;
    private Byte[] coverImage;
    private String coverImagePath;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getTimesPlayed() {
        return timesPlayed;
    }

    public void setTimesPlayed(int timesPlayed) {
        this.timesPlayed = timesPlayed;
    }

    public Date getLastPlayed() {
        return lastPlayed;
    }

    public void setLastPlayed(Date lastPlayed) {
        this.lastPlayed = lastPlayed;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public Byte[] getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(Byte[] coverImage) {
        this.coverImage = coverImage;
    }

    public String getCoverImagePath() {
        return coverImagePath;
    }

    public void setCoverImagePath(String coverImagePath) {
        this.coverImagePath = coverImagePath;
    }

    //rating
    //fileSize

    //bByte[] coverImage = List<Artwork> //(parse to Image), store as Bbyte[]. Display: ByteArrayInputStream
    // if (artworkBytes != null) {
    //    Image image = new Image(new ByteArrayInputStream(artworkBytes));
    //    imageView.setImage(image); // Set the image on your JavaFX ImageView
    //} else {
    //    // Display a default image or placeholder
    //}

// File file;
// try{
//     Tag artist = AudioFileIO.read(file);
//    } catch (CannotReadException e){
//        System.err.println(e.getMessage());
//    }

//    catch (java.io.IOException e){
//        System.err.println(e.getMessage());
//    }
//    catch (TagException e){
//        System.err.println(e.getMessage());
//    }
//    catch (ReadOnlyFileException e){
//        System.err.println(e.getMessage());
//    }
//    catch (InvalidAudioFrameException e){
//        System.err.println(e.getMessage());
//    }
}
