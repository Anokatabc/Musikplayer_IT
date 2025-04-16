//Application of class
//instance variable or no? MP3MetaDataExtractor extractor; (and use later this.MetadataExtractor = new...)
//MP3MetadataExtractor extractor = new MP3MetadataExtractor();
//Map<String, Object> metadata = extractor.extractMetadata(notYetConvertedFile);


package org.example.musikplayer_doit.model;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import javax.swing.text.html.HTML;
import java.io.File;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MP3FileMetadataExtractor {
    static {
        //reduce JAudioTagger's default red text clutter
        Logger.getLogger("org.jaudiotagger").setLevel(Level.WARNING);
    }

    //check for existence of higher tags first and call them, then use basic getTag
    public void extractTagFromMp3(Song[] songArray) {

        for (var s : songArray) {
            try {
                File file = new File(s.getPath());
                AudioFile audioFile = AudioFileIO.read(file);
                Tag tag = audioFile.getTag();
                if (tag != null) {
//                    String extractedAlbum = tag.getFirst(FieldKey.ALBUM);
//                    s.setAlbum(extractedAlbum);
                    s.addMetadata("Album", tag.getFirst(FieldKey.ALBUM));
                    //s.addMetadata("Title", tag.getFirst(FieldKey.TITLE));
                    s.addMetadata("Artist", tag.getFirst(FieldKey.ARTIST));
                    s.addMetadata("Genre", tag.getFirst(FieldKey.GENRE));
                    //s.addMetadata("Track", tag.getFirst(FieldKey.TRACK));
                    s.addMetadata("Length", String.valueOf(audioFile.getAudioHeader().getTrackLength()));
                    System.out.println("Scanned metadata for file" + s.getPath()+"\nMetadata scanned: "+
                            tag.getFirst(FieldKey.ALBUM)
                            //+tag.getFirst(FieldKey.TITLE)
                            +tag.getFirst(FieldKey.ARTIST)
                            +tag.getFirst(FieldKey.GENRE)
                            +tag.getFirst(String.valueOf(audioFile.getAudioHeader().getTrackLength())));
                            //+tag.getFirst(FieldKey.TRACK))
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



/// - - - - - prioritize higher tags
// Okay, that's a very insightful question and touches on a key aspect of working with MP3 metadata, especially with older or mixed-origin files. You are right to be thinking about the different tag versions.
//
//Let's clarify JAudioTagger's behavior and then redesign the class.
//
//**JAudioTagger and Tag Versions (`getTag()`):**
//
//*   **It's Smarter Than Just ID3v1:** You're right that ID3v1 is limited. Thankfully, `audioFile.getTag()` in JAudioTagger is designed to be more robust. **It generally tries to return the "best" or most complete tag available in the file.**
//    *   If an MP3 file has **both** ID3v1 and ID3v2.x tags (which is common for compatibility), `getTag()` will **usually return the ID3v2.x tag** because it's more capable.
//    *   If a file *only* has ID3v1, `getTag()` will return that.
//    *   If a file *only* has ID3v2.x, `getTag()` will return that.
//    *   If a file has no tags, `getTag()` returns `null`.
//*   **Accessing Specific Versions:** While `getTag()` provides a convenient default, JAudioTagger *does* allow you to explicitly request specific tag versions if needed using `audioFile.getID3v1Tag()` and `audioFile.getID3v2Tag()`. This gives you finer control.
//
//**Need for Conversion (ID3v1 -> ID3v2):**
//
//*   **For Reading:** For *just reading* data, conversion isn't strictly necessary if you use JAudioTagger correctly. By prioritizing ID3v2 (either implicitly with `getTag()` or explicitly), you get the richer data if it exists. If only ID3v1 exists, you get what's available.
//*   **For Writing/Standardizing:** If your goal is to *standardize* your music library by ensuring all files *have* ID3v2 tags, potentially copying data from ID3v1 if v2 is missing, then yes, a "conversion" step (which involves **writing** to the file) would be needed. This is a separate concern from *reading*.
//*   **Heterogeneous Datasets:** In a mixed dataset, a strategy of "read v2 if present, otherwise read v1" is a good *reading* approach. A separate process might be desired later to *update* files to a consistent v2 format.
//
//**Revising `Mp3MetadataExtractor` for Explicit ID3v2 Preference:**
//
//Let's redesign the class to *explicitly* prefer ID3v2 tags, falling back to ID3v1 only if v2 is not found. This directly addresses your concern and provides more certainty. We'll focus on *reading* for now, as per the original request, but I'll add notes on where conversion logic would fit.
//
//```java
//import java.io.File;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
/// / --- JAudioTagger Imports ---
//import org.jaudiotagger.audio.AudioFile;
//import org.jaudiotagger.audio.AudioFileIO;
//import org.jaudiotagger.audio.exceptions.CannotReadException;
//import org.jaudiotagger.audio.exceptions.CannotWriteException;
//import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
//import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
//import org.jaudiotagger.tag.FieldKey;
//import org.jaudiotagger.tag.Tag;
//import org.jaudiotagger.tag.TagException;
//import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
//import org.jaudiotagger.tag.id3.ID3v1Tag;
//import org.jaudiotagger.tag.id3.ID3v24Tag; // Example if you needed v2.4 specific things
//
//
//public class Mp3MetadataExtractor {
//
//    // Optional: Disable JAudioTagger's internal logging clutter if desired
//    static {
//        Logger.getLogger("org.jaudiotagger").setLevel(Level.WARNING);
//    }
//
//    /**
//     * Retrieves the Album metadata from an MP3 file using JAudioTagger,
//     * explicitly preferring ID3v2 tags over ID3v1.
//     *
//     * @param mp3File     The MP3 file object (assumed to exist and be an MP3).
//     * @param metadataMap The map where the "Album" key-value pair should be stored.
//     */
//    public void addAlbumToMetadataMap(File mp3File, Map<String, Object> metadataMap) {
//        if (!isValidInput(mp3File, metadataMap)) {
//            return; // Error message printed in isValidInput
//        }
//
//        System.out.println("Processing file (v2 Preferred): " + mp3File.getName());
//
//        AudioFile audioFile = null;
//        try {
//            // 1. Read the audio file
//            audioFile = AudioFileIO.read(mp3File);
//
//            Tag tagToUse = null;
//            String sourceTagType = "N/A";
//
//            // 2. Explicitly Prefer ID3v2 tag
//            //    getID3v2Tag() returns AbstractID3v2Tag (could be v2.3 or v2.4)
//            AbstractID3v2Tag id3v2Tag = audioFile.getID3v2Tag();
//
//            if (id3v2Tag != null) {
//                System.out.println("  -> Found ID3v2 tag.");
//                tagToUse = id3v2Tag;
//                sourceTagType = "ID3v2." + id3v2Tag.getMajorVersion(); // e.g., "ID3v2.3" or "ID3v2.4"
//            } else {
//                System.out.println("  -> No ID3v2 tag found. Checking for ID3v1.");
//                // 3. Fallback to ID3v1 tag if ID3v2 doesn't exist
//                ID3v1Tag id3v1Tag = audioFile.getID3v1Tag();
//                if (id3v1Tag != null) {
//                    System.out.println("  -> Found ID3v1 tag.");
//                    tagToUse = id3v1Tag;
//                    sourceTagType = "ID3v1";
//                } else {
//                    System.out.println("  -> No ID3v1 tag found either.");
//                    // No tags found at all
//                }
//            }
//
//            // 4. Extract data from the selected tag (if any)
//            if (tagToUse != null) {
//                String album = tagToUse.getFirst(FieldKey.ALBUM);
//
//                if (album != null && !album.trim().isEmpty()) {
//                    metadataMap.put("Album", album);
//                    // Optionally, store which tag type it came from
//                    metadataMap.put("TagSource", sourceTagType);
//                    System.out.println("  -> Album found: '" + album + "' (from " + sourceTagType + ") - Added to map.");
//                } else {
//                    System.out.println("  -> Album field not found or is empty in the " + sourceTagType + " tag.");
//                }
//            } else {
//                System.out.println("  -> No usable ID3 tags found in this file.");
//            }
//
//        } catch (CannotReadException e) {
//            System.err.println("Error: Cannot read file (may not be a valid audio file or permission issue): " + mp3File.getName() + " - " + e.getMessage());
//        } catch (IOException e) {
//            System.err.println("Error: I/O exception while reading file: " + mp3File.getName() + " - " + e.getMessage());
//        } catch (TagException e) {
//            System.err.println("Error: Cannot read tag data (metadata might be corrupt): " + mp3File.getName() + " - " + e.getMessage());
//        } catch (ReadOnlyFileException e) {
//            // Should not prevent reading, but good to know
//            System.err.println("Info: File is read-only: " + mp3File.getName() + " - " + e.getMessage());
//        } catch (InvalidAudioFrameException e) {
//            System.err.println("Error: Invalid audio frame found (file might be corrupt): " + mp3File.getName() + " - " + e.getMessage());
//        } catch (Exception e) {
//            // Catch-all for any other unexpected exceptions
//            System.err.println("An unexpected error occurred while processing file: " + mp3File.getName() + " - " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Helper method for basic input validation.
//     *
//     * @param mp3File     File to check
//     * @param metadataMap Map to check
//     * @return true if inputs are valid, false otherwise
//     */
//    private boolean isValidInput(File mp3File, Map<String, Object> metadataMap) {
//         if (mp3File == null || !mp3File.exists() || !mp3File.isFile()) {
//            System.err.println("Error: Provided file is invalid or does not exist: " + (mp3File != null ? mp3File.getPath() : "null"));
//            return false;
//        }
//        if (metadataMap == null) {
//            System.err.println("Error: Provided metadataMap is null.");
//            return false;
//        }
//        return true;
//    }
//
//
//    // --- Notes on Conversion (File Modification) ---
//    /*
//     * If you want to *standardize* files by ensuring they have ID3v2 tags,
//     * potentially copying from v1 if v2 is missing, you'd need a *write* operation.
//     * This MODIFIES THE FILE ON DISK.
//     *
//     * This is a separate concern from just reading. A dedicated class
//     * (e.g., `Mp3TagConverter`) might be a good idea for this.
//     *
//     * Basic Idea for a conversion method:
//     *
//     * public boolean ensureID3v2Tag(File mp3File) {
//     *     if (mp3File == null || !mp3File.exists() || !mp3File.canWrite()) {
//     *         System.err.println("Cannot convert: Invalid or non-writable file.");
//     *         return false;
//     *     }
//     *
//     *     AudioFile audioFile = null;
//     *     try {
//     *         audioFile = AudioFileIO.read(mp3File);
//     *
//     *         // Check if v2 tag exists
//     *         if (audioFile.getID3v2Tag() != null) {
//     *             System.out.println("File already has ID3v2 tag: " + mp3File.getName());
//     *             return true; // Already has v2, nothing to do
//     *         }
//     *
//     *         // Check if v1 tag exists (to copy from)
//     *         ID3v1Tag v1Tag = audioFile.getID3v1Tag();
//     *         if (v1Tag == null) {
//     *             System.out.println("No ID3v1 tag found to copy from for: " + mp3File.getName());
//     *             // Optionally create a blank v2 tag? Depends on requirements.
//     *             // audioFile.setTag(new ID3v24Tag()); // Create blank v2.4 tag
//     *             // audioFile.commit();
//     *             return false; // No source data
//     *         }
//     *
//     *         System.out.println("Converting ID3v1 to ID3v2 for: " + mp3File.getName());
//     *
//     *         // Create a new ID3v2 tag (e.g., v2.4)
//     *         AbstractID3v2Tag v2Tag = new ID3v24Tag(); // Or ID3v23Tag()
//     *
//     *         // **Copy fields from v1 to v2**
//     *         // This requires manually copying relevant fields
//     *         // JAudioTagger doesn't have a direct "copy v1 to v2" method built-in AFAIK
//     *         try {
//     *              if(v1Tag.getFirst(FieldKey.ARTIST) != null) v2Tag.setField(FieldKey.ARTIST, v1Tag.getFirst(FieldKey.ARTIST));
//     *              if(v1Tag.getFirst(FieldKey.ALBUM) != null) v2Tag.setField(FieldKey.ALBUM, v1Tag.getFirst(FieldKey.ALBUM));
//     *              if(v1Tag.getFirst(FieldKey.TITLE) != null) v2Tag.setField(FieldKey.TITLE, v1Tag.getFirst(FieldKey.TITLE));
//     *              if(v1Tag.getFirst(FieldKey.TRACK) != null) v2Tag.setField(FieldKey.TRACK, v1Tag.getFirst(FieldKey.TRACK));
//     *              if(v1Tag.getFirst(FieldKey.YEAR) != null) v2Tag.setField(FieldKey.YEAR, v1Tag.getFirst(FieldKey.YEAR));
//     *              if(v1Tag.getFirst(FieldKey.GENRE) != null) v2Tag.setField(FieldKey.GENRE, v1Tag.getFirst(FieldKey.GENRE));
//     *              if(v1Tag.getFirst(FieldKey.COMMENT) != null) v2Tag.setField(FieldKey.COMMENT, v1Tag.getFirst(FieldKey.COMMENT));
//     *              if(v1Tag.getFirst(FieldKey.COMMENT) != null) v2Tag.setField(FieldKey.COMMENT, v1Tag.getFirst(FieldKey.COMMENT));
//     *              // ... potentially others if needed, v1 is limited
//     *         } catch (TagException e) {
//     *              System.err.println("Error copying tag field during conversion: " + e.getMessage());
//     *              // Decide how to handle partial copy failure
//     *         }
//     *
//     *         // Set the new v2 tag in the audio file object
//     *         audioFile.setTag(v2Tag);
//     *
//     *         // **IMPORTANT: Write changes back to the file**
//     *         AudioFileIO.write(audioFile);
//     *         // Or use audioFile.commit();
//     *
//     *         System.out.println("Conversion successful for: " + mp3File.getName());
//     *         return true;
//     *
//     *     } catch (CannotWriteException e) {
//     *         System.err.println("Error: Cannot write tag to file (permissions?): " + mp3File.getName() + " - " + e.getMessage());
//     *         return false;
//     *     } catch (Exception e) {
//     *         System.err.println("An error occurred during tag conversion for " + mp3File.getName() + ": " + e.getMessage());
//     *          e.printStackTrace();
//     *         return false;
//     *     }
//     * }
//     */
//
//
//    // --- Example Usage ---
//    public static void main(String[] args) {
//        Mp3MetadataExtractor extractor = new Mp3MetadataExtractor();
//        Map<String, Object> songMetadata = new HashMap<>();
//
//        // *** IMPORTANT: Replace with YOUR file paths ***
//        String testMp3PathV2 = "path/to/your/song_with_v2_tags.mp3";
//        String testMp3PathV1Only = "path/to/your/song_with_only_v1_tags.mp3";
//        String testMp3PathNoTags = "path/to/your/song_with_no_tags.mp3";
//        String nonExistentPath = "path/to/non_existent_file.mp3";
//
//        System.out.println("--- Processing file with likely V2 tags ---");
//        extractor.addAlbumToMetadataMap(new File(testMp3PathV2), songMetadata);
//        printMap(songMetadata);
//        songMetadata.clear(); // Clear map for next test
//
//        System.out.println("\n--- Processing file with only V1 tags ---");
//        extractor.addAlbumToMetadataMap(new File(testMp3PathV1Only), songMetadata);
//        printMap(songMetadata);
//        songMetadata.clear();
//
//        System.out.println("\n--- Processing file with no tags ---");
//        extractor.addAlbumToMetadataMap(new File(testMp3PathNoTags), songMetadata);
//        printMap(songMetadata);
//        songMetadata.clear();
//
//        System.out.println("\n--- Processing non-existent file ---");
//        extractor.addAlbumToMetadataMap(new File(nonExistentPath), songMetadata);
//        printMap(songMetadata);
//        songMetadata.clear();
//    }
//
//    private static void printMap(Map<String, Object> map) {
//        System.out.println("  Map Contents:");
//        if (map.isEmpty()) {
//            System.out.println("  -> Map is empty.");
//        } else {
//            map.forEach((key, value) ->
//                System.out.println("  -> Key: " + key + ", Value: " + value)
//            );
//        }
//    }
//}
//```
//
//**Key Changes and Explanations:**
//
//1.  **Explicit Tag Preference:**
//    *   Instead of just `audioFile.getTag()`, we now do:
//        *   `AbstractID3v2Tag id3v2Tag = audioFile.getID3v2Tag();`
//        *   `ID3v1Tag id3v1Tag = audioFile.getID3v1Tag();`
//    *   We check if `id3v2Tag` exists. If yes, we use it.
//    *   If `id3v2Tag` is `null`, we then check if `id3v1Tag` exists. If yes, we use that.
//    *   If neither exists, we note that no tags were found.
//2.  **Tracking Tag Source:**
//    *   A `sourceTagType` variable keeps track of whether the data came from "ID3v2.x" or "ID3v1".
//    *   This is added to the map as `"TagSource"` (optional, but useful for understanding your data).
//3.  **Input Validation Helper:** A small `isValidInput` method is added for cleaner code.
//4.  **Notes on Conversion:**
//    *   A commented-out section (`ensureID3v2Tag`) provides a blueprint for how you *would* perform a conversion if needed.
//    *   **Crucially, it highlights:**
//        *   This is a **write** operation.
//        *   It **modifies the file**.
//        *   It requires **write permissions**.
//        *   You need to **manually copy fields** from the v1 tag to the new v2 tag.
//        *   You must call `AudioFileIO.write(audioFile)` or `audioFile.commit()` to save changes.
//        *   Putting this logic in a separate class (`Mp3TagConverter`?) is recommended for separation of concerns, especially if it gets more complex.
//
//Now, your `Mp3MetadataExtractor` explicitly tries to get data from ID3v2 tags first, falling back to ID3v1, which aligns better with your requirement to prioritize the richer tag format when reading. The conversion aspect is outlined but kept separate from the primary reading task.


/// - - - - - prioritize higher tags v2
//You've raised a very important point about ID3 tag versions! You are correct to be thinking about ID3v1 vs. ID3v2 and their implications.  Let's clarify and adjust the `MP3MetadataExtractor` accordingly.
//
//**Understanding `audioFile.getTag()` and ID3 Versions in JAudioTagger**
//
//Your initial suspicion that `audioFile.getTag()` only retrieves ID3v1 is **incorrect**. JAudioTagger is designed to be more intelligent than that.
//
//*   **`audioFile.getTag()` in JAudioTagger is designed to retrieve the "best" available tag.**  "Best" generally means prioritizing ID3v2 tags (v2.4, v2.3, v2.2) because they are more modern, flexible, and capable of storing a wider range of metadata and larger amounts of text. If no ID3v2 tag is found, it will typically fall back to reading ID3v1 tags if they exist.  It might also look for APE tags or other tag formats depending on the audio file type and available tag types.
//
//*   **JAudiotagger handles the abstraction:** You generally don't need to explicitly tell JAudioTagger to read ID3v2 or ID3v1. `audioFile.getTag()` is meant to provide you with a unified `Tag` object, regardless of the underlying tag version (as much as is practically possible).  The `Tag` interface is designed to abstract away the differences between tag versions for common metadata fields.
//
//*   **Explicit ID3v1/ID3v2 access (for advanced cases):**  If you *really* need to work with *specific* tag versions (e.g., to check if an ID3v2 tag exists or to manipulate ID3v1 tags directly), JAudioTagger provides methods like:
//    *   `audioFile.getID3v2Tag()`:  Returns the ID3v2 tag (if one exists), or `null` if not. You can then work with the `ID3v2Tag` object specifically.
//    *   `audioFile.getID3v1Tag()`: Returns the ID3v1 tag (if one exists), or `null` if not. You can work with the `ID3v1Tag` object.
//
//    However, for most common metadata retrieval tasks (like getting album, artist, title), using `audioFile.getTag()` is sufficient and recommended as it handles the tag version selection for you.
//
//**Refined `MP3MetadataExtractor` Class (Considering ID3v2 and More Metadata)**
//
//Let's refine the `MP3MetadataExtractor` to:
//
//1.  **Continue using `audioFile.getTag()`:**  This is still the best approach for general metadata retrieval.
//2.  **Retrieve more common ID3v2 metadata fields:** Expand the example to extract Artist, Title, and Year as well, to demonstrate handling more than just Album.
//3.  **Keep the structure clean and error handling robust.**
//4.  **Optionally, add code (commented out) showing how to explicitly check for ID3v2 tag (for informational purposes and more advanced use cases, but not essential for basic retrieval).**
//
//Here's the revised `MP3MetadataExtractor` class:
//
//```java
//import org.jaudiotagger.audio.AudioFile;
//import org.jaudiotagger.audio.AudioFileIO;
//import org.jaudiotagger.audio.exceptions.*;
//import org.jaudiotagger.tag.FieldKey;
//import org.jaudiotagger.tag.Tag;
//import org.jaudiotagger.tag.TagException;
//import org.jaudiotagger.tag.id3.ID3v24Frames; // Example of ID3v2.4 specific class (not needed for basic retrieval)
//
//import java.io.File;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//public class MP3MetadataExtractor {
//
//    public Map<String, Object> extractMetadata(File mp3File) {
//        Map<String, Object> metadataMap = new HashMap<>();
//        try {
//            AudioFile audioFile = AudioFileIO.read(mp3File);
//            Tag tag = audioFile.getTag(); // **Retrieves the "best" tag available (likely ID3v2 if present)**
//
//            if (tag != null) {
//                // Retrieve common metadata fields using FieldKey (works for both ID3v1 and ID3v2 tags)
//                String album = tag.getFirst(FieldKey.ALBUM);
//                String artist = tag.getFirst(FieldKey.ARTIST);
//                String title = tag.getFirst(FieldKey.TITLE);
//                String year = tag.getFirst(FieldKey.YEAR); // Note: Year might be a number or string
//
//                if (album != null && !album.isEmpty()) {
//                    metadataMap.put("Album", album);
//                } else {
//                    metadataMap.put("Album", "Unknown Album");
//                }
//
//                if (artist != null && !artist.isEmpty()) {
//                    metadataMap.put("Artist", artist);
//                } else {
//                    metadataMap.put("Artist", "Unknown Artist");
//                }
//
//                if (title != null && !title.isEmpty()) {
//                    metadataMap.put("Title", title);
//                } else {
//                    metadataMap.put("Title", "Unknown Title");
//                }
//
//                if (year != null && !year.isEmpty()) {
//                    metadataMap.put("Year", year);
//                } else {
//                    metadataMap.put("Year", "Unknown Year");
//                }
//
//                // **[Optional - For advanced cases/info] - Check for ID3v2 Tag specifically:**
//                /*
//                if (audioFile.getID3v2Tag() != null) {
//                    System.out.println("File has an ID3v2 tag.");
//                    // You could do something specific with the ID3v2 tag if needed, but usually not necessary for retrieval
//                    // ID3v2Tag v2tag = audioFile.getID3v2Tag();
//                    // Example: Accessing specific ID3v2 frames (more advanced)
//                    // String genreDescription = v2tag.frameOfType(ID3v24Frames.FRAME_ID_GENRE).getDescription();
//                } else {
//                    System.out.println("File does not have an ID3v2 tag (or JAudioTagger couldn't find one).");
//                    // It might still have ID3v1 or no tags at all.
//                }
//                */
//
//
//            } else {
//                System.out.println("No tags found in the MP3 file.");
//            }
//
//        } catch (CannotReadException | TagException | ReadOnlyFileModificationException |
//                 InvalidAudioFrameException | IOException | Exception e) {
//            System.err.println("Error extracting metadata: " + e.getMessage());
//            e.printStackTrace();
//            return null;
//        }
//        return metadataMap;
//    }
//
//    public static void main(String[] args) { // Main method for testing
//        String mp3FilePath = "path/to/your/mp3file.mp3"; // Replace with a test file
//        File mp3File = new File(mp3FilePath);
//        MP3MetadataExtractor extractor = new MP3MetadataExtractor();
//        Map<String, Object> metadata = extractor.extractMetadata(mp3File);
//        if (metadata != null) {
//            System.out.println("Metadata: " + metadata);
//        } else {
//            System.out.println("Metadata extraction failed.");
//        }
//    }
//}
//```
//
//**Key Changes and Explanations:**
//
//*   **Still using `audioFile.getTag()`:** We retain `audioFile.getTag()` as the primary method for getting the tag. This is generally the correct and easiest approach for most metadata retrieval scenarios.
//*   **Retrieving more metadata fields:** We now retrieve `Album`, `Artist`, `Title`, and `Year` using `tag.getFirst(FieldKey....)` for each. This shows how to access more common metadata attributes.
//*   **Optional ID3v2 Tag Check (Commented Out):** I've added commented-out code that demonstrates how you *could* explicitly check for and get the `ID3v2Tag` using `audioFile.getID3v2Tag()`. This is for informational purposes and if you ever need to do something very specific with ID3v2 tags.  For standard metadata retrieval, it's not usually necessary.
//*   **`ID3v24Frames` Import (Example, Not Strictly Necessary):**  The import `org.jaudiotagger.tag.id3.ID3v24Frames;` is included as an example.  If you were to work directly with `ID3v2Tag` objects, you might use classes like `ID3v24Frames` to access specific frames within ID3v2 tags (for more advanced operations).  For basic retrieval using `FieldKey`, you usually don't need to import these specific ID3v2 classes.
//
//**Regarding Tag Conversion and Heterogeneous Data:**
//
//You are right that tag heterogeneity is a real issue in music datasets. JAudioTagger *does* support tag manipulation, including the possibility of converting between tag versions.  However, **within this `MP3MetadataExtractor` class, we are focusing on *reading* metadata reliably, not *converting* tags**.
//
//**Tag Conversion as a Separate Operation (If Needed):**
//
//If you need to *convert* tags (e.g., to ensure all your MP3s have ID3v2.4 tags), you would do that as a *separate process* from metadata extraction.  You would:
//
//1.  **Read the `AudioFile`:** `AudioFile audioFile = AudioFileIO.read(mp3File);`
//2.  **Get the existing `Tag` (using `audioFile.getTag()`) or specific tag versions (like `audioFile.getID3v1Tag()`).**
//3.  **Create a *new* `ID3v24Tag()` (or the desired version) and copy the metadata from the existing tag to the new ID3v24 tag.**  JAudiotagger provides methods for copying metadata between tags.
//4.  **Remove the old tags from the `AudioFile` (if desired).**
//5.  **Set the new `ID3v24Tag` on the `AudioFile`:** `audioFile.setTag(newID3v24Tag);`
//6.  **Commit the changes to the file:** `audioFile.commit();`
//
//Tag conversion is a more involved process, and it's often done as a separate utility or batch process rather than being integrated directly into every metadata extraction operation.
//
//**In summary:**
//
//For your use case of retrieving metadata from MP3 files in a JavaFX application, the refined `MP3MetadataExtractor` class using `audioFile.getTag()` is generally sufficient and will reliably retrieve metadata from both ID3v1 and ID3v2 tags (prioritizing ID3v2) without needing explicit tag version handling in most cases.  If you have more specialized needs related to specific tag versions or tag conversion, you can explore the more advanced JAudioTagger API as needed.