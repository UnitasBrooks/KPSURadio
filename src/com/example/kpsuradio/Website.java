package com.example.kpsuradio;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;
import org.kpsu.kpsuradio.R;

@SuppressWarnings("unused")
public class Website extends Activity {

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_website);
		
		/*TextView textView = new TextView(this);
		textView.setText("This is where the web site will be displayed");
		setContentView(textView);*/
	}
	
	// On click send URLs to callBrowser
	public void webSite(View view)
	{
		String url = "http://www.kpsu.org"; 
		callBrowser(url);
	}
	
	public void facebook(View view)
	{
		String url = "https://www.facebook.com/KPSUPORTLAND";
		callBrowser(url);
	}
	
	public void donate(View view)
	{
		String url = "https://www.foundation.pdx.edu/publicgift/kpsu.jsp";
		callBrowser(url);
	}
	
	public void twitter(View view)
	{
		String url = "https://twitter.com/KPSU_PDX";
		callBrowser(url);
	}
	
	// Calls up the phone browser
	public void callBrowser(String url)
	{
		Context context = getApplicationContext();  
		Intent intent = new Intent(Intent.ACTION_VIEW);  
		Uri u = Uri.parse(url);  
		intent.setData(u);  
		try {  
			  // Start the activity  
			  startActivity(intent);  
			} catch (ActivityNotFoundException e) {  
			  // Raise on activity not found  
			  Toast toast = Toast.makeText(context, "Browser not found.", Toast.LENGTH_SHORT);
			  toast.show();
			}  
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.website, menu);
		return true;
	}

}
