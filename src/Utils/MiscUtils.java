package Utils;

import Arch.Encoding;

public class MiscUtils {

	public static void printAdjustedInfo(String[] opLookup, short[][] info) {
		String adjusted = "";
		for(int i = 0, j = 0; i < opLookup.length; i++) {
			if(opLookup[i].equals("")) {
				adjusted += "null";
			}
			else {
				String pair = "";
				pair += "{";
				for(int l = 0; l < info[j].length; l++) {
					pair += info[j][l];
					if(l < info[j].length-1) pair += ",";
				}
				pair += "}";
				adjusted += pair;
				j++;
			}
			if(i < opLookup.length-1) adjusted += ",";
		}
		System.out.println(adjusted);
	}
	
	public static double nanos2seconds(long nanos) {
		return (double)nanos / 1000000000L;
	}
	
	public static void strArray2Constants(String[] arr) {
		for(int i = 0; i < arr.length; i++) {
			
			System.out.println("public static final int " + arr[i] + " = " + i + ";");
		}
	}
	
	public static int[] compatibilityMapCbOpsToOps() {
		int[] map = new int[Encoding.cbOps.length];
		int i = 0;
		int l = 0;
		int extra = 0;
		boolean foundMatch = false;
		for(String s : Encoding.cbOps) {
			l = 0;			
			foundMatch = false;
			
			for(String _s : Encoding.ops) {
				if(s.equals(_s)) {
					map[i] = l;
					foundMatch = true;
					break;
				} 
				l++;
			}
			
			if(!foundMatch) {
				//System.out.println("No match for: " + s);
			}
			
			i++;
		}
		
		//for(i = 0; i < map.length; i++)
			//System.out.println(Encoding.ops[map[i]]);
		
		return map;
	}
	
	public static void main(String[] args) {
		System.out.println(Encoding.ops.length);
		//strArray2Constants(Encoding.cbOps);
		compatibilityMapCbOpsToOps();
	}
}
