package il.co.pelephone.musix.UI.utility;

import java.util.ArrayList;
import java.util.Collections;

import android.util.Log;

public class MusixPlayList {
	
	private ArrayList<String> playlistSongIDs;
	private ArrayList<String> originalPlaylistSongIDs;
	private int currentID; 
	private boolean shuffled;
	private boolean repeating;
	private String TAG="MusixPlayList";
	
	public boolean isShuffled() {
		return shuffled;
	}

	@SuppressWarnings("unchecked")
	public void setShuffled(boolean shuffled) {
		this.shuffled = shuffled;
		if(shuffled){
			originalPlaylistSongIDs = (ArrayList<String>) playlistSongIDs.clone();
			Collections.shuffle(playlistSongIDs);
			Log.d(TAG, "list shuffled");
		}
		else{
			if(originalPlaylistSongIDs != null){
				playlistSongIDs = (ArrayList<String>) originalPlaylistSongIDs.clone();
				Log.d(TAG, "list was restiored to previous state (not shuffled)");
			}
		}
	}

	public boolean isRepeating() {
		return repeating;
	}

	public void setRepeating(boolean repeating) {
		this.repeating = repeating;
		Log.d(TAG, "list will be repeated: "+this.repeating);
	}

	public MusixPlayList(){
		playlistSongIDs = new ArrayList<String>();
		currentID = 0;
	}
	
	public void add(String mediaID){
		playlistSongIDs.add(mediaID);
	}
	
	public void add(String[] mediaIDs){
		for (String mediaID : mediaIDs) {
			playlistSongIDs.add(mediaID);
		}
	}
	
	public boolean hasNext(){
		if(repeating)//// if repeating
			return true;//// always has next
		
		if(playlistSongIDs.size() > 0 && currentID < (playlistSongIDs.size()-1))
			return true;
		
		return false;
	}
	
	public boolean hasPrevious(){
		if(repeating)//// if repeating
			return true;//// always has next
		
		if(playlistSongIDs.size() > 0 && currentID > 0)
			return true;
		
		return false;
	}
	
	public String getNext(){
		if(!hasNext())
			return null;
		
		currentID++;
		if(repeating && currentID >= playlistSongIDs.size())////only if repeating is set to true
			currentID = 0;////start at the beginning
		
		return getCurrent();
	}
	
	public String peekNext(){
		if(!hasNext())
			return null;
		
		int nextID = currentID+1;
		if(repeating && nextID >= playlistSongIDs.size())////only if repeating is set to true
			nextID = 0;////start at the beginning
		
		return playlistSongIDs.get(nextID);
	}
	
	public String getPrevious(){
		if(!hasPrevious())
			return null;
		
		currentID--;
		if(repeating && currentID < 0)////only if repeating is set to true
			currentID = playlistSongIDs.size()-1;////start at the end
		
		return getCurrent();
	}
	
	public String getCurrent(){
		if(playlistSongIDs.size() < 1 || currentID < 0)
			return null;
		Log.d(TAG, "current song is: "+playlistSongIDs.get(currentID));
		return playlistSongIDs.get(currentID); 
	}
	
	public boolean setCurrent(String current){
		if(playlistSongIDs.size() < 1)
			return false;
		
		for (int i=0; i<playlistSongIDs.size(); i++) {
			if(playlistSongIDs.get(i).equals(current)){
				currentID = i;
				return true;
			}
		}
		return false;
	}
}
