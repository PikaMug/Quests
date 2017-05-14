package me.blackvein.quests.util;

import java.util.LinkedList;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;

public class MiscUtil {

    public static String getCapitalized(String s) {

        if (s.isEmpty()) {
            return s;
        }

        s = s.toLowerCase();
        String s2 = s.substring(0, 1);
        s2 = s2.toUpperCase();

        s = s.substring(1, s.length());

        return s2 + s;

    }

    //Time: 7d 24h 5m 10s 20ms 
    public static long getTimeFromString(String string) {
        //if it returns -1 then the string is incorrect.
        long timeMilliSeconds = -1;
        //replace 2 or more spaces with one space.
        string = string.replaceAll("[ ]{2,}", " ");

        String[] dates = string.split(" ");

        for (String date : dates) {
            String num = date.split("[a-zA-Z]+")[0];
            String type = date.split("[0-9]+")[1];

            int t = 0;
            try {
                t = Math.abs(Integer.parseInt(num));
            } catch (NumberFormatException e) {
            }

            if (type.equals("d")) {
                timeMilliSeconds += t * 86400000L;
            } else if (type.equals("h")) {
                timeMilliSeconds += t * 3600000;
            } else if (type.equals("m")) {
                timeMilliSeconds += t * 60000;
            } else if (type.equals("s")) {
                timeMilliSeconds += t * 1000;
            } else if (type.equals("ms")) {
                timeMilliSeconds += t;
            }
        }

        //To balance the -1 at the beginning.
        if (timeMilliSeconds > -1) {
            timeMilliSeconds++;
        }

        return timeMilliSeconds;
    }

    public static String getProperMobName(EntityType type) {

        String name = type.name().toLowerCase();

        name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        while (fixUnderscore(name) != null) {
            name = fixUnderscore(name);
        }

        return name;
    }

    public static EntityType getProperMobType(String properName) {

        properName = properName.replaceAll("_", "").toUpperCase();
        for (EntityType et : EntityType.values()) {
        	
            if (et.isAlive() && et.name().replaceAll("_", "").equalsIgnoreCase(properName)) {
                return et;
            }

        }

        return null;

    }

    private static String fixUnderscore(String s) {

        int index = s.indexOf('_');
        if (index == -1) {
            return null;
        }

        s = s.substring(0, (index + 1)) + Character.toUpperCase(s.charAt(index + 1)) + s.substring(index + 2);
        s = s.replaceFirst("_", "");

        return s;
    }

    public static String concatArgArray(String[] args, int startingIndex, int endingIndex, char delimiter) {

        String s = "";

        for (int i = startingIndex; i <= endingIndex; i++) {

            s += args[i] + delimiter;

        }

        s = s.substring(0, s.length());

        return s.trim().equals("") ? null : s.trim();
    }
    
    public static LinkedList<String> makeLines(String s, String wordDelimiter, int lineLength, ChatColor lineColor) {
        
        LinkedList<String> toReturn = new LinkedList<String>();
        String[] split = s.split(wordDelimiter);
        String line = "";
        int currentLength = 0;
        
        for (String piece : split) {
            
            if ((currentLength + piece.length()) > (lineLength + 1)) {
                toReturn.add(lineColor + line.replaceAll("^" + wordDelimiter, ""));
                line = piece + wordDelimiter;
                currentLength = piece.length() + 1;
            } else {
                line += piece + wordDelimiter;
                currentLength += piece.length() + 1;
            }
            
        }
        
        if(line.equals("") == false)
            toReturn.add(lineColor + line);
        
        return toReturn;
        
    }

}