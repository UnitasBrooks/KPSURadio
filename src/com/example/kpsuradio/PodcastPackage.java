package com.example.kpsuradio;
import java.io.IOException;
import org.kpsu.kpsuradio.R;

import android.annotation.SuppressLint;
//import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ProgressBar;
@SuppressWarnings("deprecation")
public class PodcastPackage implements OnPreparedListener, OnAudioFocusChangeListener{
	
	protected Button button;
	protected boolean playingFlag;
	protected String mediaURL;
	protected String showName;
	protected MediaPlayer mp;
	protected Context actFrom = null;
	protected TextView duration;
	protected TextView showDuration;
	protected ImageButton playPause;
	protected ProgressBar pb;
	protected TextView total;
	protected SeekBar seek;
	
	public PodcastPackage() {
		button = null;
		playingFlag = false;
		mediaURL = null;
		showName = null;
		mp = null;
		duration = null;
		playPause = null;
		pb = null;
		total = null;
	}
	

	public void stopStream()
	{	
		
		if(mp != null)
		{
			pb.setVisibility(View.INVISIBLE);
			playingFlag = false;
			mp.stop();
			mp.release();
			mp = null;
			button.setBackgroundColor(Color.GRAY);
		}
		audioCheck();
	}
	
	@SuppressLint("NewApi")
	public void playStream()
	{
		// Important checks, make sure we are done scraping before calling seAudioStream
		if(mp == null && mediaURL != null)
		{
			pb.setVisibility(View.VISIBLE);
			
			button.setBackgroundColor(Color.GREEN);
			// Set up this media player object to stream music
			mp = new MediaPlayer();
			playingFlag = true;
			mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
			try {
				mp.setDataSource(mediaURL); // link				
				//mp.prepare();
				mp.prepareAsync(); // buffer
				mp.setOnPreparedListener(this);
				
		
				
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		audioCheck();
	}
	public void audioCheck() {
		// Checks audio focus
		if(mp != null)
		{
			AudioManager audioManager = (AudioManager)this.actFrom.getSystemService(Context.AUDIO_SERVICE);
			int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
			onAudioFocusChange(result);
		}
		
	}

	
	// This is where we actually play the media player when it is prepared
	public void onPrepared(MediaPlayer mp) {
		//Play Stream
		mp.start();
		pb.setVisibility(View.INVISIBLE);
		// run audio check here, that way if a audio event comes in from another app we can deal with it
		
		// Get the duration text view
		if(mp != null)
		{
			long totalTime = mp.getDuration();
			String stringTime = milliSecondsToTimer(totalTime);
			total.setText("/" + stringTime);
		}
		
		audioCheck();
	}
	
	public void onAudioFocusChange(int focusChange) {
	    switch (focusChange) {
	        case AudioManager.AUDIOFOCUS_GAIN:
	            // resume play back
	            if (mp != null  && !mp.isPlaying() && playingFlag != false)
	            {
	            	playingFlag = true;
	            	mp.start();
	            	mp.setVolume(1.0f, 1.0f);
	            }
	            break;

	        case AudioManager.AUDIOFOCUS_LOSS:
	            // Lost focus for an unbounded amount of time: stop playback and release media player
	            if (mp != null && mp.isPlaying())
	            {	
		            mp.stop();
		            mp = null;
		            playingFlag = false;
		            Drawable pause = actFrom.getResources().getDrawable( R.drawable.podplay );
		            //playPause.setBackground(pause);
		            playPause.setBackgroundDrawable(pause);
		            button.setBackgroundColor(Color.GRAY);
		            duration.setText(null);
		            total.setText(null);
		            seek.setProgress(0);
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


	public int getProgressPercentage(long currentDuration, long totalDuration) {
	    Double percentage = (double) 0;
	  
        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);
 
        // calculating percentage
        percentage =(((double)currentSeconds)/totalSeconds)*100;
 
        // return percentage
        return percentage.intValue();
	}


	public int progressToTimer(int progress, int totalDuration) {
	   int currentDuration = 0;
       totalDuration = (int) (totalDuration / 1000);
       currentDuration = (int) ((((double)progress) / 100) * totalDuration);
 
        // return current duration in milliseconds
       return currentDuration * 1000;
	}
	
	public String milliSecondsToTimer(long milliseconds){
        String finalTimerString = "";
        String secondsString = "";
        String minutesString = "";
 
        // Convert total duration into time
           int hours = (int)( milliseconds / (1000*60*60));
           int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
           int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
           // Add hours if there
           if(hours > 0){
               finalTimerString = hours + ":";
           }
 
           // Prepending 0 to seconds if it is one digit
           if(seconds < 10){
               secondsString = "0" + seconds;
           }
           else
               secondsString = "" + seconds;
           
           
           if(minutes < 10){
        	   minutesString = "0" + minutes;
           }
           else
        	   minutesString = "" + minutes;
           
        	   
 
           finalTimerString = finalTimerString + minutesString + ":" + secondsString;
 
        // return timer string
        return finalTimerString;
    }
	
}
