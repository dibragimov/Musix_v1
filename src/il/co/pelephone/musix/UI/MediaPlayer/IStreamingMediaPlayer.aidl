//AIDL file for StreamMediaPlayer
package il.co.pelephone.musix.UI.MediaPlayer;

// Interface for Streaming Player.
interface IStreamingMediaPlayer {
      
    // Check to see if service is playing audio
	boolean isPlaying();
	//Start playing audio
	void startAudio();
	//pause playing audio
	void pauseAudio();
	//6. The UI layer needs to be able to query for played time, and song ID (the other stuff it can take from the data layer) (Ron Srebro, Skype chat)
	String getSongID();
	//6. The UI layer needs to be able to query for played time, and song ID 
	int getPlayedTime();
	// stops the service
	void stop(); 
	//Start next audio
	void playNext();
	//Start previous audio
	void playPrevious();
	//repeat playing music after the end is reached
	void setRepeatPlayback(boolean repeating);
	//shuffle the playlist
	void setShufflePlaylist(boolean shuffling);
}