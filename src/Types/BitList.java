package Types;

public class BitList {
	
	protected int[] bits = null;
	protected int val;
	
	protected int[][] cachedFromTo = new int[32][32];
 
	public BitList(int val) {
		this.val = val;
		this.bits = getAllBits(32);
		
		for(int f = 0; f < cachedFromTo.length; f++)
			for(int t = 0; t < cachedFromTo[0].length; t++)
				cachedFromTo[f][t] = -1;
	}
	
	private int[] getAllBits(int len) {
		int[] bits = new int[len];
		
		for(int p = 0, b = len-1; p < len; p++, b--) {
			bits[p] = (this.val >> b) & 0x1;
		}
		
		return bits;
	}
	
	public boolean isSet(int pos) {
		if(pos >= 0 && pos <= bits.length) {
			return bits[31 - pos] == 1;
		}
		return false;
	}
	
	public boolean getBit(int pos) {
		if(pos >= 0 && pos <= bits.length) {
			return bits[31 - pos] == 1;
		}
		return false;
	}
	
	/*** 
	 * Select bit range from FROM to TO (includes to)
	 * */
	public int spliceVal(int from, int to) {
		int val = 0;
		
		if( cachedFromTo[from][to] != -1 )
			return cachedFromTo[from][to];
			
		for(int pos = from; pos >= to; pos--) {
			val |= bits[31 - pos];
			if(pos > to) val <<= 1;
		}
		return (cachedFromTo[from][to] = val);		
	}
	
	public int spliceComplementedVal(int from, int to) {
		return (~(~spliceVal(from, to))) + 1;
	}
	
	public static int concat(int left, int padd, int right) {
		return (left << padd) | right;
	}
	
	public void printBits() {
		for(int b : bits)
			System.out.print(b);
	}
	
	public static void main(String[] args) {
		BitList b = new BitList(0x1D);
		//b.printBits();
		//System.out.println(b.spliceVal(1, 31));
		//System.out.println(BitList.concat(9, 8));
		//int l = b.spliceVal(0, 1);
		//System.out.println(l);
		//int c = BitList.concat(b.spliceVal(0, 1), 1, b.spliceVal(4, 4));
		//System.out.println(c);
		//b.printBits();
		int l = b.spliceVal(4, 3);
		int r = b.spliceVal(0, 0);
		System.out.println();
		System.out.println(BitList.concat(l, 1, r));
		//int val = 0x18;
		//System.out.println((val >> 3) & 1);
	}
}
