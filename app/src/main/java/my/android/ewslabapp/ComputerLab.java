package my.android.ewslabapp;

/** Class representing a computer lab object **/

public class ComputerLab {

	private String labName;
	private int computersInUse;
	private int totalComputers;
	
	ComputerLab(String labName, int computersInUse, int totalComputers) {
		this.labName = labName;
		this.computersInUse = computersInUse;
		this.totalComputers = totalComputers;
	}
	
	String getLabName() {
		return this.labName;
	}
	
	void setLabName( String newName ) {
		this.labName = newName;
	}
	
	int getComputersInUse() {
		return this.computersInUse;
	}
	
	void setComputersInUse( int n ) {
		this.computersInUse = n;
	}
	
	int getTotalComputers() {
		return this.totalComputers;
	}
	
	void setTotalComputers( int n ) {
		this.totalComputers = n;
	}
}
