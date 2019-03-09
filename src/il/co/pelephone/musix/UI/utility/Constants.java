package il.co.pelephone.musix.UI.utility;

public class Constants {
	public static final class Strings {
		public static final String MSG = "MESSAGE";
		public static final String URL = "URL";
		public static final String tPLAY = "tab_nowplaying";
		public static final String CATALOGMEDIAID = "catalogMediaID";
		public static final String SONGPLAYEXPECTEDTIME = "expectedPlay";
		public static final String DECRYPTED_PART = "decrypted";
		public static final String PLAYLIST_SONGS = "playlistSongs";
		public static final String MEDIA_DOWNLOAD_URL_PART = "/api/mobileapi/downloadMedia?userMediaId=%s&downloaded=0";
		public static final String ENCRYPTED_SONG_EXTENSION = "em3";
		public static final String DOT = ".";
		public static final String MUSIX_PATH = "/sdcard/musix/";
		public static final String INTENT_PLAYLIST_SONGS = "IntentPlaylistSongs";
		public static final String SELECTED_SONG = "il.co.pelephone.musix.data.Song";
		public static final String CURRENT_SONG = "current_song";
		public static final String CURRENT_SONG_DURATION = "current_song_duration";
		public static final String CURRENT_SONG_HAS_NEXT = "current_song_has_next";
		public static final String CURRENT_SONG_HAS_PREVIOUS = "current_song_has_prev";
		public static final String CURRENT_SONG_CURRENT_POSITION = "current_song_current_position";
		public static final String SYNC = "synching";
		public static final String MODE = "mode";
		public static final String SONGPLAYEDTIME = "playedTime";
	}
	
	public static final class Integers {
		public static final int UPDATE = 0;
		public static final int STOP = 1;
		public static final int START = 2;
		public static final int PAUSE = 3;
		public static final int SPIN = 4;
		public static final int STOPSPIN = 5;
		public static final int TROUBLEWITHAUDIO = 6;
		public static final int NO_NEXT_SONG = 7;
		public static final int NO_PREV_SONG = 8;
		public static final int CURRENT_SONG = 9;
		
		public static final int ALLSONGSPROCESSED = 21;
		public static final int ALLALBUMSPROCESSED = 22;
		public static final int ALLARTISTPROCESSED = 23;
		public static final int ALLGENRESPROCESSED = 24;
		public static final int ALLPLAYLISTSPROCESSED = 25;
		public static final int ONESONGPROCESSED = 26;
		
		public static final int MXAppModeNormal = 31;
		public static final int MXAppModeOffline = 32;
		public static final int MXAppModeDisabled = 33;
	}
}
