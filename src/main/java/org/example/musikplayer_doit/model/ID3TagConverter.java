package org.example.musikplayer_doit.model;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.id3.ID3v1Tag;
import org.jaudiotagger.tag.id3.ID3v24FieldKey;
import org.jaudiotagger.tag.id3.ID3v24Tag;

import java.io.File;

public class ID3TagConverter {

    public static void convertID3v1ToID3v2(File[] files) {
        for (File file : files) {
            try {
                AudioFile audioFile = AudioFileIO.read(file);
                Tag tag = audioFile.getTag();

                if (tag instanceof ID3v1Tag) {
                    System.out.println("Converting ID3v1 to ID3v2 for file: " + file.getName());
                    ID3v1Tag id3v1Tag = (ID3v1Tag) tag;

                    // Erstellen eines neuen ID3v2.4-Tags
                    ID3v24Tag id3v2Tag = new ID3v24Tag();
                    if(id3v1Tag.getFirst(FieldKey.ALBUM) != null)id3v2Tag.setField(id3v2Tag.createField(ID3v24FieldKey.ALBUM, id3v1Tag.getAlbum().toString()));
                    //if(id3v1Tag.getFirst(FieldKey.ARTIST) != null)id3v2Tag.setField(id3v2Tag.createField(ID3v24FieldKey.ARTIST, id3v1Tag.getArtist().toString()));
                    //if(id3v1Tag.getFirst(FieldKey.TITLE) != null)id3v2Tag.setField(id3v2Tag.createField(ID3v24FieldKey.TITLE, id3v1Tag.getFirstTitle()));
                    //if(id3v1Tag.getFirst(FieldKey.YEAR) != null)id3v2Tag.setField(id3v2Tag.createField(ID3v24FieldKey.YEAR, id3v1Tag.getFirstYear()));
                    //if(id3v1Tag.getFirst(FieldKey.GENRE) != null)id3v2Tag.setField(id3v2Tag.createField(ID3v24FieldKey.GENRE, id3v1Tag.getFirstGenre()));
                    //if(id3v1Tag.getFirst(FieldKey.TRACK) != null)id3v2Tag.setField(id3v2Tag.createField(ID3v24FieldKey.TRACK, id3v1Tag.getFirstTrack()));
                    //if(id3v1Tag.getFirst(FieldKey.COMMENT) != null)id3v2Tag.setField(id3v2Tag.createField(ID3v24FieldKey.COMMENT, id3v1Tag.getFirstComment()));
                    //if(id3v1Tag.getFirst(FieldKey.LYRICS) != null) id3v2Tag.setField(FieldKey.LYRICS, id3v1Tag.getFirst(FieldKey.LYRICS));





                    //TRACK

                    //COMMENT
                    // ID3v2-Tag dem AudioFile hinzufügen
                    audioFile.setTag(id3v2Tag);
                    AudioFileIO.write(audioFile);
                }
            } catch (CannotReadException e) {
                System.err.println("Cannot read file: " + file.getName() + " - " + e.getMessage());
            } catch (CannotWriteException e) {
                System.err.println("Cannot write to file: " + file.getName() + " - " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Error processing file: " + file.getName() + " - " + e.getMessage());
            }
        }
    }
}

