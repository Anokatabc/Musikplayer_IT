package org.example.musikplayer_doit.model;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MP3FileMetadataExtractor {
    static {
        //reduce JAudioTagger's default red text clutter
        Logger.getLogger("org.jaudiotagger").setLevel(Level.WARNING);
    }

    public void extractTagFromMp3(Song[] songArray) throws NullPointerException {

        for (var s : songArray) {
            try {
                File file = new File(s.getPath());
                AudioFile audioFile = AudioFileIO.read(file);
                Tag tag = audioFile.getTag();
                if (tag != null) {
                    s.addMetadata("Album", tag.getFirst(FieldKey.ALBUM));
                    //s.addMetadata("Title", tag.getFirst(FieldKey.TITLE));
                    s.addMetadata("Artist", tag.getFirst(FieldKey.ARTIST));
                    s.addMetadata("Genre", tag.getFirst(FieldKey.GENRE));
                    s.addMetadata("Length", String.valueOf(audioFile.getAudioHeader().getTrackLength()));


                    System.out.println("Scanned metadata for file" + s.getPath()+"\nMetadata scanned: "+
                            tag.getFirst(FieldKey.ALBUM)
                            //+tag.getFirst(FieldKey.TITLE)
                            +tag.getFirst(FieldKey.ARTIST)
                            +tag.getFirst(FieldKey.GENRE));

                } else {
                    System.out.println("No tag found for file: "+file.getName());
                }
//            metadataMap.put("bitrate", audioFile.getAudioHeader().getBitRate());
//            metadataMap.put("sampleRate", audioFile.getAudioHeader().getSampleRate());
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }
}