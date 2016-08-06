package Types;

public class uint {
	private long l;
	
	public uint(int val) {
		l = new Long(val & 0xffffffffL);
	}
	
	public uint(long val) {
		l = new Long(val & 0xffffffffL);
	}
	
	public long getVal() {return l;}
	
	public void setVal(long val) {
		this.l = val & 0xffffffffl;
	}
	
	public int getInt() {return (int)l;}
	
	public String toString(int radix) {
		String str = "";
		switch(radix) {
		case 16: str = Long.toHexString(l);
		break;
		default: break;
		}
		return str;
	}
}
