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
    			timeMilliSeconds += t * 86400000;
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
    	if (timeMilliSeconds > -1) timeMilliSeconds++;
    	
    	return timeMilliSeconds;
    }
}
