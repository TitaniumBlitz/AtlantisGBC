package Utils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Vector;

import Types.uint;

public class ByteArray {
	
	public Vector<Byte> buffer = new Vector<Byte>();
	public int position = 0;
	public int bytesAvailable = 0;
	public int length = 0;
	public ByteOrder endian = ByteOrder.LITTLE_ENDIAN;
	
	/*** 
	 * Default no arg constructor
	 * */
	public ByteArray() {}
	
	/*** 
	 * Initialize a ByteArray from a file
	 * */
	public ByteArray(String fileName) {
		loadFromFile(fileName);
	}
	
	private void updateInfo(int posDelta) {
		this.position += posDelta;
		this.length = buffer.size();
		this.bytesAvailable = this.length - this.position;		
	}
	
	public void rewind(int rewind) {
		updateInfo(-1 * rewind);
	}

	private static short getLower16Bits(int x)
	{ 
		return (short)((x & 0xffff) - ((x & 0x8000) << 1));
	}
	
	private static byte[] getBytesFromShort(short x) {
		return new byte[] {(byte)(x & 0xff), (byte)((x >> 8) & 0xff)};
	}
	
	private static byte[] getBytesFromInt(int x) {
		return new byte[] {(byte)(x & 0xff), (byte)((x >> 8) & 0xff), (byte)((x >> 16) & 0xff), (byte)((x >> 24) & 0xff)};
	}
	
	private int storeByte(int pos, Byte b) { // this doesnt change ACTUAL bytebuffer position... just temp one to keep track of where we are
		if(pos < buffer.size()) {
			buffer.set(pos, b);
		} else {
			buffer.add(b);
		}
		return ++pos;
	}
	
	public void writeShort(int val) {
		short x = getLower16Bits(val);
		byte[] bytes = getBytesFromShort(x);
		/*int p = this.position;
		for(Byte b : bytes) {
			if(p < buffer.size()) buffer.set(p, b);
			else buffer.add(b);
			p++;
		}*/
		int p = this.position;
		if(endian == ByteOrder.BIG_ENDIAN) {
			p = storeByte(p, bytes[1]);
			p = storeByte(p, bytes[0]);
		} else { // else little endian
			p = storeByte(p, bytes[0]);
			p = storeByte(p, bytes[1]);
		}
		updateInfo(2);
	}
	
	public void writeInt(int val) { // checked
		byte[] bytes = getBytesFromInt(val);
		/*int p = this.position;
		for(Byte b : bytes) {
			if(p < buffer.size()) buffer.set(p, b);
			else buffer.add(b);
			p++;
		}*/
		int p = this.position;
		if(endian == ByteOrder.BIG_ENDIAN) {
			p = storeByte(p, bytes[3]);
			p = storeByte(p, bytes[2]);
			p = storeByte(p, bytes[1]);
			p = storeByte(p, bytes[0]);
		} else { // else little endian
			p = storeByte(p, bytes[0]);
			p = storeByte(p, bytes[1]);
			p = storeByte(p, bytes[2]);
			p = storeByte(p, bytes[3]);
		}
		updateInfo(4);
	}
	
	public void writeUnsignedInt(uint u) {
		writeInt((int)(u.getVal() & 0xffffffffl));
	}
	
	/*public void writeUnsignedShort(int s) {
		
	}*/
	
	public void writeByte(int val) {
		byte b = (byte) (val & 0xff);
		if(this.position < buffer.size()) buffer.set(this.position, b);
		else buffer.add(b);
		updateInfo(1);
	}
	
	public int readInt() { //checked
		if(bytesAvailable < 4) return -1;
		//int ret = (int)( ((buffer.get(position+3)&0xFF)<<24) | (buffer.get(position+2)&0xFF)<<16 | (buffer.get(position+1)&0xFF)<<8 | (buffer.get(position)&0xFF) );
		byte[] bytes = new byte[4];
		int i = 0;
		for(int p = this.position; p < this.position+4; p++) {bytes[i]=buffer.get(p); i++;} 
		updateInfo(4);
		return ByteBuffer.wrap(bytes).order(endian).getInt();
	}
	
	private long readLong() {
		long ret = (long)( ((buffer.get(position+7)&0xFF)<<56) | (buffer.get(position+6)&0xFF)<<48 | (buffer.get(position+5)&0xFF)<<40 | (buffer.get(position+4)&0xFF)<<32 | (buffer.get(position+3)&0xFF)<<24 | (buffer.get(position+2)&0xFF)<<16 | (buffer.get(position+1)&0xFF)<<8 | (buffer.get(position)&0xFF) );
		updateInfo(8);
		return ret;
	}
	
	public float readFloat() {
		return Float.intBitsToFloat(readInt());
	}
	
	public double readDouble() {
		//return Double.longBitsToDouble(readLong());
		byte[] bytes = new byte[8];
		int i = 0;
		for(int p = this.position; p < this.position+8; p++) {bytes[i]=buffer.get(p); i++;} 
		updateInfo(8);
		return ByteBuffer.wrap(bytes).order(endian).getDouble();
	}
	
