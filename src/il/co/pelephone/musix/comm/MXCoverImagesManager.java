package il.co.pelephone.musix.comm;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class MXCoverImagesManager {

	private static MXCoverImagesManager _instance;
	
	public static final int SIZE_60_60 = 1;
	public static final int SIZE_110_110 = 2;
	
	public final String DIR_PATH_ALBUM = "/sdcard/musix/images/albums/";
	public final String DIR_PATH_PLAYLIST = "/sdcard/musix/images/playlists/";
	private final String DIR_PATH_60 = "60_60/";
	private final String DIR_PATH_110 = "110_110/";
	public final String SERVER_PLAYLIST_PATH = "/covers/playlist/";
	public final String SERVER_ALBUM_PATH = "/covers/album/";
	public final String SERVER_SUBPATH_60 = "/60_60.png";
	public final String SERVER_SUBPATH_110 = "/110_110.png";
	
	private String TAG ="MXCoverImagesManager";
	
	private HashMap<String, Bitmap> albumCovers60;
	private HashMap<String, Bitmap> playlistCovers60;
	private HashMap<String, Bitmap> albumCovers110;
	private HashMap<String, Bitmap> playlistCovers110;
	
	private int counterForAlbums = 0;
	private int counterForPlaylists = 0;
	
	private IMXImageCallback callback;

	public static MXCoverImagesManager getInstance(){
		if(_instance==null)
			_instance = new MXCoverImagesManager();
		
		return _instance;
	}
	
	private MXCoverImagesManager() {
		File dir = new File(DIR_PATH_ALBUM+DIR_PATH_60);
		if(!dir.exists())
			dir.mkdirs();
		dir = new File(DIR_PATH_ALBUM+DIR_PATH_110);
		if(!dir.exists())
			dir.mkdirs();
		dir = new File(DIR_PATH_PLAYLIST+DIR_PATH_60);
		if(!dir.exists())
			dir.mkdirs();
		dir = new File(DIR_PATH_PLAYLIST+DIR_PATH_110);
		if(!dir.exists())
			dir.mkdirs();
		
		playlistCovers60 = new HashMap<String, Bitmap>();
		playlistCovers110 = new HashMap<String, Bitmap>();
		albumCovers60 = new HashMap<String, Bitmap>();
		albumCovers110 = new HashMap<String, Bitmap>();
	}
	
	private boolean albumImageLocallyExists(String id, ImageSize size){
		if(size.equals(ImageSize.SIZE_60)){
			if(albumCovers60.containsKey(id))
				return true;
			////if not - now check in file system
			File img = new File(DIR_PATH_ALBUM + DIR_PATH_60+id);
			return img.exists();
		}
		else if (size.equals(ImageSize.SIZE_110)){
			if(albumCovers110.containsKey(id))
				return true;
			////if not - now check in file system
			File img = new File(DIR_PATH_ALBUM + DIR_PATH_110+id);
			return img.exists();
		}
		Log.d(TAG, "no such album image");
		return false;
	}
	
	private boolean playlistImageLocallyExists(String id, ImageSize size){
		if(size.equals(ImageSize.SIZE_60)){
			if(playlistCovers60.containsKey(id))
				return true;
			////if not - now check in file system
			File img = new File(DIR_PATH_PLAYLIST + DIR_PATH_60+id);
			return img.exists();
		}
		else if (size.equals(ImageSize.SIZE_110)){
			if(playlistCovers110.containsKey(id))
				return true;
			////if not - now check in file system
			File img = new File(DIR_PATH_PLAYLIST + DIR_PATH_110+id);
			return img.exists();
		}
		Log.d(TAG, "no such album image");
		return false;
	}
	
	private void getImageLocally(String id, ImageSize size, boolean isPlaylistImage) {
		if(size.equals(ImageSize.SIZE_60)){
			
			if(isPlaylistImage){
//				if(playlistCovers60==null)
//					playlistCovers60 = new HashMap<String, Bitmap>();

				if(playlistCovers60.containsKey(id))
					return;
				
				File img = new File(DIR_PATH_PLAYLIST + DIR_PATH_60+id);
				if (!img.exists())
//					throw new RuntimeException("image file does not exist");
					return;
				Bitmap imgDrawable = BitmapFactory.decodeFile(DIR_PATH_PLAYLIST + DIR_PATH_60+id);
				playlistCovers60.put(id, imgDrawable);
			}
			else{
//				if(albumCovers60==null)
//					albumCovers60 = new HashMap<String, Bitmap>();

				if(albumCovers60.containsKey(id))
					return;
				
				File img = new File(DIR_PATH_ALBUM + DIR_PATH_60+id);
				if (!img.exists())
//					throw new RuntimeException("image file does not exist");
					return;
				Bitmap imgDrawable = BitmapFactory.decodeFile(DIR_PATH_ALBUM + DIR_PATH_60+id);
				albumCovers60.put(id, imgDrawable);
			}
			
		}
		else if (size.equals(ImageSize.SIZE_110)){
			
			if(isPlaylistImage){
//				if(playlistCovers110==null)
//					playlistCovers110 = new HashMap<String, Bitmap>();

				if(playlistCovers110.containsKey(id))
					return;
				
				File img = new File(DIR_PATH_PLAYLIST + DIR_PATH_110+id);
				if (!img.exists())
//					throw new RuntimeException("image file does not exist");
					return;
				Bitmap imgDrawable = BitmapFactory.decodeFile(DIR_PATH_PLAYLIST + DIR_PATH_110+id);
				playlistCovers110.put(id, imgDrawable);
			}
			else{
//				if(albumCovers110==null)
//					albumCovers110 = new HashMap<String, Bitmap>();

				if(albumCovers110.containsKey(id))
					return;
				
				File img = new File(DIR_PATH_ALBUM + DIR_PATH_110+id);
				if (!img.exists())
//					throw new RuntimeException("image file does not exist");
					return;
				Bitmap imgDrawable = BitmapFactory.decodeFile(DIR_PATH_ALBUM + DIR_PATH_110+id);
				albumCovers110.put(id, imgDrawable);
			}
		}
	}
	
	private String getStringUrl(ImageSize size, String id, boolean isPlaylistImage){
		byte env;
		String url = null;
		env=MXEnv.getEnv();
		if ( env == MXEnv.ENV_DEV )
		{
			url=MXCommLayer.TRIPLAY_URL_DEV;
		}
		else if ( env == MXEnv.ENV_TEST )
		{
			url=MXCommLayer.TRIPLAY_URL_TEST;			
		}
		else
			url=MXCommLayer.TRIPLAY_URL_PROD;
		
		if(isPlaylistImage)
			url = url+SERVER_PLAYLIST_PATH+id;
		else
			url = url+SERVER_ALBUM_PATH+id;
		
		if(ImageSize.SIZE_110.equals(size))
			url = url+SERVER_SUBPATH_110;
		else
			url = url+SERVER_SUBPATH_60;
		
		return url;
	}
	
	protected void retrieveImageFromServer(ImageSize size, String id, boolean isPlaylistImage){
		String imageUrl = getStringUrl(size, id, isPlaylistImage);
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(imageUrl);
		
		request.setHeader("User-Agent", "Mozilla/5.0 (Linux; U; Android 1.5; iw-il; GT-I5700");
		
		Bitmap img = null;
		try {
			HttpResponse response =  client.execute(request);
			BufferedInputStream in = new BufferedInputStream(response.getEntity().getContent());
			int imgLength = in.available();
			byte[] imgBytes = new byte[imgLength];
			in.read(imgBytes);
			Log.d(TAG, "image read, size: "+imgBytes.length);
			img = BitmapFactory.decodeStream(in);
			if(img != null){
				Log.d(TAG, "bitmap image is not null");
				ByteArrayOutputStream baos = new ByteArrayOutputStream();   
				img.compress(Bitmap.CompressFormat.PNG, 100, baos); //bm is the bitmap object    
				imgBytes = baos.toByteArray();  
				saveImageLocally(size, id, isPlaylistImage, imgBytes);
				getImageLocally(id, size, isPlaylistImage);//// puts it into album or playlist hashmap
			}
		} catch (ClientProtocolException e) {
			Log.d(TAG, "ClientProtocolException"+e);
			e.printStackTrace();
		} catch (IOException e) {
			Log.d(TAG, "IOException"+e);
			e.printStackTrace();
		}
		if(!isPlaylistImage){
			counterForAlbums--;
			if(counterForAlbums < 1){
				HashMap<String, Bitmap> images = (size.equals(ImageSize.SIZE_60)) ? albumCovers60 : albumCovers110;
				callback.imagesReceived(images, isPlaylistImage);
			}
		}
		else{
			counterForPlaylists--;
			if(counterForPlaylists < 1){
				HashMap<String, Bitmap> images = (size.equals(ImageSize.SIZE_60)) ? playlistCovers60 : playlistCovers110;
				callback.imagesReceived(images, isPlaylistImage);
			}
		}
	}
	
	public void getImagesForAlbums(IMXImageCallback callback, String[] albumIDs, final ImageSize size){
		this.callback = callback;
		counterForAlbums = 0;
		boolean isDonwloadingFromServer = false;
		for (final String id : albumIDs) {
			if(albumImageLocallyExists(id, size)){
				getImageLocally(id, size, false);
			}
			else{
				isDonwloadingFromServer = true;
				Runnable r = new Runnable() {
					
					@Override
					public void run() {
						counterForAlbums++;
						retrieveImageFromServer(size, id, false);
					}
				};
				Thread t = new Thread(r);
				t.start();
			}
		}
		if(!isDonwloadingFromServer && counterForAlbums < 1){
			HashMap<String, Bitmap> images = (size.equals(ImageSize.SIZE_60)) ? albumCovers60 : albumCovers110;
			callback.imagesReceived(images, false);
		}
	}
	
	public void getImagesForPlaylists(IMXImageCallback callback, String[] playlistIDs, final ImageSize size){
		this.callback = callback;
		counterForPlaylists = 0;
		boolean isDonwloadingFromServer = false;
		for (final String id : playlistIDs) {
			if(playlistImageLocallyExists(id, size)){
				getImageLocally(id, size, true);
			}
			else{
				isDonwloadingFromServer = true;
				Runnable r = new Runnable() {
					
					@Override
					public void run() {
						counterForPlaylists++;
						retrieveImageFromServer(size, id, true);
					}
				};
				Thread t = new Thread(r);
				t.start();
			}
		}
		if(!isDonwloadingFromServer && counterForPlaylists < 1){
			HashMap<String, Bitmap> images = (size.equals(ImageSize.SIZE_60)) ? playlistCovers60 : playlistCovers110;
			callback.imagesReceived(images, true);
		}
	}
	
	private synchronized void saveImageLocally(ImageSize size, String id, boolean isPlaylistImage, byte[] bmp){
		Log.d(TAG, "saving file");
		File img = null;
		if(size.equals(ImageSize.SIZE_60)){
			if(isPlaylistImage){
				img = new File(DIR_PATH_PLAYLIST + DIR_PATH_60+id);
			}
			else{
				img = new File(DIR_PATH_ALBUM + DIR_PATH_60+id);
			}
			
		}
		else if (size.equals(ImageSize.SIZE_110)){
			if(isPlaylistImage){
				img = new File(DIR_PATH_PLAYLIST + DIR_PATH_110+id);
			}
			else{
				img = new File(DIR_PATH_ALBUM + DIR_PATH_110+id);
			}
		}
		FileOutputStream fos;
		try {
			Log.d(TAG, "saving fileOutputStream");
			fos = new FileOutputStream(img);
			fos.write(bmp);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public enum ImageSize{
		SIZE_60,
		SIZE_110
	}
}
