package com.openatk.libtrello;

import java.util.Date;

public class TrelloObject {
	
	public TrelloObject(){
		
	}
	
	public Object[] toObjectArray(){	
		return null;
	}
	
	public static Long DateToUnix(Date date){
		if(date == null) return 0L;
		return date.getTime();
	}
	
	public static Date UnixToDate(Long unix){
		if(unix == null) return new Date(0);
		return new Date(unix);
	}
}
