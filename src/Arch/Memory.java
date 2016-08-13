package Arch;

import Utils.ByteArray;

public class Memory extends ByteArray {

	/*** 
	 * Default no arg constructor
	 * */
	public Memory() {
		super();
	}
	
	/*** 
	 * Initialize a ByteArray from a file
	 * */
	public Memory(String fileName) {
		super(fileName);
	}
	
	public byte fetchByteNoPosDelta(short addr) {
		int oldPos = this.position;
		this.position = addr;
		byte b = this.readByte();
		this.position = oldPos;
		this.updateInfo(0);
		return b;
	}
	
	public void storeByteNoPosDelta(short addr, byte data) {
		int oldPos = this.position;
		this.position = addr;
		this.writeByte(data);
		this.position = oldPos;
		updateInfo(0);
	}
	
	public void storeShortNoPosDelta(short addr, short data) {
		int oldPos = this.position;
		this.position = addr;
		this.writeShort(data);
		this.position = oldPos;
		updateInfo(0);
	}
	
	public short readShortNoPosDelta(short addr) {
		int oldPos = this.position;
		this.position = addr;
		short data = this.readShort();
		this.position = oldPos;
		updateInfo(0);
		return data;
	}
	
	public static void main(String[] args) {
		short x = (short) 0xfff0;
		System.out.println( x + 15 );
	}
}
