package cz.fit.next.backend;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTime {

	/**
	 * Format used for saving
	 */
	public static final String UNIFIED_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSZ";

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
	 * Underlying Date object
	 */
	protected Date mDate;


	/**
	 * Create from current time
	 */
	public DateTime() {
		mDate = new Date();
	}

	/**
	 * Create from date object
	 */
	public DateTime(Date date) {
		mDate = date;
	}

	/**
	 * Create from string in one of supported formats
	 */
	public DateTime(String s) {
		for (String format : FORMATS) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
				mDate = sdf.parse(s);
			} catch (ParseException e) {
			}
		}

		if (mDate == null)
			throw new RuntimeException("Unable to parse DateTime [" + s + "]");
	}


	/**
	 * Underlying Date object getter
	 */
	public Date getDate() {
		return mDate;
	}




	/**
	 * Returns date in unified format suitable for saving into file
	 * 
	 * If you need human readable format, use toLocaleString
	 */
	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat(UNIFIED_FORMAT, Locale.US);
		return sdf.format(mDate);
	}


	/**
	 * Returns localized date string
	 * (Uses service to get localized DateFormatter)
	 */
	public String toLocaleString() {
		return TasksModelService.getInstance().getLocalizedDateFormat().format(mDate);
	}




}
