package Arch;

import Utils.MiscUtils;

public class Registers {
	
	protected short PC = 1;
	
	public static final int A = 0;
	public static final int ACCUMULATOR = A;
	public static final int B = 20;
	public static final int C = 4;
	public static final int D = 15;
	public static final int E = 22;
	public static final int H = 14;
	public static final int L = 17;
	public static final int S = 39;
	public static final int P = 40;
	
	// 8bit reg pairs
	public static final int AF = 27;
	public static final int BC = 23;
	public static final int DE = 35;
	public static final int HL = 8;
	public static final int SP = 3;
	
	public static final int BC_ptr = 24;
	public static final int HL_ptr = 18;
	public static final int DE_ptr = 36;
	
	protected boolean  Flags_00 = false;
	protected boolean  Flags_01 = false;
	protected boolean  Flags_02 = false;
	protected boolean  Flags_03 = false;
	protected boolean  Flags_Z_ZERO = false;
	protected boolean  Flags_N_SUBTRACT = false;
	protected boolean  Flags_H_HALFCARRY = false;
	protected boolean  Flags_C_CARRY = false;

	protected byte[] tbl = new byte[41];
	protected int[][] regpair_tbl = new int[41][2];
	protected int[] ptr2regs = new int[41];
	
	protected int[] cbReverseLookup;
	
	public Registers() {
		this.cbReverseLookup = MiscUtils.compatibilityMapCbOpsToOps();
		
		tbl[ A ] = 0; // default val of accumulator is 0 most likely...?
		
		regpair_tbl[ BC ] = new int[]{B, C};
		regpair_tbl[ DE ] = new int[]{D, E};
		regpair_tbl[ HL ] = new int[]{H, L};
		regpair_tbl[ SP ] = new int[]{S, P};
		
		ptr2regs[ BC_ptr ] = BC;
		ptr2regs[ HL_ptr ] = HL;
		ptr2regs[ DE_ptr ] = DE;
	}
	
	// o - operand
	public static boolean isReg16(int op) {
		return op == BC ||
				op == DE ||
				op == HL ||
				op == SP;
	}
	
	public static boolean is16bitPointer(int op) {
		return op == BC_ptr 
				|| op == HL_ptr 
				|| op == DE_ptr; 
	}	
	
	public int grabRegPtr(int r, boolean isCB) {		
		if(isCB) {
			return cbReverseLookup[r];
		}
		return r;
	}
	
	/*** 
	 * Returns the result of two 8bit regs "concatenated" together.
	 *  code:
	 *  ((tbl[ reg16[0] ] << 8)) & 0xFF00 OR'd with (tbl[ reg16[1] ] & 0xff);
	 * 
	 * */
	public short getConcatRegs(int[] reg16) {
		short val1 = (short) ((tbl[ reg16[0] ] << 8));
		short val2 = (short) (tbl[ reg16[1] ] & 0xff);
		return (short) ((val1 & 0xFF00) | val2);
	}
	
	/*** 
	 * Prepares the stack for a PUSH -- decrements stack pointer by 2
	 * */
	public short decSP(int amt) {
		short sp = getConcatRegs( regpair_tbl[ SP ] );
		sp -= amt;
		tbl[ S ] = (byte) ((sp >> 8) & 0xff);
		tbl[ P ] = (byte) (sp & 0xff);
		return sp;
	}
	
	/*** 
	 * Completes a POP of the stack -- increments stack pointer by 2
	 * */
	public short incSP(int amt) {
		short sp = getConcatRegs( regpair_tbl[ SP ] );
		sp += amt;
		tbl[ S ] = (byte) ((sp >> 8) & 0xff);
		tbl[ P ] = (byte) (sp & 0xff);
		return sp;
	}
	
	/*** 
	 * Returns the address that a reg pair represents
	 * 
	 * */
	public short getAddress(int regs_ptr) {
		int[] regs = regpair_tbl[ ptr2regs[ regs_ptr ] ];
		short addr = getConcatRegs( regs );
		return addr;
	}
	
	public static void main(String[] args) {
		Registers regs = new Registers();
		System.out.println( regs.grabRegPtr(0, true) );
	}
}
