package Arch;

import ROM.ROMData;
import Types.BitList;
import Utils.ByteArray;
import Utils.MiscUtils;
import Utils.StringUtils;

public class Disassembler {
	
	protected Memory mem;
	
	private StringBuilder log = new StringBuilder();

	public Disassembler(Memory rData) {
		this.mem = rData;
	}
	
	private static boolean equalsEither(int v, int... vals) {
		for(int val : vals)
			if(val == v) return true;
		return false;
	}
	
	public void disasmAllInstructions() {	
		Processor p = new Processor(mem);
		Instruction ins = null;		
		short[] insParams = null;
		int op = (byte)0xff;
		long elapsed = 0L, start = System.nanoTime();
		do {			
			op = mem.readUnsignedByte();			
			ins = p.fetchInstruction(op);
			
			if(ins == null) {
				continue;
			}
			
			insParams = p.fetchInstructionParams(op);
			
			ins.log(log, insParams);
			log.append("\n");
			
			p.regs.PC ++;
			
			//System.out.println(p.fetchOpName(op));
			
			//System.out.println("pos: " + StringUtils.toHexStr((short)data.position));
			
		} while(mem.bytesAvailable > 1);
		
		elapsed = System.nanoTime() - start;
		
		System.out.println("Disasm took approx: " + MiscUtils.nanos2seconds(elapsed) + " seconds. ");
		
		System.out.println(log.toString());
	}	
	
	//public static void main(String[] args) {
		//int x = 9;
		//int spliced = spliceBits(x, 3, 3);
		//System.out.println(spliced);
	//}
}
