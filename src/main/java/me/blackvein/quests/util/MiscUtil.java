package me.blackvein.quests.util;

public class MiscUtil {
    
    public static String getCapitalized(String s){
        
        if(s.isEmpty())
            return s;
        
        s = s.toLowerCase();
        String s2 = s.substring(0, 1);
        s2 = s2.toUpperCase();
        
        s = s.substring(1, s.length());
        
        return s2 + s;
        
    }
    
}
