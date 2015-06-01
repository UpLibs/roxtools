package roxtools;

import java.lang.ref.SoftReference;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;


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
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////

	static public Date parseHTTPDate(String dateValue) {
		return HTTPDateFormat.parseDate(dateValue) ;
	}
	
	static public String formatHTTPDate(Date date) {
		return HTTPDateFormat.formatDate(date) ;
	}
	
	// From org.apache.http.client.utils.DataUtils
	static private final class HTTPDateFormat {

		/**
		 * Date format pattern used to parse HTTP date headers in RFC 1123
		 * format.
		 */
		public static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";

		/**
		 * Date format pattern used to parse HTTP date headers in RFC 1036
		 * format.
		 */
		public static final String PATTERN_RFC1036 = "EEE, dd-MMM-yy HH:mm:ss zzz";

		/**
		 * Date format pattern used to parse HTTP date headers in ANSI C
		 * <code>asctime()</code> format.
		 */
		public static final String PATTERN_ASCTIME = "EEE MMM d HH:mm:ss yyyy";

		private static final String[] DEFAULT_PATTERNS = new String[] {
				PATTERN_RFC1123, PATTERN_RFC1036, PATTERN_ASCTIME };

		private static final Date DEFAULT_TWO_DIGIT_YEAR_START;

		public static final TimeZone GMT = TimeZone.getTimeZone("GMT");

		static {
			final Calendar calendar = Calendar.getInstance();
			calendar.setTimeZone(GMT);
			calendar.set(2000, Calendar.JANUARY, 1, 0, 0, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			DEFAULT_TWO_DIGIT_YEAR_START = calendar.getTime();
		}

		/**
		 * Parses a date value. The formats used for parsing the date value are
		 * retrieved from the default http params.
		 *
		 * @param dateValue
		 *            the date value to parse
		 *
		 * @return the parsed date or null if input could not be parsed
		 */
		public static Date parseDate(final String dateValue) {
			return parseDate(dateValue, null, null);
		}

		/**
		 * Parses the date value using the given date formats.
		 *
		 * @param dateValue
		 *            the date value to parse
		 * @param dateFormats
		 *            the date formats to use
		 * @param startDate
		 *            During parsing, two digit years will be placed in the
		 *            range <code>startDate</code> to
		 *            <code>startDate + 100 years</code>. This value may be
		 *            <code>null</code>. When <code>null</code> is given as a
		 *            parameter, year <code>2000</code> will be used.
		 *
		 * @return the parsed date or null if input could not be parsed
		 */
		public static Date parseDate(final String dateValue,
				final String[] dateFormats, final Date startDate) {

			if (dateValue == null)
				throw new NullPointerException("Date value");

			final String[] localDateFormats = dateFormats != null ? dateFormats
					: DEFAULT_PATTERNS;
			final Date localStartDate = startDate != null ? startDate
					: DEFAULT_TWO_DIGIT_YEAR_START;
			String v = dateValue;
			// trim single quotes around date if present
			// see issue #5279
			if (v.length() > 1 && v.startsWith("'") && v.endsWith("'")) {
				v = v.substring(1, v.length() - 1);
			}

			for (final String dateFormat : localDateFormats) {
				final SimpleDateFormat dateParser = DateFormatHolder
						.formatFor(dateFormat);
				dateParser.set2DigitYearStart(localStartDate);
				final ParsePosition pos = new ParsePosition(0);
				final Date result = dateParser.parse(v, pos);
				if (pos.getIndex() != 0) {
					return result;
				}
			}
			return null;
		}

		/**
		 * Formats the given date according to the RFC 1123 pattern.
		 *
		 * @param date
		 *            The date to format.
		 * @return An RFC 1123 formatted date string.
		 *
		 * @see #PATTERN_RFC1123
		 */
		public static String formatDate(final Date date) {
			return formatDate(date, PATTERN_RFC1123);
		}

		/**
		 * Formats the given date according to the specified pattern. The
		 * pattern must conform to that used by the {@link SimpleDateFormat
		 * simple date format} class.
		 *
		 * @param date
		 *            The date to format.
		 * @param pattern
		 *            The pattern to use for formatting the date.
		 * @return A formatted date string.
		 *
		 * @throws IllegalArgumentException
		 *             If the given date pattern is invalid.
		 *
		 * @see SimpleDateFormat
		 */
		public static String formatDate(final Date date, final String pattern) {
			if (date == null)
				throw new NullPointerException("Date");
			if (pattern == null)
				throw new NullPointerException("Patter");

			final SimpleDateFormat formatter = DateFormatHolder
					.formatFor(pattern);
			return formatter.format(date);
		}

		/** This class should not be instantiated. */
		private HTTPDateFormat() {
		}

		/**
		 * A factory for {@link SimpleDateFormat}s. The instances are stored in
		 * a threadlocal way because SimpleDateFormat is not threadsafe as noted
		 * in {@link SimpleDateFormat its javadoc}.
		 *
		 */
		final static class DateFormatHolder {

			private static final ThreadLocal<SoftReference<Map<String, SimpleDateFormat>>> THREADLOCAL_FORMATS = new ThreadLocal<SoftReference<Map<String, SimpleDateFormat>>>() {

				@Override
				protected SoftReference<Map<String, SimpleDateFormat>> initialValue() {
					return new SoftReference<Map<String, SimpleDateFormat>>(
							new HashMap<String, SimpleDateFormat>());
				}

			};

			/**
			 * creates a {@link SimpleDateFormat} for the requested format
			 * string.
			 *
			 * @param pattern
			 *            a non-<code>null</code> format String according to
			 *            {@link SimpleDateFormat}. The format is not checked
			 *            against <code>null</code> since all paths go through
			 *            {@link DateUtils}.
			 * @return the requested format. This simple dateformat should not
			 *         be used to {@link SimpleDateFormat#applyPattern(String)
			 *         apply} to a different pattern.
			 */
			public static SimpleDateFormat formatFor(final String pattern) {
				final SoftReference<Map<String, SimpleDateFormat>> ref = THREADLOCAL_FORMATS
						.get();
				Map<String, SimpleDateFormat> formats = ref.get();
				if (formats == null) {
					formats = new HashMap<String, SimpleDateFormat>();
					THREADLOCAL_FORMATS
							.set(new SoftReference<Map<String, SimpleDateFormat>>(
									formats));
				}

				SimpleDateFormat format = formats.get(pattern);
				if (format == null) {
					format = new SimpleDateFormat(pattern, Locale.US);
					format.setTimeZone(TimeZone.getTimeZone("GMT"));
					formats.put(pattern, format);
				}

				return format;
			}

		}

	}
	
}
