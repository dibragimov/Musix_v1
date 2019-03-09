package il.co.pelephone.musix.UI;

public interface IMXUIMusicCatalogCallback {
	public enum MusicCatalogResponseType{
		ResponseTypeGeneralPromotion, //// indicates general promotion (form playlists, Albums, SongsResponseType
		ResponseTypeAlbumPromotion, //// indicates promotion for Albums
		ResponseTypeMediaPromotion, //// indicates promotion for Songs
		ResponseTypePlaylistPromotion, //// indicates promotion for Playlists
		ResponseTypeSearchedAlbums, //// indicates featured Albums for specific genre
		ResponseTypeSearchedArtists, //// indicates featured Playlists for specific genre
		ResponseTypeSearchedMedias, //// indicates featured Songs for specific genre
		ResponseTypeAllGenres, //// indicates all genres
	}
	
	void responseReceived(MusicCatalogResponseType type);
	
	void requestFailedNetworkError();
	void requestFailedInternalError();
}
