package il.co.pelephone.musix.UI;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import il.co.pelephone.musix.UI.widget.MusixTabHost;

public class MXUIMusixMainScreen extends TabActivity {
	
	private MusixTabHost mTabHost;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mTabHost = (il.co.pelephone.musix.UI.widget.MusixTabHost)findViewById(android.R.id.tabhost);// getTabHost();  
        
        showMainScreen();
    }

	private void showMainScreen() {
		Intent myMusicIntent = new Intent(MXUIMusixMainScreen.this, MXUIMyMusicScreen.class);
        Intent musicCatalogIntent = new Intent(MXUIMusixMainScreen.this, MXUIMusicCatalogScreen.class);
        Intent nowPlayingIntent = new Intent(MXUIMusixMainScreen.this, MXUINowPlayingScreen.class);
        
        mTabHost.addTab(mTabHost.newMusixTabSpec("tab_nowplaying").setIndicator(getResources().getString(R.string.tab_nowplaying), getResources().getDrawable(R.drawable.playing_now_tab)).setContent(nowPlayingIntent));//R.id.textview3));
        mTabHost.addTab(mTabHost.newMusixTabSpec("tab_musiccatalog").setIndicator(getResources().getString(R.string.tab_musiccatalog), getResources().getDrawable(R.drawable.music_catalog_tab)).setContent(musicCatalogIntent));//R.id.textview2));    
        mTabHost.addTab(mTabHost.newMusixTabSpec("tab_mymusic").setIndicator(getResources().getString(R.string.tab_mymusic), getResources().getDrawable(R.drawable.my_music_tab)).setContent(myMusicIntent));//R.id.textview1));
        mTabHost.setCurrentTab(2);
	}

////right now these methods are not needed
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	////right now these methods are not needed
	@Override
	protected void onStart() {
		super.onStart();
	}
	
	public void setCurrentTab(Tabs currentTab){
		switch (currentTab) {
			case NowPlaying:
				mTabHost.setCurrentTab(0);
				break;

			case MusicCatalog:
				mTabHost.setCurrentTab(1);
				break;
				
			case MyMusic:
				mTabHost.setCurrentTab(2);
				break;
				
			default:
				break;
		}
	}
	
	public enum Tabs {
		NowPlaying ,
		MusicCatalog,
		MyMusic
	}
}