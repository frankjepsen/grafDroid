package org.yavdr.grafdroid.tcp;


public class GraphTFTHeader {
	public static final int UNKNOWN = -1;
	public static final int WELCOME = 0;
	public static final int DATA = 1;
	public static final int MOUSEEVENT = 2;
	public static final int LOGOUT = 3;
	public static final int STARTCALIBRATION = 4;
	public static final int STOPCALIBRATION = 5;
	public static final int CHECK = 6;

	public int command;
	public int size;
	
	public GraphTFTHeader() {
		this(-1, 0);
	}
	
	public GraphTFTHeader(int command, int size) {
		this.command = command;
		this.size = size;
	}
}
