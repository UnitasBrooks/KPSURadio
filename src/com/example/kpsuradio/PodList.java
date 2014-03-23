package com.example.kpsuradio;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.widget.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.kpsu.kpsuradio.R;

import java.io.IOException;
@SuppressWarnings("deprecation")
public class PodList extends Activity implements SeekBar.OnSeekBarChangeListener {
	public  int PODNUM = 20;
	private Document doc = null;
	private PodcastPackage [] buttonArray = new PodcastPackage[PODNUM];
	private PodcastPackage currentlyPlaying = null;
	private ImageButton playPause = null;
	private SeekBar seek = null;
	private Drawable playImage = null;
	private Drawable stopImage = null; 
	private Handler mHandler = new Handler();
	private TextView duration = null;
	private ProgressBar pb = null;
	private TextView total = null;
	boolean pauseFlag = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		initializeViews();
		fillButtonArray();
		populateCasts();
		setGreyBack();
		currentlyPlaying = buttonArray[0]; // if no button is chosen currentlyPlaying is the most recent podcast
		seek.setOnSeekBarChangeListener(this);
	}
	
	private void initializeViews()
	{
		total = (TextView)findViewById(R.id.total);
		pb = (ProgressBar)findViewById(R.id.buffer);
		pb.setVisibility(View.INVISIBLE); // Set it to invisible
		playPause = (ImageButton) findViewById(R.id.playPause);
		playImage = getBaseContext().getResources().getDrawable( R.drawable.podplay );
		stopImage = getBaseContext().getResources().getDrawable( R.drawable.podpause );
		seek = (SeekBar)findViewById(R.id.seekBar1);
		duration = (TextView)findViewById(R.id.duration);
	}

	// Places the play icon on all the buttons
	private void setGreyBack()
	{
		
		for(int i = 0; i < PODNUM; i++)
		{
			buttonArray[i].button.setBackgroundColor(Color.GRAY);
		}
	}
	
	// Calls the async task
	public void populateCasts()
	{
		new grabCast(this).execute("http://kpsu.org/archives");
	}
	
	// Called from the async task, initialize the buttons with the scraped info
	public void setButton(String[] pods,String[] url)
	{
		 for(int i = 0; i < PODNUM; i++)
		 {
			 buttonArray[i].button.setText(pods[i]);
			 buttonArray[i].showName = pods[i];
			 buttonArray[i].mediaURL = url[i];
			 buttonArray[i].actFrom = this;
			 buttonArray[i].playPause = playPause; // allows seek bar to be manipulated through podpackage
			 buttonArray[i].pb = pb;
			 buttonArray[i].total = total;
			 buttonArray[i].duration = duration;
			 buttonArray[i].seek = seek;
		 }
	}
	
	public void playPod(View view)
	{
		setGreyBack();
		int id = view.getId(); // get the calling views id
		
		// Find which button was pressed
		for(int i  = 0; i < PODNUM; i++)
		{
			if(buttonArray[i].button.getId() == id)
			{
				currentlyPlaying = buttonArray[i];
			}
			else
			{
				buttonArray[i].playingFlag = false;
				buttonArray[i].stopStream();
			}
		}

		// Check if playing or not
		if(currentlyPlaying.playingFlag == true || currentlyPlaying.mp != null )
		{
			stop();
		}
		else
		{
			play(currentlyPlaying);
		}
		updateProgressBar();
	}
	
	
	public void imageButtonController(View view)
	{
		
		if(currentlyPlaying.mp != null && currentlyPlaying.playingFlag == true)
		{
			if(currentlyPlaying.mp.isPlaying())
			{
				currentlyPlaying.mp.pause();
				currentlyPlaying.playingFlag = false;
				pauseFlag = true;
			}
			else // Fixes the problem if they spam the button
			{
				stop();
			}
			// change image
			//playPause.setBackground(playImage);
			playPause.setBackgroundDrawable(playImage);
		}
		else
		{
			if(currentlyPlaying.mp != null)
			{
				currentlyPlaying.mp.start();
				currentlyPlaying.playingFlag = true;
				pauseFlag = false;
				playPause.setBackgroundDrawable(stopImage);
			}
			else
				play(currentlyPlaying);
			
			
		}
		updateProgressBar();
	}
	
	private void stop()
	{
		seek.setProgress(0);
		total.setText(null);
		duration.setText(null);
		currentlyPlaying.playingFlag = false; // update flag

		// Change image to play
		//playPause.setBackground(playImage);
		playPause.setBackgroundDrawable(playImage);
		
		// Stop stream and update duration
		currentlyPlaying.stopStream();
	}
	
	private void play(PodcastPackage currentlyPlaying)
	{
		
		
		// Play Stream
		currentlyPlaying.playStream();
		
		if(currentlyPlaying.mp != null)
		{
			currentlyPlaying.playingFlag = true; // update flag
			
			// Change image to stop
			playPause.setBackgroundDrawable(stopImage);
			// Reset seekbar
			//seek.setProgress(0);
		}
	}
	
	// Scraping thread class!
	public class grabCast extends AsyncTask<String, Object, String>
	{
		//private Context c = null;
		grabCast(Context context)
		{
			//c = context;
		}
		
		protected String doInBackground(String...url) 
		{
			  try 
			    {
			    	// Connect to URL
					doc = Jsoup.connect(url[0]).timeout(10*1000).get();
				} catch (IOException e) {
					e.printStackTrace();
				}
            String title = "KPSU Podcast";
            if(doc != null) {
		    	title = doc.title();
            }

		    	return title;	
		}
		
		protected void onPostExecute(String title)
		{
			String [] pods  = new String[PODNUM];
			String [] url   = new String[PODNUM];
			for(int i = 0; i < PODNUM; i++)
			{
				 //Element podcasts = doc.getElementById("download_submit");

				 Element name = doc.getElementsByClass("row").get(i);
				 Elements href = name.getElementsByTag("a");
						 
				 pods[i] = new String();
				 pods[i] = name.text();
				 url[i]  = new String();
				 url[i]  = href.attr("abs:href");
				 
				 for(int c = 0; c < pods[i].length(); c++)
				 {
					 if(pods[i].charAt(c) == ',')
					 {
						 pods[i] = pods[i].substring(0,c);
					 }
				 }
				
			}
			 setButton(pods,url); 
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.about, menu);
		return true;
	}
	
	// A shitty function that assigns buttons to our array of pcpckgs
	public void fillButtonArray()
	{
		 for(int i = 0; i < PODNUM; i++)
		 {
			 buttonArray[i] = new PodcastPackage();
		 }
		 buttonArray[0].button  = (Button) findViewById(R.id.button1);
		 buttonArray[1].button  = (Button) findViewById(R.id.button2);
		 buttonArray[2].button  = (Button) findViewById(R.id.button3);
		 buttonArray[3].button = (Button) findViewById(R.id.button4);
		 buttonArray[4].button = (Button) findViewById(R.id.button5);
		 buttonArray[5].button = (Button) findViewById(R.id.button6);
		 buttonArray[6].button = (Button) findViewById(R.id.button7);
		 buttonArray[7].button = (Button) findViewById(R.id.button8);
		 buttonArray[8].button = (Button) findViewById(R.id.button9);
		 buttonArray[9].button = (Button) findViewById(R.id.button10);
		 buttonArray[10].button = (Button) findViewById(R.id.button11);
		 buttonArray[11].button = (Button) findViewById(R.id.button12);
		 buttonArray[12].button = (Button) findViewById(R.id.button13);
		 buttonArray[13].button = (Button) findViewById(R.id.button14);
		 buttonArray[14].button = (Button) findViewById(R.id.button15);
		 buttonArray[15].button = (Button) findViewById(R.id.button16);
		 buttonArray[16].button = (Button) findViewById(R.id.button17);
		 buttonArray[17].button = (Button) findViewById(R.id.button18);
		 buttonArray[18].button = (Button) findViewById(R.id.button19);
		 buttonArray[19].button = (Button) findViewById(R.id.button20);
		
		 /*
		 buttonArray[0].button = button;
		 buttonArray[1].button = button2;
		 buttonArray[2].button = button3;
		 buttonArray[3].button = button4;
		 buttonArray[4].button = button5;
		 buttonArray[5].button = button6;
		 buttonArray[6].button = button7;
		 buttonArray[7].button = button8;
		 buttonArray[8].button = button9;
		 buttonArray[9].button = button10; */
	}

	public void updateProgressBar()
	{
		mHandler.postDelayed(mUpdateTimeTask, 100);
	}
	
	private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
        	if(currentlyPlaying.mp != null)
        	{
	            long totalDuration = currentlyPlaying.mp.getDuration();
	            long currentDuration = currentlyPlaying.mp.getCurrentPosition();
	            seek.setMax(100);
	            // Displaying Total Duration time
	            //songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
	            // Displaying time completed playing
	            //songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));
	
	            // Updating progress bar
	            int progress = (int)(currentlyPlaying.getProgressPercentage(currentDuration, totalDuration));
	            //Log.d("Progress", ""+progress);
	            seek.setProgress(progress);
	            
	    		duration.setText(currentlyPlaying.milliSecondsToTimer(currentDuration));
	    		 
	            // Running this thread after 100 milliseconds
	            mHandler.postDelayed(this, 100);
        	}
        }
     };
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		mHandler.removeCallbacks(mUpdateTimeTask);
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if(currentlyPlaying.mp != null)
		{
		    mHandler.removeCallbacks(mUpdateTimeTask);
	        int totalDuration = currentlyPlaying.mp.getDuration();
	        int currentPosition = currentlyPlaying.progressToTimer(seekBar.getProgress(), totalDuration);
	 
	        // forward or backward to certain seconds
	        currentlyPlaying.mp.seekTo(currentPosition);
	 
	        // update timer progress again
	        updateProgressBar();
		}
	}
}
