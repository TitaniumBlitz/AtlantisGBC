import ROM.ROMData;
import Utils.ByteArray;

public class Main {

	public static void main(String[] args) {
		ByteArray romFile = new ByteArray("./ROMs/stopwatch-V1.gb");
		ROMData rom = new ROMData(romFile);
		//rom.printHeader();
		rom.disasm();
	}
}
