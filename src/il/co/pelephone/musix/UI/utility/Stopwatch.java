package il.co.pelephone.musix.UI.utility;

public class Stopwatch {
	private long elapsed=0;
	public void setElapsed(int elapsedSecs) {
		this.elapsed = elapsedSecs * 1000;////convert to millisec
	}

	private long start=0;
	private long stop=0;
	private boolean isRunning;
	
	public void initialize(){
		elapsed = 0;
		start = 0;
		stop = 0;
		isRunning = false;
	}
	
	public void start(){
		if(!isRunning){
			start = System.currentTimeMillis();
			isRunning = true;
		}
		
	}
	
	public void stop(){
		stop = System.currentTimeMillis();
		
		if( isRunning && (start > 0) ) 
			elapsed = elapsed + stop - start;
		
		isRunning = false;
		start = 0;
	}
	
	public int getElapsedTimeInSec(){
		if(!isRunning)
			return (int)(elapsed/1000);
		else{
			long curTime = System.currentTimeMillis();
			return (int)((elapsed + curTime - start)/1000);
		}
	}
}
