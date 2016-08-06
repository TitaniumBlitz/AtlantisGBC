package Utils;

public class BitUtils {

	private static byte spliceByte(byte val, int from, int to) {
		return (byte)((val >> to) & ~(-1 << (from-to)+1));
	}
	
	/*public static void main(String[] args) {
		byte _b = 0x63;
		byte b = spliceByte(_b, 6, 0);
		System.out.println(b);
	}*/
}