	public long readUnsignedInt() {
		return (long)readInt() & 0xFFFFFFFFL;
	}
	
	public byte readByte() {
		byte ret = buffer.get(position);
		updateInfo(1);
		return ret;
	}
	
	public int readUnsignedByte() {
		int ret = (int)buffer.get(position)&0xff;
		updateInfo(1);
		return ret;
	}
	
	public short readShort() {
		//int ret = (short)( ((buffer.get(position)&0xFF)<<8) | (buffer.get(position+1)&0xFF) );
		byte bytes[] = new byte[2];
		bytes[0] = buffer.get(this.position);
		bytes[1] = buffer.get(this.position+1);
		updateInfo(2);
		return ByteBuffer.wrap(bytes).order(endian).getShort();
	}
	
	public int readUnsignedShort() {
		//int ret = ((((int)buffer.get(position+1)&0xFF)<<8) | ((int)buffer.get(position)&0xFF));
		byte bytes[] = new byte[2];
		bytes[0] = buffer.get(this.position);
		bytes[1] = buffer.get(this.position+1);
		updateInfo(2);
		//return ret;
		return (int)ByteBuffer.wrap(bytes).order(endian).getShort()&0xffff;
	}
	
	public void writeUTFBytes(String s) { //checked
		try {
			byte[] bytes = s.getBytes("UTF-8");
			int p = this.position;
			/*if(endian == ByteOrder.BIG_ENDIAN) {
				for(Byte b : bytes)
					p = storeByte(p, b);
			} else { // little endian
				System.out.println("little");				
				for(int i = bytes.length-1; i >= 0; i--) 
					p = storeByte(p, bytes[i]);
			}*/
			for(Byte b : bytes)
				p = storeByte(p, b);
			updateInfo(s.length());
		} catch(UnsupportedEncodingException uee) {}
	}
	
	public void writeUTF(String s) {
		writeShort(s.length());
		writeUTFBytes(s);
	}
	
	public void writeBytes(ByteArray bytes, int offset, int length) {
		length = (length == 0) ? bytes.length - offset : length;
		bytes.position = offset;
		for(int p = offset; p < offset+length; p++) {
			this.writeByte(bytes.readByte());
		}
	}

	public void writeBytes(ByteArray bytes) {
		writeBytes(bytes, 0, 0);
	}
	
	public void readBytes(ByteArray store, int offset, int length) {
		length = (length == 0) ? this.length - offset : length;
		store.position = offset;
		for(int p = offset; p < offset+length; p++) {
			store.writeByte(readByte());
		}
	}
	
	public void readBytes(ByteArray store) {
		readBytes(store, 0, 0);
	}
	
	public byte[] readBytes(int length) {
		byte[] bytes = new byte[length];
		for(int p = 0; p < length; p++)
			bytes[p] = readByte();
		return bytes;
	}
	
	public String readUTFBytes(int length) { //checked
		String str = "";
		byte[] b = new byte[length];
		int _p = 0;
		for(int p = this.position; p < this.position+length; p++) {
			b[_p] = buffer.get(p);
			_p++;
		}
		updateInfo(length);
		try {
			//str = new String(b, "US-ASCII");
			str = new String(b, "UTF-8");
		} catch(UnsupportedEncodingException uee) {}
		return str;
	}
	
	public String readUTF() {
		int len = readUnsignedShort();
		return this.readUTFBytes(len);
	}
	
	public byte get(long offset) {
		return this.buffer.get((int)offset);
	}
	
	private byte[] toByteArray() {
	    final int n = buffer.size();
	    byte ret[] = new byte[n];
	    for (int i = 0; i < n; i++) {
	        ret[i] = buffer.get(i);
	    }
	    return ret;
	}
	
	//compression methods
	
	// not implemented...
	public void deflate() {}
	
	// not implemented...
	public void inflate() {}
	
	public void printAllBytes() {
		for(byte b : buffer) {
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("%02X ", readUnsignedByte()));
			System.out.println(sb.toString().toUpperCase());
		}
	}
	
	public void loadFromFile(String fileName) {
		try {
			RandomAccessFile f = new RandomAccessFile(fileName, "r");
			byte[] bytes = new byte[(int)f.length()];
			f.read(bytes);
			f.close();
			//System.out.println(bytes.length);
			for(byte b : bytes) writeByte(b);
		} catch(IOException ioe) {}
	}
	
	public void loadFromBuffer(byte[] buf) {
		this.buffer = new Vector<Byte>();
		for(byte b : buf)
			writeByte(b);
	}
	
	public void writeBufBytes(byte[] buf, int pos, int len) {
		for(int p = pos; p < pos+len; p++)
			writeByte(buf[p]);
	}
	
	public static void main(String[] args) {
	}
}
