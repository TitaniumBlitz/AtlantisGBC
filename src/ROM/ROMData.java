package ROM;

import Arch.Disassembler;
import Utils.ByteArray;

public class ROMData {
	
	private ByteArray data;

	// header information
	// .....................
	
	private Disassembler disasm; // disassembled instructions

	public ROMData(ByteArray data) {
		if(data != null) {
			this.data = data;
			parseHeader();
		}
	}
	
	private void parseHeader() {
		this.data.position = 0x0;
		
		// read header info here
		
		this.data.position = 0x0; // reset position of data pointer
	}
	
	public void printHeader() {
		// .... 
	}
	
	public void disasm() {
		disasm = new Disassembler(data);
		disasm.disasmAllInstructions();
	}
}
