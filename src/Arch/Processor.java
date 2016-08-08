package Arch;

import java.util.HashMap;

import Utils.ByteArray;
import Utils.StringUtils;

public class Processor {	
	
	// Instruction maps
	private static HashMap<String, Instruction> UNPREFIXED = new HashMap<String, Instruction>();
	private static HashMap<String, Instruction> CBPREFIXED = new HashMap<String, Instruction>();
	
	private static String[] opLookup_unprefixed = Encoding.opLookup_unprefixed;	
	private static String[] opLookup_cbprefixed = Encoding.opLookup_cbprefixed;
	
	private static Instruction[] unprefixedLookup = new Instruction[opLookup_unprefixed.length];
	private static Instruction[] cbprefixedLookup = new Instruction[opLookup_cbprefixed.length];
	
	private static short[][] unprefixed_info = Encoding.unprefixed_info;
	private static short[][] cbprefixed_info = Encoding.cbprefixed_info;
	
	private static String[] ops = Encoding.ops;
	private static String[] cbOps = Encoding.cbOps;
	
	private ByteArray data;
	
	protected Registers regs;

	//public static void main(String[] args) {
		//MiscUtils.printAdjustedInfo(opLookup_unprefixed, unprefixed_info);
		//System.out.println(cbprefixed_info.length);
		//Processor p = new Processor();
	//}
	
	public Processor(ByteArray data) {
		this.data = data;
		this.regs = new Registers(); // init a new register context
		//System.out.println("len: " + opLookup_unprefixed.length);
		mapInstructions();
	}
	
	public String fetchOpName(int index) {
		//int index = (byteValue >= (byte) 0) ? (int) byteValue : 256 + (int) byteValue; 
		return opLookup_unprefixed[index];
	}
	
	public Instruction fetchInstruction(int op) {
		if(op == 0xCB) {
			int _op = data.readUnsignedByte();
			data.rewind(1);
			return cbprefixedLookup[_op];
		}
		else {
			return unprefixedLookup[op];
		}
	}
	
	public short[] fetchInstructionParams(int op) {
		if(op == 0xCB) {
			int _op = data.readUnsignedByte();
			//data.rewind(1);
			return cbprefixed_info[_op];
		}
		return unprefixed_info[op];
	}
	
	public void mapInstructions() {
		System.out.println("Mapping Instructions...");
		
		// Map unprefixed instructions 		
		Instruction LD = new Instruction("LD",UNPREFIXED) {
			public void execute(short... params) {
				
			}
			
			public void log(StringBuilder log, short... params) {
				log.append(this.getMnemonic() + "\t");
				
				if( params[Instruction.Operand_2] == Instruction.IMMD_8 ) {
					byte immd8 = data.readByte();
					log.append( ops[params[Instruction.Operand_1]] + ", " + StringUtils.toHexStr(immd8));
				}
				else if( params[Instruction.Operand_1] == Instruction.IMMD_16_ADDR ) {
					short addr16 = data.readShort();
					log.append( "(" + StringUtils.toHexStr(addr16) + ")" + ", " + ops[params[Instruction.Operand_2]]);
				}
				else if( params[Instruction.Operand_2] == Instruction.IMMD_16_ADDR ) {
					short addr16 = data.readShort();
					log.append( ops[params[Instruction.Operand_1]] + ", " + "(" + StringUtils.toHexStr(addr16) + ")");
				}
				else if( params[Instruction.Operand_2] == Instruction.IMMD_16_DATA ) {
					short addr16 = data.readShort();
					log.append( ops[params[Instruction.Operand_1]] + ", " + StringUtils.toHexStr(addr16) );
				}
			}
		};
		
		Instruction DEC = new Instruction("DEC",UNPREFIXED) {
			public void execute(short... params) {
				
			}
			
			public void log(StringBuilder log, short... params) {
				log.append(this.getMnemonic() + "\t" + ops[ params[Instruction.Operand_1] ] );
			}
		};
		
		Instruction AND = new Instruction("AND",UNPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append(getMnemonic() + "\t");
				int op1_index = params[Instruction.Operand_1];
				if(op1_index == Instruction.IMMD_8) {
					byte immd8 = data.readByte();
					log.append(StringUtils.toHexStr(immd8));
				} else  {
					log.append( ops[ op1_index ] );
				}
			}
		};
		
