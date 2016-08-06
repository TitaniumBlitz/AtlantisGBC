package Utils;

public final class StringUtils {

	public static String toHexStr(int hex) {
		return "0x" + String.format("%08X", hex);
	}
	
	public static String toHexStr(short hex) {
		return "0x" + String.format("%04X", hex);
	}
	
	public static String toHexStr(byte hex) {
		return "0x" + String.format("%02X", hex);
	}
}
