package il.co.pelephone.musix.comm;

import java.util.HashMap;

import android.graphics.Bitmap;

public interface IMXImageCallback {
	void imagesReceived(HashMap<String, Bitmap> images, boolean isPlaylistImages);
}
