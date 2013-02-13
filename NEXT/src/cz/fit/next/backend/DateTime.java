package cz.fit.next.backend;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateTime implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6021425039338598508L;

	private final static long MILLISECS_PER_DAY = 24 * 60 * 60 * 1000;

	/**
	 * Timestamp (in milis) used for 'someday'
	 * 
	 * DO NOT CHANGE IF YOU DON'T UNDERSTAND THE IMPACTS
	 * the msecs value is used in TasksDataSource for ordering
	 */
	public static final long SOMEDAY_MILISECONDS = 666;

	/**
	 * Amount of miliseconds to represent 'all day' date. We do not 
	 * use miliseconds anywhere else, so it's safe to encode some 
	 * data in them, in this case 'all day' flag
	 * 
	 * DO NOT CHANGE IF YOU DON'T UNDERSTAND THE IMPACTS
	 * the msecs value is used in TasksDataSource for ordering
	 */
	public static final int ALLDAY_MILISECONDS = 333;


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
		} else if (isSomeday()) {
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
		} else if (isAllday()) {
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
	 * Returns localized datetime string. Uses date and/or time as expected
	 * (Uses service to get localized DateFormatter)
	 */
	public String toLocaleDateTimeString() {
		if (isSomeday())
			return TasksModelService.getInstance().getLocalizedSomedayTime();

		if (isAllday()) {
			if (isToday()) {
				return TasksModelService.getInstance().getLocalizedToday();
			}

			if (isTomorrow()) {
				return TasksModelService.getInstance().getLocalizedTomorrow();
			}

			if (isYesterday()) {
				return TasksModelService.getInstance().getLocalizedYesterday();
			}

			return TasksModelService.getInstance().getLocalizedDate(new Date(mTimestamp));
		} else {
			Date d = new Date(mTimestamp);

			if (isToday()) {
				return TasksModelService.getInstance().getLocalizedToday() + " " + TasksModelService.getInstance().getLocalizedTime(d);
			}

			if (isTomorrow()) {
				return TasksModelService.getInstance().getLocalizedTomorrow() + " " + TasksModelService.getInstance().getLocalizedTime(d);
			}

			if (isYesterday()) {
				return TasksModelService.getInstance().getLocalizedYesterday() + " " + TasksModelService.getInstance().getLocalizedTime(d);
			}

			return TasksModelService.getInstance().getLocalizedDateTime(d);
		}
	}

	/**
	 * Returns localized date string
	 * (Uses service to get localized DateFormatter)
	 */
	public String toLocaleDateString() {
		return isSomeday() ? TasksModelService.getInstance().getLocalizedSomedayTime() : TasksModelService.getInstance().getLocalizedDate(new Date(mTimestamp));
	}

	/**
	 * Returns localized time string
	 * (Uses service to get localized DateFormatter)
	 */
	public String toLocaleTimeString() {
		return isSomeday() ? TasksModelService.getInstance().getLocalizedSomedayTime() : TasksModelService.getInstance().getLocalizedTime(new Date(mTimestamp));
	}

	/**
	 * Returns number of days needed to be added to this object in order
	 * to make it the same date as the object in parameter
	 */
	public int diffInDays(DateTime to) {
		GregorianCalendar self = toCalendar();
		self.set(Calendar.HOUR_OF_DAY, 0);
		self.set(Calendar.MINUTE, 0);
		self.set(Calendar.SECOND, 0);
		self.set(Calendar.MILLISECOND, 0);
		long selfMs = self.getTimeInMillis();

		GregorianCalendar other = to.toCalendar();
		other.set(Calendar.HOUR_OF_DAY, 0);
		other.set(Calendar.MINUTE, 0);
		other.set(Calendar.SECOND, 0);
		other.set(Calendar.MILLISECOND, 0);
		long otherMs = other.getTimeInMillis();

		long diff = (selfMs - otherMs) / MILLISECS_PER_DAY;
		return (int) diff;
	}


	public boolean isToday() {
		DateTime today = new DateTime();
		return (diffInDays(today) == 0);
	}

	public boolean isTomorrow() {
		DateTime today = new DateTime();
		return (diffInDays(today) == 1);
	}

	public boolean isYesterday() {
		DateTime today = new DateTime();
		return (diffInDays(today) == -1);
	}



	/**

	 * Returns date/time as gregorian calendar
	 */
	public GregorianCalendar toCalendar() {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(mTimestamp);
		return cal;
	}

	public boolean equalsToDay(DateTime second) {
		if ((toCalendar().get(Calendar.YEAR) == second.toCalendar().get(Calendar.YEAR))
				&& (toCalendar().get(Calendar.MONTH) == second.toCalendar().get(Calendar.MONTH))
				&& (toCalendar().get(Calendar.DATE) == second.toCalendar().get(Calendar.DATE))) {

			return true;
		} else
			return false;

	}

	public boolean equalsToMinute(DateTime second) {
		if ((toCalendar().get(Calendar.YEAR) == second.toCalendar().get(Calendar.YEAR))
				&& (toCalendar().get(Calendar.MONTH) == second.toCalendar().get(Calendar.MONTH))
				&& (toCalendar().get(Calendar.DATE) == second.toCalendar().get(Calendar.DATE))
				&& (toCalendar().get(Calendar.HOUR_OF_DAY) == second.toCalendar().get(Calendar.HOUR_OF_DAY))
				&& (toCalendar().get(Calendar.MINUTE) == second.toCalendar().get(Calendar.MINUTE))) {

			return true;
		} else
			return false;

	}

}
