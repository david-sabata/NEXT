package cz.fit.next.backend;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateTime implements Serializable {

	/**
	 * Format supported for parsing
	 */
	//	private static final String[] FORMATS = new String[] {
	//			"yyyy-MM-dd'T'HH:mm:ss.SSSZ",
	//			"yyyy-MM-dd HH:mm:ss.SSSZ",
	//			"yyyy-MM-dd HH:mm:ss",
	//			"yyyy-MM-dd HH:mmZ",
	//			"yyyy-MM-dd HH:mm",
	//			"yyyy-MM-dd",
	//			"dd.MM.yyyy",
	//			"dd. MM. yyyy"
	//	};

	/**
	 * Timestamp (in milis) used for 'someday'
	 */
	private static final long SOMEDAY_MILISECONDS = 333;

	/**
	 * Amount of miliseconds to represent 'all day' date. We do not 
	 * use miliseconds anywhere else, so it's safe to encode some 
	 * data in them, in this case 'all day' flag
	 */
	private static final int ALLDAY_MILISECONDS = 666;


	/**
	 * Timestamp
	 */
	protected long mTimestamp;





	/**
	 * Create from current time 
	 * (miliseconds are reset)
	 */
	public DateTime() {
		mTimestamp = new Date().getTime();
		resetMilis();
	}

	/**
	 * Create from timestamp in miliseconds
	 * 
	 * Be careful about miliseconds as they are used internally 
	 * to represent various flags. If you want to set custom date/time 
	 * use default constructor and setTime/setDate methods.
	 */
	public DateTime(long time) {
		mTimestamp = time;
	}


	/**
	 * Returns miliseconds part of the timestamp
	 */
	private long getMilis() {
		return mTimestamp % 1000;
	}

	/**
	 * Reset miliseconds part of the timestamp, effectively 
	 * removing all flags (e.g. SOMEDAY, ALLDAY) 
	 */
	private void resetMilis() {
		mTimestamp -= getMilis();
	}




	/**
	 * Is this date someday?
	 */
	public boolean isSomeday() {
		return getMilis() == SOMEDAY_MILISECONDS;
	}

	/**
	 * Is this date all day?
	 */
	public boolean isAllday() {
		return getMilis() == ALLDAY_MILISECONDS;
	}



	/**
	 * Set that this datetime is someday
	 */
	public void setIsSomeday(boolean set) {
		if (set) {
			resetMilis();
			mTimestamp += SOMEDAY_MILISECONDS;
		}
		else if (isSomeday()) {
			resetMilis();
		}
	}

	/**
	 * Set that this datetime is allday
	 */
	public void setIsAllday(boolean set) {
		if (set) {
			resetMilis();
			mTimestamp += ALLDAY_MILISECONDS;
		}
		else if (isAllday()) {
			resetMilis();
		}
	}



	/**
	 * Set date 
	 * (removes SOMEDAY flag if set) 
	 * 
	 * @param year
	 * @param month 1..12
	 * @param day 1..30/31
	 */
	public void setDate(int year, int month, int day) {
		GregorianCalendar c = new GregorianCalendar();
		c.setTimeInMillis(mTimestamp);

		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month - 1); // JANUARY == 0
		c.set(Calendar.DAY_OF_MONTH, day);

		mTimestamp = c.getTimeInMillis();
		setIsSomeday(false);
	}

	/**
	 * Set time
	 * (removes SOMEDAY or ALLDAY flags if set)
	 * 
	 * @param hour
	 * @param minute
	 */
	public void setTime(int hour, int minute) {
		GregorianCalendar c = new GregorianCalendar();
		c.setTimeInMillis(mTimestamp);

		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, 0);

		mTimestamp = c.getTimeInMillis();
		resetMilis();
	}




	/**
	 * Returns time in miliseconds of GMT
	 */
	public long toMiliseconds() {
		return mTimestamp;
	}

	/**
	 * Returns timestamp in miliseconds of GMT
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
	 * Returns localized time string
	 * (Uses service to get localized DateFormatter)
	 */
	public String toLocaleTimeString() {
		return isSomeday() ?
				TasksModelService.getInstance().getLocalizedSomedayTime() :
				TasksModelService.getInstance().getLocalizedTime(new Date(mTimestamp));
	}


	/**

	 * Returns date/time as gregorian calendar
	 */
	public GregorianCalendar toCalendar() {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(mTimestamp);
		return cal;
	}

}
