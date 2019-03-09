package il.co.pelephone.musix.comm;

public interface IMXSyncCallback {

	public enum MXSyncError {
		MXSyncErrorNetwork ,
		MXSyncErrorInvalidXML
	};
	
	//	Notifies the delegate that the sync process was completed successfully, and the number of new songs added to device in the process.
	void syncCompleted (int newSongCount);
	
	//	Notifies the delegate that the specified list of songs was deleted from the device
	void syncSongsCopyrightDeleted(String[] songNames);
	
	
	void syncFailed(MXSyncError error);
	
}
