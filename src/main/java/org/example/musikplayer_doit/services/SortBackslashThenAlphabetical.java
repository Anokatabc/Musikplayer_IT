package org.example.musikplayer_doit.services;

import java.nio.file.Path;
import java.util.Comparator;

public class SortBackslashThenAlphabetical implements Comparator<Path> {


    //              C:\Coding\Visual Studio Code\resources\app\out = 5PC
    //              D:\music add\MediaHuman\Music\aku no = 4
    @Override
    public int compare(Path o1, Path o2) {
        int count1 = o1.getNameCount();
        int count2 = o2.getNameCount();
        if (count1 != count2){
            return Integer.compare(count1, count2);
        }
        return o1.toString().compareToIgnoreCase(o2.toString());
    }
//        old compare method
//        int count1 = countBackslashes(o1); = 3
//        int count2 = countBackslashes(o2); = 3
//        if (count1 != count2){
//            return Integer.compare(count1, count2);
//        }
//        return o1.toString().compareToIgnoreCase(o2.toString());


    private int countBackslashes (String string){
        int count = 0;
        for (char c : string.toCharArray()){
            if (c == '\\') count++;
        }
        return count;
    }

}
