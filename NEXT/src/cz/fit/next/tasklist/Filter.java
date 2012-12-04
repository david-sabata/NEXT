package cz.fit.next.tasklist;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.util.Base64;
import android.util.Base64InputStream;
import android.util.Base64OutputStream;
import cz.fit.next.backend.DateTime;

/**
 * Task list filter representation
 * 
 * Its toString and Filter(String) are used for task list
 * adapter filtering
 * 
 * @author David
 */
public class Filter implements Serializable {


	private static final long serialVersionUID = 871359392353215005L;




	protected String mTitle;

	protected String mDescription;

	protected DateTime mDateFrom;

	protected DateTime mDateUntil;

	protected boolean mIncludeSomeday;

	protected boolean mIncludeBlocked;

	protected String mProjectID;

	protected String mContext;



	public Filter() {
	}





	public void setDateFrom(DateTime date) {
		mDateFrom = date;
	}

	public DateTime getDateFrom() {
		return mDateFrom;
	}



	public void setDateUntil(DateTime date) {
		mDateUntil = date;
	}

	public DateTime getDateUntil() {
		return mDateUntil;
	}




	public void setProjectId(String projectId) {
		mProjectID = projectId;
	}

	public String getProjectId() {
		return mProjectID;
	}





	@Override
	public String toString() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			new ObjectOutputStream(out).writeObject(this);
			byte[] data = out.toByteArray();
			out.close();

			out = new ByteArrayOutputStream();
			Base64OutputStream b64 = new Base64OutputStream(out, Base64.DEFAULT);
			b64.write(data);
			b64.close();
			out.close();

			return new String(out.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


	public static Filter fromString(String s) {
		if (s == null)
			return null;

		try {
			ObjectInputStream ois = new ObjectInputStream(new Base64InputStream(new ByteArrayInputStream(s.getBytes()), Base64.DEFAULT));
			Object o = ois.readObject();
			ois.close();
			return (Filter) o;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}


}
