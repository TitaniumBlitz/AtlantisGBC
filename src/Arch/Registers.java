package Arch;

import Utils.MiscUtils;

public class Registers {
	
	protected short PC = 1;
	
	public static final int BC = 23;
	public static final int DE = 35;
	public static final int HL = 8;
	public static final int SP = 3;

	protected byte[] r = new byte[38];
	protected int[] cbReverseLookup;
	
	public Registers() {
		this.cbReverseLookup = MiscUtils.compatibilityMapCbOpsToOps();
	}
	
	public static boolean isReg16(int r) {
		return r == BC ||
				r == DE ||
				r == HL ||
				r == SP;
	}
	
	public int grabRegPtr(int r, boolean isCB) {		
		if(isCB) {
			return cbReverseLookup[r];
		}
		return r;
	}
	
	public static void main(String[] args) {
		Registers regs = new Registers();
		System.out.println( regs.grabRegPtr(0, true) );
	}
}
