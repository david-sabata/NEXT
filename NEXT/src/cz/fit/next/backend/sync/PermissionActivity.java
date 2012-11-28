package cz.fit.next.backend.sync;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class PermissionActivity extends Activity {
	
	private int REQUEST = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        final Intent intentToLaunch = intent.getParcelableExtra("intent");
        startActivityForResult(intentToLaunch, REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST) {
        	 Intent i = new Intent(SyncService.getInstance().getApplicationContext(), SyncService.class);
             i.putExtra("SyncAlarm", 1);
             SyncService.getInstance().getApplicationContext().startService(i);
             finish();
        } 
    }

}
