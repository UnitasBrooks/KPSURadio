package com.example.kpsuradio;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.kpsu.kpsuradio.R;

import java.io.IOException;

@SuppressWarnings("unused")
public class WebStream extends Activity implements OnPreparedListener, OnAudioFocusChangeListener{

	private MediaPlayer mp = null;
	private ProgressBar pb = null;
	private Document doc = null;
	private String dj = null;
	private TextView error = null;

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR); 
		setContentView(R.layout.activity_web_stream);
		
		// Create a progress bar and find it by ID
		pb = new ProgressBar(this);
		pb = (ProgressBar)findViewById(R.id.progressBar1);
		pb.setVisibility(View.INVISIBLE); // Set it to invisible
		
		if(isNetworkAvailable() == true)
		{
			getPlayingThread();
		}
		else
		{
			error = (TextView) findViewById(R.id.error);
			error.setText("No internet connection found, please connect to the internet and restart the app");
		}
		
			
	}
	
	private boolean isNetworkAvailable() 
	{
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null;
	}
	
	public void getPlayingThread()
	{
	    new DownloadTask().execute("http://www.kpsu.org/");		
	}
	
	public class DownloadTask extends AsyncTask<String, Object, String>
	{
		protected String doInBackground(String... url) 
		{
            while(doc == null) {
                try
                {
                    // Connect to url
                    doc = Jsoup.connect(url[0]).timeout(10*1000).get();
                    System.out.println("");
                } catch (IOException e) {
                    e.printStackTrace();

                }
            }

            String title = doc.title();

	    	return title;
		}
		
		protected void onPostExecute(String title)
		{
			try 
			{
				// DJ name
				Element onAir = doc.getElementById("on-air-dj");
				Elements onAirDj = onAir.getElementsByTag("a");
				String onAirString = onAirDj.text();
				// Check to make sure it is not marked as span
				if(onAirString.equals(""))
				{
					onAirDj = onAir.getElementsByTag("span");
					onAirString = onAirDj.text();
				}
				final TextView article = (TextView)findViewById(R.id.textView1);
			    article.setText(onAirString);
			    
			    // Show name
			    Element onAirProg = doc.getElementById("on-air-program");
				Elements onAirProgTag = onAirProg.getElementsByTag("a");
				String onAirProgString = onAirProgTag.text();
				// Check to make sure it is not marked as span
				if(onAirProgString.equals(""))
				{
					onAirProgTag = onAirProg.getElementsByTag("span");
					onAirProgString = onAirProgTag.text();
				}
				final TextView progView = (TextView)findViewById(R.id.textView3);
			    progView.setText(onAirProgString);
			    
			    // Up Next
			    Element upNext = doc.getElementById("on-next");
				Elements upNextTag = upNext.getElementsByTag("a");
				String upNextString = upNextTag.text();
				if(upNextString.equals(""))
				{
					upNextTag = upNext.getElementsByTag("span");
					upNextString = upNextTag.text();
				}
				final TextView nextView = (TextView)findViewById(R.id.textView4);
			    nextView.setText(upNextString);
			    
			} 
			catch (Exception ex) 
			{
				ex.printStackTrace();
			    ((TextView)findViewById(R.id.textView1)).setText("error");
			}
		}
	}
	
	// Play on click
	public void play(View view) throws IllegalStateException, IOException
	{	
		audioCheck();
		
		ImageButton button  = (ImageButton) findViewById(R.id.imageButton2);
		getPlayingThread();
		if(mp == null)
		{
			Drawable img = getBaseContext().getResources().getDrawable( R.drawable.stoppress );
			button.setBackgroundDrawable(img);
			playStream();
		}
		else
		{
			pb.setVisibility(View.INVISIBLE);
			Drawable img = getBaseContext().getResources().getDrawable( R.drawable.playpress );
			button.setBackgroundDrawable(img);
			stopStream();
		}
	}
	
	public void playStream()
	{
		if(mp == null)
		{
			// While it is loading show the PB
			pb.setVisibility(View.VISIBLE);
			
			// Set up this media player object to stream music
			mp = new MediaPlayer();
			mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
			try {
				mp.setDataSource("http://stream.kpsu.org:8080/listen"); // link				
				//mp.prepare();
				mp.prepareAsync(); // buffer
				mp.setOnPreparedListener(this);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		audioCheck();
	}
	
	// Stop on click
	public void stop(View view)
	{
		pb.setVisibility(View.INVISIBLE);
		if(mp != null)
		stopStream();
	}
	
	public void stopStream()
	{
		if(mp != null)
		{
			mp.stop();
			mp.release();
			mp = null;
		}
	}
	
	public void audioCheck()
	{
		// Checks audio focus
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
		onAudioFocusChange(result);
	}

	@Override
	// This is where we actually play the media player when it is prepared
	public void onPrepared(MediaPlayer mp) {
		//Play Stream
		mp.start();
		// Show to the progress bar
		pb.setVisibility(View.INVISIBLE);
		// run audio check here, that way if a audio event comes in from another app we can deal with it
		audioCheck();
	}


	public void onAudioFocusChange(int focusChange) {
	    switch (focusChange) {
	        case AudioManager.AUDIOFOCUS_GAIN:
	            // resume play back
	            if (mp != null  && !mp.isPlaying())
	            {
	            	mp.start();
	            	mp.setVolume(1.0f, 1.0f);
	            }
	            break;

	        case AudioManager.AUDIOFOCUS_LOSS:
	            // Lost focus for an unbounded amount of time: stop playback and release media player
	            if (mp != null && mp.isPlaying()) 
	            {	
	            	ImageButton button  = (ImageButton) findViewById(R.id.imageButton2);
	            	pb.setVisibility(View.INVISIBLE);
	    			Drawable img = getBaseContext().getResources().getDrawable( R.drawable.playpress );
	    			button.setBackgroundDrawable(img);
	            	mp.stop();
	            	mp = null;
	            }
	            break;

	        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
	            // Lost focus for a short time, but we have to stop
	            // playback. We don't release the media player because playback
	            // is likely to resume
	            if (mp != null && mp.isPlaying()) mp.pause();
	            break;

	        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
	            // Lost focus for a short time, but it's ok to keep playing
	            // at an attenuated level
	            if (mp != null && mp.isPlaying()) mp.pause();
	            break;
	    }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.web_stream, menu);
		return true;
	}
	
}
