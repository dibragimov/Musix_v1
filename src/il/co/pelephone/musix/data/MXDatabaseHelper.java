package il.co.pelephone.musix.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MXDatabaseHelper extends SQLiteOpenHelper {

	public static String DB_PATH = "/data/data/il.co.pelephone.musix.UI/databases/"; //  "/sdcard/Musix/databases/"; //"/sdcard/"; //"/sdcard/Musix/databases/"; ////right now works in root of SDCard.
	////need to change a code to create subdirectories.
	 
    public static String DB_NAME = "musix.db"; 
 
    private final Context myContext;
    
    private static int CURRENT_VERSION = 1;

	
	public MXDatabaseHelper(Context context) {
		super(context,  DB_NAME, null, CURRENT_VERSION);
		this.myContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
	
		Log.d("MXDatabaseHelper", "onCreate called");
	}
	
	

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
	
	/**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public synchronized  SQLiteDatabase createDataBase() {
 
    	boolean dbExist = checkDataBase();
    	Log.i("MXDatabaseHelper", "Does DB exist: "+dbExist);
 
    	if(!dbExist){
    		
    		try{
    			copyDataBase();
    			Log.i("MXDatabaseHelper", "DB copied");
    		}
    		catch(IOException ioEx){
    			return null;
    		}
    	}
    	try{
    		String myPath = DB_PATH + DB_NAME;
    		Log.i("MXDatabaseHelper", "DB path: "+myPath);
    	//	SQLiteDatabase db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    		Log.i("MXDatabaseHelper", "DB opened");
    		//return db;
    		return null;
    	}
    	catch(SQLiteException sEx){
			return null;
		}
    }
	
	/**
     * Copies the database from the local assets-folder to sdcard (DB_PATH)
     * from where it can be accessed and handled.
     * This is done by transferring bytestream.
     * */
    public void copyDataBase() throws IOException{
 
    	Log.i("MXDatabaseHelper", "start copying DB");
    	//Open your local db as the input stream
    	InputStream myInput = myContext.getAssets().open(DB_NAME);
 
    	// Path to the empty db
    	String outFileName = DB_PATH + DB_NAME; ///// - /sdcard/Musix/databases/musix.db
    	File fl = new File(DB_PATH);
    	if(!fl.exists()){
    		fl.mkdirs();
    		Log.i("MXDatabaseHelper", "Path created");
    	}
 
    	//Open the empty db as the output stream
    	OutputStream myOutput = new FileOutputStream(outFileName);
 
    	//transfer bytes from the input file to the output file
    	byte[] buffer = new byte[1024];
    	int length;
    	while ((length = myInput.read(buffer))>0){
    		myOutput.write(buffer, 0, length);
    	}
 
    	//Close the streams
    	myOutput.flush();
    	myOutput.close();
    	myInput.close();
    	Log.i("MXDatabaseHelper", "DB copied");
 
    }
    
    public boolean isDataBaseExist() {
        File dbFile = new File(DB_PATH+DB_NAME);
        return dbFile.exists();
}
    
    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    public boolean checkDataBase(){
    	SQLiteDatabase checkDB = null;
 
    	try{
    		String myPath = DB_PATH + DB_NAME;
    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
 
    	}catch(SQLiteException e){
    		//database does't exist yet.
    	}
 
    	if(checkDB != null){
    		checkDB.close();
    	}
 
    	Log.i("MXDatabaseHelper", "DB check: "+(checkDB != null));
    	return checkDB != null ? true : false;
    }


}
