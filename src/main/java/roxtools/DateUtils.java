package roxtools;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


final public class DateUtils {

	static final public DateFormat DATE_FORMAT_yyyyMMdd = new SimpleDateFormat("yyyyMMdd", Locale.US) ;
	static final public DateFormat DATE_FORMAT_yyyyMM = new SimpleDateFormat("yyyyMM", Locale.US) ;
	
	static final public DateFormat DATE_FORMAT_yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US) ;
	static final public DateFormat DATE_FORMAT_yyyyMMddHHmm = new SimpleDateFormat("yyyyMMddHHmm", Locale.US) ;
	
	static final public DateFormat DATE_FORMAT_yyyy_MM_dd = new SimpleDateFormat("yyyy/MM/dd", Locale.US) ;
	static final public DateFormat DATE_FORMAT_yyyy_MM = new SimpleDateFormat("yyyy/MM", Locale.US) ;
	
	static final public DateFormat DATE_FORMAT_yyyy_MM_dd_HH_mm_ss = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US) ;
	static final public DateFormat DATE_FORMAT_yyyy_MM_dd_HH_mm = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US) ;
	
	/////////////////////////////////////////////////////////////////////////////
	
	static public int getDate_Year(long timeMillis) {
		return Integer.parseInt( DATE_FORMAT_yyyyMM.format(timeMillis).substring(0,4) ) ;
	}
	
	static public int getDate_Month(long timeMillis) {
		return Integer.parseInt( DATE_FORMAT_yyyyMM.format(timeMillis).substring(4,4+2) ) ;
	}
	
	static public int getDate_Day(long timeMillis) {
		return Integer.parseInt( DATE_FORMAT_yyyyMMdd.format(timeMillis).substring(4+2,4+2+2) ) ;
	}
	
	static public int getDate_Hour(long timeMillis) {
		return Integer.parseInt( DATE_FORMAT_yyyyMMddHHmm.format(timeMillis).substring(4+2+2,4+2+2+2) ) ;
	}
	
	static public int getDate_Minute(long timeMillis) {
		return Integer.parseInt( DATE_FORMAT_yyyyMMddHHmm.format(timeMillis).substring(4+2+2+2,4+2+2+2+2) ) ;
	}
	
	static public int getDate_Second(long timeMillis) {
		return Integer.parseInt( DATE_FORMAT_yyyyMMddHHmmss.format(timeMillis).substring(4+2+2+2+2,4+2+2+2+2+2) ) ;
	}
	
	/////////////////////////////////////////////////////////////////////////////
	
	static public String formatDate_yyyyMMdd(long timeMillis) {
		return DATE_FORMAT_yyyyMMdd.format(new Date(timeMillis)) ;
	}
	
	static public String formatDate_yyyyMM(long timeMillis) {
		return DATE_FORMAT_yyyyMM.format(new Date(timeMillis)) ;
	}
	
	static public String formatDate_yyyyMMddHHmmss(long timeMillis) {
		return DATE_FORMAT_yyyyMMddHHmmss.format(new Date(timeMillis)) ;
	}
	
	static public String formatDate_yyyyMMddHHmm(long timeMillis) {
		return DATE_FORMAT_yyyyMMddHHmm.format(new Date(timeMillis)) ;
	}
	
	static public String formatDate_yyyy_MM_dd(long timeMillis) {
		return DATE_FORMAT_yyyy_MM_dd.format(new Date(timeMillis)) ;
	}
	
	static public String formatDate_yyyy_MM(long timeMillis) {
		return DATE_FORMAT_yyyy_MM.format(new Date(timeMillis)) ;
	}
	
	static public String formatDate_yyyy_MM_dd_HH_mm_ss(long timeMillis) {
		return DATE_FORMAT_yyyy_MM_dd_HH_mm_ss.format(new Date(timeMillis)) ;
	}
	
	static public String formatDate_yyyy_MM_dd_HH_mm(long timeMillis) {
		return DATE_FORMAT_yyyy_MM_dd_HH_mm.format(new Date(timeMillis)) ;
	}
	
	/////////////////////////////////////////////////////////////////////////////
	
	static public long parseDate_yyyyMMdd(String str) throws ParseException {
		return DATE_FORMAT_yyyyMMdd.parse(str).getTime() ;
	}
	
	static public long parseDate_yyyyMM(String str) throws ParseException {
		return DATE_FORMAT_yyyyMM.parse(str).getTime() ;
	}
	
	static public long parseDate_yyyyMMddHHmmss(String str) throws ParseException {
		return DATE_FORMAT_yyyyMMddHHmmss.parse(str).getTime() ;
	}
	
	static public long parseDate_yyyyMMddHHmm(String str) throws ParseException {
		return DATE_FORMAT_yyyyMMddHHmm.parse(str).getTime() ;
	}
	
	static public long parseDate_yyyy_MM_dd(String str) throws ParseException {
		return DATE_FORMAT_yyyy_MM_dd.parse(str).getTime() ;
	}
	
	static public long parseDate_yyyy_MM(String str) throws ParseException {
		return DATE_FORMAT_yyyy_MM.parse(str).getTime() ;
	}
	
	static public long parseDate_yyyy_MM_dd_HH_mm_ss(String str) throws ParseException {
		return DATE_FORMAT_yyyy_MM_dd_HH_mm_ss.parse(str).getTime() ;
	}
	
	static public long parseDate_yyyy_MM_dd_HH_mm(String str) throws ParseException {
		return DATE_FORMAT_yyyy_MM_dd_HH_mm.parse(str).getTime() ;
	}
	
	
}
