package il.co.pelephone.musix.UI.utility;

public class ScreenInfo {
	private String name;
	private int number;
	private int previousScreenNumber;
	private int previousState;
	
	public ScreenInfo(String name, int number, int previousScreenNumber) {
		super();
		this.name = name;
		this.number = number;
		this.previousScreenNumber = previousScreenNumber;
		previousState = -1;
	}
	
	public ScreenInfo(String name, int number, int previousScreenNumber,
			int previousState) {
		super();
		this.name = name;
		this.number = number;
		this.previousScreenNumber = previousScreenNumber;
		this.previousState = previousState;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public int getPreviousScreenNumber() {
		return previousScreenNumber;
	}
	public void setPreviousScreenNumber(int previousScreenNumber) {
		this.previousScreenNumber = previousScreenNumber;
	}
	public int getPreviousState() {
		return previousState;
	}
	public void setPreviousState(int previousState) {
		this.previousState = previousState;
	}
}
