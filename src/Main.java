import Arch.Memory;
import ROM.ROMData;

// by Taylor

public class Main {

	public static void main(String[] args) {
		Memory romFile = new Memory("./ROMs/stopwatch-V1.gb");
		ROMData rom = new ROMData(romFile);
		//rom.printHeader();
		rom.disasm();
	}
}
