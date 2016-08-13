package Arch;

import java.util.HashMap;

public class Instruction {
	
	public static Instruction CBPREFIX = null; 	
	private String mnemonic;
	
	public static final int Flags_Z = 0;
	public static final int Flags_H = 1;
	public static final int Flags_N = 2;
	public static final int Flags_C = 3;
	public static final int Operand_1 = 4;
	public static final int Operand_2 = 5;
	public static final int Operand_Count = 6;
	public static final int Bytes = 7;
	public static final int Cycles = 8; 
	
	public static final int IMMD_8 = 1; // d8
	//public static final int (HL-) = 2;
	public static final int IMMD_16 = 5; // a16
	//public static final int Z = 6;
	public static final int SIGNED_8 = 7; // r8
	public static final int HL = 8;
	//public static final int 08H = 9;
	public static final int IMMD_16_ADDR = 10; // (a16)
	//public static final int (HL+) = 11;
	public static final int NZ = 12;
	public static final int IMMD_16_DATA = 13; // d16
	//public static final int (C) = 16;
	//public static final int NC = 19;
	//public static final int 38H = 21;
	//public static final int 20H = 25;
	//public static final int 28H = 26;
	//public static final int 18H = 28;
	//public static final int 30H = 29;
	public static final int IMMD_8_ADDR = 30; // (a8)
	//public static final int 10H = 32;
	//public static final int 00H = 33;
	//public static final int SP+r8 = 34;
	//public static final int 0 = 37;

	
	public Instruction(String mnemonic, HashMap<String, Instruction> map) {
		if(mnemonic != null && map != null) {
			this.mnemonic = mnemonic;
			
			if(!map.containsKey(mnemonic)) {
				map.put(mnemonic, this);
			}
		}
	}

	public void execute(short Z, short N, short H, short C, short op1, short op2, short op_count, short bytes, short cycles) {
		
	}
	
	public void execute(short... params) {
		
	}
	
	public void debugExecute(short... params) {
		
	}
	
	public void debugExecute() {
		
	}
	
	public void log(StringBuilder logger, short... params) {
	}
	
	public void printMnemonic() {
		System.out.println(this.mnemonic);
	}
	
	public String getMnemonic() { return this.mnemonic;	}
}
