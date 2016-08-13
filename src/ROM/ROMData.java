package ROM;

import Arch.Disassembler;
import Arch.Memory;

public class ROMData {
	
	private Memory mem;

	// header information
	// .....................
	
	private Disassembler disasm; // disassembled instructions

	public ROMData(Memory mem) {
		if(mem != null) {
			this.mem = mem;
			parseHeader();
		}
	}
	
	private void parseHeader() {
		this.mem.position = 0x0;
		
		// read header info here
		
		this.mem.position = 0x0; // reset position of mem pointer
	}
	
	public void printHeader() {
		// .... 
	}
	
	public void disasm() {
		disasm = new Disassembler(mem);
		disasm.disasmAllInstructions();
	}
}
