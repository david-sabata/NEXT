package cz.fit.next;

import com.deaux.fan.FanView;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	private FanView fan; 
	private View mainView;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        
//        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        
        fan = (FanView) findViewById(R.id.fan_view);
        fan.setViews(R.layout.main, R.layout.fan);
        
        //get handle to main view
        mainView = (View) findViewById(R.id.mainLayout) ;
        
     	
        /*
         * fan.setFadeOnMenuToggle(true);
         * fan.setAnimationDuration(500);
         * fan.setIncludeDropshadow(false);
         */ 
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    
    public void unclick(View v) {
    	System.out.println("CLOSE");
    	if(v == mainView) {
    		fan.showMenu();
    	}
    }
    
    public void click(View v) {
    	System.out.println("OPEN");
    	if(v == mainView) {
    		fan.showMenu();
    	}
    } 
}



