package org.example.musikplayer_doit.model;


import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/**
 * @param mp3Paths Set(Path) containing a list of all unique Paths containing mp3 files
 * @param folderMp3Count Map(Path, Integer) containing all Paths with respective mp3 file count. 0 = is no mp3 folder
 * @param mp3Count Integer count of all mp3 files scanned
 */
public record Mp3FolderInfo(Set<Path> mp3Paths, Map<Path, Integer> folderMp3Count, int mp3Count) {}
