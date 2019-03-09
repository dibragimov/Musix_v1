package il.co.pelephone.musix.comm;


public interface IMXLoginCallback {
	public enum LoginSuccessType
	{
		
		LoginSuccessTypeSuccessful , //		Indicates that the login process was successful and the app should be fully functional
		LoginSuccessTypeOffline, //	Indicates that the app can only be used in offline mode.
								//Might be because roaming or no network access.
		LoginSuccessTypeAcceptTerms , //	Login succeeded but since it’s the first time the user is logged in show, the app terms.
		LoginSuccessTypeSuccessUpgrade, //	Login succeeded and the server notified that a non mandatory upgrade is available for
										//the app. Currently not supported by the server.
	}
	
	public enum  LoginFailureType {
		LoginFailureTypeUserBlocked , //		Indicates the the Musix app is blocked for the user
		LoginFailureTypeUserPrepaid, //		The user is a prepaid subscriber and can’t use the app
		LoginFailureTypeRegisterToWebOnly, //
		LoginFailureTypeRegisterToAppFirst, //	The user didn’t register for the client app, and needs to register first
		LoginFailureTypeErrorInXML, //
		LoginFailureTypeUnknown , //
		LoginFailureTypeNetworkError, //
		LoginFailureTypeDifferentEncryptionKey, //
		LoginFailureTypeMandatoryUpgrade, //	Login failed because a mandatory upgrade for the app is required.
	}
	
		
	/*
	 * 	Notifies the delegate that login was successful and provides 
	 * additional information about	the login.
	*/
	public void loginSuccess (LoginSuccessType type); 
	
	/*
	 * Notifies the delegate that login has failed specifying the failure reason.
	*/
	public void loginFailed (LoginFailureType reason);
	
	/*
	 * Notifies the delegate that the encryption key received is different than the one currently
	saved on the device, and asks the delegate if to proceed. If the delegate returns no, the
	login process should be stopped and a failed login should be sent to the delegate.
	*/	
	public boolean changeEncryptionKey(String curEncriptionKey);	
	
}