		Instruction CCF = new Instruction("CCF",UNPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append(this.getMnemonic());
			}
		};
		
		Instruction INC = new Instruction("INC",UNPREFIXED) {
			public void execute(short... params) {
				int op1 = params[ Instruction.Operand_1 ];
				
				// dealing with an 8bit reg here...
				if( !Registers.isReg16(op1) ) {
					
				}
				// otherwise, we're dealing with a 16bit reg
				else {
					
				}
			}
			
			public void log(StringBuilder log, short... params) {
				log.append(this.getMnemonic() + "\t" + ops[ params[Instruction.Operand_1] ] );
			}
		};
		
		Instruction EI = new Instruction("EI",UNPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append(this.getMnemonic());
			}
		};
		
		Instruction CALL = new Instruction("CALL",UNPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append(getMnemonic() + "\t");
				int op2_index = params[ Instruction.Operand_2 ];
				String op1 = ops[ params[Instruction.Operand_1] ];
				short immd16 = data.readShort();
				if(op2_index == Instruction.IMMD_16) {
					log.append(op1 + ", " + StringUtils.toHexStr(immd16));
				}
				else {
					log.append(StringUtils.toHexStr(immd16));
				}
			}
		};
		
		Instruction JR = new Instruction("JR",UNPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append( this.getMnemonic() + "\t");
				int op1_index = params[Instruction.Operand_1];
				int op2_index = params[Instruction.Operand_2];
				String op1 = ops[ params[Instruction.Operand_1] ];
				short sB = (short) ((short)data.readByte() + data.position); // get signed byte data
				if(op1_index == Instruction.SIGNED_8) {
					log.append(StringUtils.toHexStr(sB));
				}
				else if(op2_index == Instruction.SIGNED_8) {
					log.append(op1 + ", " + StringUtils.toHexStr(sB));
				}
			}
		};
		
		// fixdisone
		Instruction ADD = new Instruction("ADD",UNPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append(getMnemonic() + "\t");
				int op2_index = params[ Instruction.Operand_1 ];
				String op1 = ops[ params[Instruction.Operand_1] ];
				byte data8 = data.readByte();
				if(op2_index == Instruction.IMMD_8) {
					log.append(op1 + ", " + StringUtils.toHexStr(data8));
				}
				else if(op2_index == Instruction.SIGNED_8) {
					log.append(op1 + ", -" + StringUtils.toHexStr((byte)-data8));
				}
				else {
					log.append(op1 + ", " + ops[ params[Instruction.Operand_2] ]);
				}
			}
		};
		
		Instruction RST = new Instruction("RST",UNPREFIXED) {
			public void execute(short... params) {

			}
			public void log(StringBuilder log, short... params) {
				log.append(this.getMnemonic() + "\t" + ops[ params[Instruction.Operand_1] ]);
			}
		};
		Instruction DAA = new Instruction("DAA",UNPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append(this.getMnemonic());
			}
		};
		Instruction CP = new Instruction("CP",UNPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append(getMnemonic() + "\t");
				int op1_index = params[Instruction.Operand_1];
				if(op1_index == Instruction.IMMD_8) {
					byte immd8 = data.readByte();
					log.append(StringUtils.toHexStr(immd8));
				} else {
					log.append( ops[ op1_index ] );
				}
			}
		};
		Instruction SCF = new Instruction("SCF",UNPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append(this.getMnemonic());
			}
		};
		Instruction RET = new Instruction("RET",UNPREFIXED) {
			public void execute(short... params) {
				
			}
			
			public void log(StringBuilder log, short... params) {
				log.append(this.getMnemonic() + "\t");
				int op1_index = params[Instruction.Operand_1];
				if(op1_index != -1) {
					log.append(ops[ params[Instruction.Operand_1] ]);
				}
			}
		};
		Instruction POP = new Instruction("POP",UNPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				String op1 = ops[ params[Instruction.Operand_1] ];
				log.append(getMnemonic() + "\t" + op1);				
			}
		};
		Instruction ADC = new Instruction("ADC",UNPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append(getMnemonic() + "\t");
				String op1 = ops[ params[Instruction.Operand_1] ];
				int op2_index = params[Instruction.Operand_2];
				if(op2_index == Instruction.IMMD_8) {
					byte immd8 = data.readByte();
					log.append(op1 + ", " + StringUtils.toHexStr(immd8));
				}
				else {
					log.append(op1 + ", " + ops[op2_index]);
				}
			}
		};
		Instruction CPL = new Instruction("CPL",UNPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append(getMnemonic());
			}
		};
		Instruction NOP = new Instruction("NOP",UNPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append("nop");
			}
		};
		Instruction RLCA = new Instruction("RLCA",UNPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append(getMnemonic());
			}
		};
		Instruction XOR = new Instruction("XOR",UNPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append(this.getMnemonic() + "\t");				
				int op1 = params[ Instruction.Operand_1 ];
				byte immd8 = data.readByte();
				
				if(op1 == Instruction.IMMD_8) {
					log.append(StringUtils.toHexStr(immd8));
				}
				else {
					log.append( ops[ params[Instruction.Operand_1] ] ); //*
				}
			}
		};
		Instruction RRCA = new Instruction("RRCA",UNPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append(getMnemonic());
			}
		};
		Instruction PUSH = new Instruction("PUSH",UNPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append(this.getMnemonic() + "\t" + ops[ params[Instruction.Operand_1] ]);
			}
		};
		Instruction OR = new Instruction("OR",UNPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append(this.getMnemonic() + "\t");				
				int op1 = params[ Instruction.Operand_1 ];
				byte immd8 = data.readByte();
				
				if(op1 == Instruction.IMMD_8) {
					log.append(StringUtils.toHexStr(immd8));
				}
				else {
					log.append( ops[ params[Instruction.Operand_1] ] ); //*
				}
			}
		};
		Instruction LDH = new Instruction("LDH",UNPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append("LD\t");
				int op1_index = params[ Instruction.Operand_1 ];
				int op2_index = params[ Instruction.Operand_2 ];
				String op2 = ops[ params[Instruction.Operand_2] ];
				byte unsigned8 = data.readByte();
				if( op1_index == Instruction.IMMD_8_ADDR ) {
					log.append( "(0xFF00+" + StringUtils.toHexStr(unsigned8) + ")" + ", " + op2);
				}
				else if(op2_index == Instruction.IMMD_8_ADDR) {
					log.append( ops[ params[Instruction.Operand_1] ] + ", " + "(0xFF00+" + StringUtils.toHexStr(unsigned8) + ")");
				}
			}
		};
		Instruction JP = new Instruction("JP",UNPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append(this.getMnemonic() + "\t");
				String op1 = ops[ params[Instruction.Operand_1] ];
				if( params[Instruction.Operand_1] == Instruction.IMMD_16 ) {
					short immd16 = data.readShort();
					log.append(StringUtils.toHexStr(immd16));
				}
				else if( params[Instruction.Operand_2] == Instruction.IMMD_16 ) {
					short immd16 = data.readShort();
					log.append(op1 + ", " + StringUtils.toHexStr(immd16));
				}
				else {
					log.append(op1);
				}
			}
		};
		
		// should never be executed
		Instruction PREFIX = new Instruction("PREFIX",UNPREFIXED) {
			public void execute(short... params) {

			}
		};
		
		Instruction HALT = new Instruction("HALT",UNPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append(getMnemonic());
			}
		};
		
		Instruction DI = new Instruction("DI",UNPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append(getMnemonic());
			}
		};
		
		Instruction SBC = new Instruction("SBC",UNPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append(getMnemonic() + "\t");
				String op1 = ops[ params[Instruction.Operand_1] ];
				int op2_index = params[Instruction.Operand_2];
				if(op2_index == Instruction.IMMD_8) {
					byte immd8 = data.readByte();
					log.append(op1 + ", " + StringUtils.toHexStr(immd8));
				}
				else {
					log.append(op1 + ", " + ops[op2_index]);
				}
			}
		};
		
		Instruction RETI = new Instruction("RETI",UNPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append(this.getMnemonic());
			}
		};
		
		Instruction RRA = new Instruction("RRA",UNPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append(getMnemonic());
			}
		};
		
		Instruction SUB = new Instruction("SUB",UNPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append(getMnemonic() + "\t");
				int op1_index = params[Instruction.Operand_1];
				if(op1_index == Instruction.IMMD_8) {
					byte immd8 = data.readByte();
					log.append(StringUtils.toHexStr(immd8));
				} else  {
					log.append( ops[ op1_index ] );
				}
			}
		};
		
		Instruction RLA = new Instruction("RLA",UNPREFIXED) {
			public void execute(short... params) {

			}
			public void log(StringBuilder log, short... params) {
				log.append(getMnemonic());
			}
		};
		
		Instruction STOP = new Instruction("STOP",UNPREFIXED) {
			public void execute(short... params) {

			}
			public void log(StringBuilder log, short... params) {
				log.append(getMnemonic());
			}
		};
		
		/* ******************************************************************************************** */
		
		// Map cb-prefixed instructions		
		Instruction SRL = new Instruction("SRL",CBPREFIXED) {
			public void execute(short... params) {
				
			}
			
			public void log(StringBuilder log, short... params) {
				log.append(getMnemonic() + "\t" + cbOps[ params[Instruction.Operand_1] ]); 
			}
		};
		Instruction SET = new Instruction("SET",CBPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append(getMnemonic() + "\t" + cbOps[ params[Instruction.Operand_1] ] + ", " + cbOps[ params[Instruction.Operand_2] ]); 
			}
		};
		Instruction SRA = new Instruction("SRA",CBPREFIXED) {
			public void execute(short... params) {
				
			}
			
			public void log(StringBuilder log, short... params) {
				log.append(getMnemonic() + "\t" + cbOps[ params[Instruction.Operand_1] ] + ", " + cbOps[ params[Instruction.Operand_2] ]); 
			}
		};
		
		Instruction SLA = new Instruction("SLA",CBPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append(getMnemonic() + "\t" + cbOps[ params[Instruction.Operand_1] ]); 
			}
		};
		
		Instruction RES = new Instruction("RES",CBPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append(getMnemonic() + "\t" + cbOps[ params[Instruction.Operand_1] ] + ", " + cbOps[ params[Instruction.Operand_2] ]); 
			}
		};
		Instruction BIT = new Instruction("BIT",CBPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append(getMnemonic() + "\t" + cbOps[ params[Instruction.Operand_1] ] + ", " + cbOps[ params[Instruction.Operand_2] ]); 
			}
		};
		Instruction SWAP = new Instruction("SWAP",CBPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append(getMnemonic() + "\t" + cbOps[ params[Instruction.Operand_1] ]); 
			}
		};
		Instruction RRC = new Instruction("RRC",CBPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append(getMnemonic() + "\t" + cbOps[ params[Instruction.Operand_1] ]); 
			}
		};
		Instruction RLC = new Instruction("RLC",CBPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append(getMnemonic() + "\t" + cbOps[ params[Instruction.Operand_1] ]); 
			}
		};
		Instruction RR = new Instruction("RR",CBPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append(getMnemonic() + "\t" + cbOps[ params[Instruction.Operand_1] ]); 
			}
		};
		Instruction RL = new Instruction("RL",CBPREFIXED) {
			public void execute(short... params) {

			}
			
			public void log(StringBuilder log, short... params) {
				log.append(getMnemonic() + "\t" + cbOps[ params[Instruction.Operand_1] ]); 
			}
		};
		
		Instruction.CBPREFIX = PREFIX;
		
		/*
		 * Now perform a reverse lookup on the hash maps and compile a lookup table for the UN PREFIXED instructions
		 * 
		 * */
		int i = 0;
		String name = "";
		for(i = 0; i < opLookup_unprefixed.length; i++) {
			if( (name = opLookup_unprefixed[i]).trim().equals("") )
				continue;
			
			unprefixedLookup[i] = UNPREFIXED.get(name);
		}
		
		/*
		 * Now perform a reverse lookup on the hash maps and compile a lookup table for the CB PREFIXED instructions
		 * 
		 * */
		for(i = 0; i < opLookup_cbprefixed.length; i++) {
			if( (name = opLookup_cbprefixed[i]).trim().equals("") )
				continue;
			
			cbprefixedLookup[i] = CBPREFIXED.get(name);
		}
		
		System.out.println("Finished mapping instructions.");
	}
}
