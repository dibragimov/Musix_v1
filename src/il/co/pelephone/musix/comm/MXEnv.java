package il.co.pelephone.musix.comm;


public  class MXEnv {

	public static final	byte ENV_DEV=1;
	public static final	byte ENV_TEST=2;
	public static final	byte ENV_PROD=3;
	
	public static final  byte curEnv=ENV_DEV;
	
	public static byte getEnv()
	{
		return curEnv;
	}
}
