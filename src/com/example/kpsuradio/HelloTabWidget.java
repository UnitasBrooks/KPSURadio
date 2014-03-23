package com.example.kpsuradio;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import org.kpsu.kpsuradio.R;

@SuppressWarnings({ "deprecation", "unused" })
public class HelloTabWidget extends TabActivity implements OnTabChangeListener{

	
	protected TabHost tabHost = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);
	    
	    Resources res = getResources(); // Resource object to get Drawables
	    tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab
	    
	    tabHost.setBackgroundColor(Color.BLACK);
	    
	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, WebStream.class);

	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("Live Stream").setIndicator(null,
	                      res.getDrawable(R.drawable.stream))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, PodList.class);
	    spec = tabHost.newTabSpec("Podcasts").setIndicator(null,
	                      res.getDrawable(R.drawable.podcast))
	                  .setContent(intent);
	    tabHost.addTab(spec);
	    
	    /*intent = new Intent().setClass(this, Website.class);
	    spec = tabHost.newTabSpec("Web Site").setIndicator(null,
	                      res.getDrawable(R.drawable.web))
	                  .setContent(intent);
	    tabHost.addTab(spec);*/

	   
	    tabHost.setCurrentTab(0); 
	}

	 /*private void setSelectedTabColor() {
	        for(int i=0;i<tabHost.getTabWidget().getChildCount();i++)  
	        {  
	           tabHost.getTabWidget().getChildAt(i).clearFocus();  
	        }  
	                                           
	       tabHost.getTabWidget().getChildAt(tabHost.getCurrentTab()).setBackgroundColor(Color.DKGRAY); 
	                                             
	    }*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.hello_tab_widget, menu);
		return true;
	}

	@Override
	public void onTabChanged(String tabId) {
		// TODO Auto-generated method stub
		
	}

}
