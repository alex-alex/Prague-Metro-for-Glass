package com.astudnicka.subwaystations.util;

import java.util.Date;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class TimeUtils {
	
	public static Date GetItemDate(final String date) {
		
	    final Calendar cal = Calendar.getInstance(TimeZone.getDefault());
	    
	    final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
	    dateFormat.setCalendar(cal);
	    
	    final SimpleDateFormat parseFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US);
	    parseFormat.setCalendar(cal);
	    
	    try {
	        return parseFormat.parse(dateFormat.format(Calendar.getInstance().getTime())+" "+date);
	    } catch (ParseException e) {
	        return null;
	    }
	}

	public static long secondsDiff(Date earlierDate, Date laterDate) {
	    if( earlierDate == null || laterDate == null ) return 0;
	    return (laterDate.getTime() - earlierDate.getTime())/1000;
	}

}
