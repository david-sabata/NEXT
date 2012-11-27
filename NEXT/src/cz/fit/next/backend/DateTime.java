package cz.fit.next.backend;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTime {

	/**
	 * Format supported for parsing
	 */
	private static final String[] FORMATS = new String[] {
			"yyyy-MM-dd'T'HH:mm:ss.SSSZ",
			"yyyy-MM-dd HH:mm:ss.SSSZ",
			"yyyy-MM-dd HH:mm:ss",
			"yyyy-MM-dd HH:mmZ",
			"yyyy-MM-dd HH:mm",
			"yyyy-MM-dd",
			"dd.MM.yyyy",
			"dd. MM. yyyy"
	};

	/**
	 * Timestamp (in milis) used for 'someday'
	 */
	public static final long SOMEDAY_TIMESTAMP = 9999999999999l;

	/**
	 * Flags used in db as datetime_type
	 */
	public static final String FLAG_DATE = "date";
	public static final String FLAG_DATETIME = "datetime";


	/**
	 * Timestamp
	 */
	protected long mTimestamp;


	/**
	 * Create from current time
	 */
	public DateTime() {
		mTimestamp = new Date().getTime();
	}

	/**
	 * Create from timestamp in miliseconds
	 */
	public DateTime(long time) {
		mTimestamp = time;
	}

	/**
	 * Create from string in one of supported formats
	 */
	public DateTime(String s) {
		boolean isOk = false;
		long parsed = 0;
		
		
		// someday
		try {
			parsed = Long.parseLong(s);
		} catch (NumberFormatException e) {}
		
		if (parsed == SOMEDAY_TIMESTAMP) {
			mTimestamp = SOMEDAY_TIMESTAMP;
			isOk = true;
		}
		

		// regular date(time)
		else {
			for (String format : FORMATS) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
					Date d = sdf.parse(s);
					mTimestamp = d.getTime();
					isOk = true;
					break;
				} catch (ParseException e) {
					// silently ignore
				}
			}
		}

		if (!isOk)
			throw new RuntimeException("Unable to parse DateTime [" + s + "]");
	}



	/**
	 * Is this date someday?
	 */
	public boolean isSomeday() {
		return mTimestamp == SOMEDAY_TIMESTAMP;
	}



	/**
	 * Returns date in unified format suitable for saving into file
	 * 
	 * If you need human readable format, use toLocaleString
	 */
	@Override
	public String toString() {
		return String.valueOf(mTimestamp);
	}


	/**
	 * Returns localized datetime string
	 * (Uses service to get localized DateFormatter)
	 */
	public String toLocaleDateTimeString() {
		return isSomeday() ?
				TasksModelService.getInstance().getLocalizedSomedayTime() :
				TasksModelService.getInstance().getLocalizedDateTime(new Date(mTimestamp));
	}

	/**
	 * Returns localized date string
	 * (Uses service to get localized DateFormatter)
	 */
	public String toLocaleDateString() {
		return isSomeday() ?
				TasksModelService.getInstance().getLocalizedSomedayTime() :
				TasksModelService.getInstance().getLocalizedDate(new Date(mTimestamp));
	}


	/**
	 * Returns time in miliseconds of GMT
	 */
	public long toMiliseconds() {
		return mTimestamp;
	}

}
