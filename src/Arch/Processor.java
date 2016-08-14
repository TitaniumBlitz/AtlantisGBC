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
	
	private Memory mem;
	
	protected Registers regs;

	/*public static void main(String[] args) {
		//MiscUtils.printAdjustedInfo(opLookup_unprefixed, unprefixed_info);
		//System.out.println(cbprefixed_info.length);
		Processor p = new Processor(null);
		
		short s = (short) 0xB3FE;
		System.out.println(s+1);
		
		// init with dummy vals to test
		p.regs.tbl[Registers.B] = (byte) 0xB3;
		p.regs.tbl[Registers.C] = (byte) 0xFE;
		
		int inc_index = 3;
		Instruction inc = unprefixedLookup[inc_index];
		inc.debugExecute();
		System.out.println( "b: " + p.regs.tbl[Registers.B] );
		System.out.println( "c: " + p.regs.tbl[Registers.C] );
	}*/
	
	public Processor(Memory mem) {
		this.mem = mem;
		this.regs = new Registers(); // init a new register context
		//System.out.println("len: " + opLookup_unprefixed.length);
		
		/* Set default Stack Pointer address ( 0xFFFE) 
		 * Regs "S" and "P" dont actually exist... but its easier if we set it up this way
		 * for the emulator. 
		 */
		this.regs.tbl[ Registers.S ] = (byte)0xFF;
		this.regs.tbl[ Registers.P ] = (byte)0xFE;
		
		mapInstructions();
	}
	
	public String fetchOpName(int index) {
		//int index = (byteValue >= (byte) 0) ? (int) byteValue : 256 + (int) byteValue; 
		return opLookup_unprefixed[index];
	}
	
	public Instruction fetchInstruction(int op) {
		if(op == 0xCB) {
			int _op = mem.readUnsignedByte();
			mem.rewind(1);
			return cbprefixedLookup[_op];
		}
		else {
			return unprefixedLookup[op];
		}
	}
	
	public short[] fetchInstructionParams(int op) {
		if(op == 0xCB) {
			int _op = mem.readUnsignedByte();
			//mem.rewind(1);
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
					byte immd8 = mem.readByte();
					log.append( ops[params[Instruction.Operand_1]] + ", " + StringUtils.toHexStr(immd8));
				}
				else if( params[Instruction.Operand_1] == Instruction.IMMD_16_ADDR ) {
					short addr16 = mem.readShort();
					log.append( "(" + StringUtils.toHexStr(addr16) + ")" + ", " + ops[params[Instruction.Operand_2]]);
				}
				else if( params[Instruction.Operand_2] == Instruction.IMMD_16_ADDR ) {
					short addr16 = mem.readShort();
					log.append( ops[params[Instruction.Operand_1]] + ", " + "(" + StringUtils.toHexStr(addr16) + ")");
				}
				else if( params[Instruction.Operand_2] == Instruction.IMMD_16_DATA ) {
					short addr16 = mem.readShort();
					log.append( ops[params[Instruction.Operand_1]] + ", " + StringUtils.toHexStr(addr16) );
				}
			}
		};
		
		Instruction DEC = new Instruction("DEC",UNPREFIXED) {
			public void execute(short... params) {
				int op1 = params[ Instruction.Operand_1 ];
				
				// dealing with an 8bit reg here...
				if( !Registers.isReg16(op1) ) {					
					int regPtr = op1;
					
					// 8bit reg pair representing a 16bit addr in memory to grab a byte of data from
					if( Registers.is16bitPointer(regPtr) ) {
						short addr = regs.getAddress(regPtr);
						byte b = mem.fetchByteNoPosDelta(addr);
						b --;						
						mem.storeByteNoPosDelta(addr, b);
						
						//adjust flags
						regs.Flags_Z_ZERO = (b == 0);
						regs.Flags_H_HALFCARRY = (b & 0xF) == 0xF;
						regs.Flags_N_SUBTRACT = true;
					}
					
					else {
						regs.tbl[ regPtr ] --;
						
						//adjust flags
						regs.Flags_Z_ZERO = (regs.tbl[regPtr] == 0);
						regs.Flags_H_HALFCARRY = (regs.tbl[regPtr] & 0xF) == 0xF;
						regs.Flags_N_SUBTRACT = true;
					}
				}
				// otherwise, we're dealing with a "16bit register"
				else {
					int[] reg16 = regs.regpair_tbl[ op1 ];
					short newVal = regs.getConcatRegs( reg16 );
					newVal --;
					
					// stuff the 16 bit val back into the corresponding 8bit regs
					regs.tbl[ reg16[0] ] = (byte) ((newVal >> 8) & 0xff);
					regs.tbl[ reg16[1] ] = (byte) (newVal & 0xff);
				}
			}
			
			public void log(StringBuilder log, short... params) {
				log.append(this.getMnemonic() + "\t" + ops[ params[Instruction.Operand_1] ] );
			}
		};
		
		Instruction AND = new Instruction("AND",UNPREFIXED) {
			public void execute(short... params) {				
				int op1_index = params[Instruction.Operand_1];
				byte b;
				if(op1_index == Instruction.IMMD_8) {
					b = mem.readByte();
					regs.tbl[ Registers.ACCUMULATOR ] = (byte)((regs.tbl[ Registers.ACCUMULATOR ] & b)&0xFF);
				}
				else {					
					if( Registers.is16bitPointer(op1_index) ) {
						short addr = regs.getAddress(op1_index);
						b = mem.fetchByteNoPosDelta(addr);
						regs.tbl[ Registers.ACCUMULATOR ] = (byte)((regs.tbl[ Registers.ACCUMULATOR ] & b)&0xFF);
					}
					// We're just AND'ing with one of the 8 bit regs
					else {
						regs.tbl[ Registers.ACCUMULATOR ] = (byte)((regs.tbl[ Registers.ACCUMULATOR ] & regs.tbl[ op1_index ])&0xFF);
					}
				}
				regs.Flags_C_CARRY = regs.Flags_N_SUBTRACT = false;
				regs.Flags_Z_ZERO = (regs.tbl[ Registers.ACCUMULATOR ] == 0);
			}
			
			public void log(StringBuilder log, short... params) {
				log.append(getMnemonic() + "\t");
				int op1_index = params[Instruction.Operand_1];
				if(op1_index == Instruction.IMMD_8) {
					byte immd8 = mem.readByte();
					log.append(StringUtils.toHexStr(immd8));
				} else  {
					log.append( ops[ op1_index ] );
				}
			}
		};
		
		// invert carry flag (and half-carry flag)
		Instruction CCF = new Instruction("CCF",UNPREFIXED) {
			public void execute(short... params) {
				regs.Flags_C_CARRY = !regs.Flags_C_CARRY;
				regs.Flags_H_HALFCARRY = !regs.Flags_H_HALFCARRY;				
				regs.Flags_N_SUBTRACT = false;
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
					int regPtr = op1;
					
					// 8bit reg pair representing a 16bit addr in memory to grab a byte of data from
					if( Registers.is16bitPointer(regPtr) ) {
						short addr = regs.getAddress(regPtr);
						byte b = mem.fetchByteNoPosDelta(addr);
						b ++;						
						mem.storeByteNoPosDelta(addr, b);
						
						//adjust flags
						regs.Flags_Z_ZERO = (b == 0);
						regs.Flags_H_HALFCARRY = (b & 0xF) == 0;
						regs.Flags_N_SUBTRACT = false;
					}
					
					else {
						regs.tbl[ regPtr ] ++;
						
						//adjust flags
						regs.Flags_Z_ZERO = (regs.tbl[regPtr] == 0);
						regs.Flags_H_HALFCARRY = (regs.tbl[regPtr] & 0xF) == 0;
						regs.Flags_N_SUBTRACT = false;
					}
				}
				// otherwise, we're dealing with a "16bit register"
				else {
					int[] reg16 = regs.regpair_tbl[ op1 ];
					short newVal = regs.getConcatRegs( reg16 );
					newVal ++;
					
					// stuff the 16 bit val back the corresponding 8bit regs
					regs.tbl[ reg16[0] ] = (byte) ((newVal >> 8) & 0xff);
					regs.tbl[ reg16[1] ] = (byte) (newVal & 0xff);
				}
			}
			
			public void log(StringBuilder log, short... params) {
				log.append(this.getMnemonic() + "\t" + ops[ params[Instruction.Operand_1] ] );
			}
		};
		
		Instruction EI = new Instruction("EI",UNPREFIXED) {
			public void execute(short... params) {
				// enable interrupts
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
				short immd16 = mem.readShort();
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
				short sB = (short) ((short)mem.readByte() + mem.position); // get signed byte mem
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
				byte mem8 = mem.readByte();
				if(op2_index == Instruction.IMMD_8) {
					log.append(op1 + ", " + StringUtils.toHexStr(mem8));
				}
				else if(op2_index == Instruction.SIGNED_8) {
					log.append(op1 + ", -" + StringUtils.toHexStr((byte)-mem8));
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
					byte immd8 = mem.readByte();
					log.append(StringUtils.toHexStr(immd8));
				} else {
					log.append( ops[ op1_index ] );
				}
			}
		};
		
		// set carry flag
		Instruction SCF = new Instruction("SCF",UNPREFIXED) {
			public void execute(short... params) {
				regs.Flags_C_CARRY = true;
				regs.Flags_H_HALFCARRY = regs.Flags_N_SUBTRACT = false;
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
				int op1_index = params[ Instruction.Operand_1 ];
				short sp = regs.getConcatRegs( regs.regpair_tbl[ Registers.SP ] );
				short bytes = mem.readShortNoPosDelta( sp );
				if(op1_index == Registers.AF) {
					regs.tbl[ Registers.A ] = (byte) ((bytes >> 8)&0xff);
					
					//pop flags
					short F = (byte) (bytes & 0xff);
					regs.Flags_Z_ZERO = ((byte)F & 0x80) == 1;
					regs.Flags_N_SUBTRACT = ((byte)F & 0x40) == 1; 
					regs.Flags_H_HALFCARRY = ((byte)F & 0x20) == 1; 
					regs.Flags_C_CARRY = ((byte)F & 0x10) == 1; 
				}
				else {
					int[] pair = regs.regpair_tbl[ op1_index ];
					regs.tbl[ pair[0] ] = (byte) ((bytes >> 8)&0xff);
					regs.tbl[ pair[1] ] = (byte) (bytes & 0xff);
				}
				regs.incSP(2);
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
					byte immd8 = mem.readByte();
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
				// do nothing...
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
				int op1_index = params[Instruction.Operand_1];
				byte b;
				if(op1_index == Instruction.IMMD_8) {
					b = mem.readByte();
					regs.tbl[ Registers.ACCUMULATOR ] = (byte)((regs.tbl[ Registers.ACCUMULATOR ] ^ b)&0xFF);
				}
				else {					
					if( Registers.is16bitPointer(op1_index) ) {
						short addr = regs.getAddress(op1_index);
						b = mem.fetchByteNoPosDelta(addr);
						regs.tbl[ Registers.ACCUMULATOR ] = (byte)((regs.tbl[ Registers.ACCUMULATOR ] ^ b)&0xFF);
					}
					// We're just XOR'ing with one of the 8 bit regs
					else {
						regs.tbl[ Registers.ACCUMULATOR ] = (byte)((regs.tbl[ Registers.ACCUMULATOR ] ^ regs.tbl[ op1_index ])&0xFF);
					}
				}
				regs.Flags_C_CARRY = regs.Flags_N_SUBTRACT = false;
				regs.Flags_Z_ZERO = (regs.tbl[ Registers.ACCUMULATOR ] == 0);
			}
			
			public void log(StringBuilder log, short... params) {
				log.append(getMnemonic() + "\t");
				int op1_index = params[Instruction.Operand_1];
				if(op1_index == Instruction.IMMD_8) {
					byte immd8 = mem.readByte();
					log.append(StringUtils.toHexStr(immd8));
				} else  {
					log.append( ops[ op1_index ] );
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
				int op1_index = params[ Instruction.Operand_1 ];
				
				short stackPointer = regs.decSP(2); // current address the stack pointer should be pointing to
				short toPush = -1;
				
				if( op1_index == Registers.AF ) {
					short A = (short) ((regs.tbl[ Registers.A ] << 8));
					
					short F = (short) (((regs.Flags_Z_ZERO ? 1 : 0) << 7)
										| ((regs.Flags_N_SUBTRACT ? 1 : 0) << 6)
										| ((regs.Flags_H_HALFCARRY ? 1 : 0) << 5)
										| ((regs.Flags_C_CARRY ? 1 : 0) << 4));
							
					short AF = (short) ((A & 0xFF00) | F);
					
					toPush = AF;					
				}
				else {
					short notAF = regs.getConcatRegs( regs.regpair_tbl[ op1_index ] );
					toPush = notAF;
				}
				
				mem.storeShortNoPosDelta(stackPointer, toPush);
			}
			
			public void log(StringBuilder log, short... params) {
				log.append(this.getMnemonic() + "\t" + ops[ params[Instruction.Operand_1] ]);
			}
		};
		
		Instruction OR = new Instruction("OR",UNPREFIXED) {
			public void execute(short... params) {				
				int op1_index = params[Instruction.Operand_1];
				byte b;
				if(op1_index == Instruction.IMMD_8) {
					b = mem.readByte();
					regs.tbl[ Registers.ACCUMULATOR ] = (byte)((regs.tbl[ Registers.ACCUMULATOR ] | b)&0xFF);
				}
				else {					
					if( Registers.is16bitPointer(op1_index) ) {
						short addr = regs.getAddress(op1_index);
						b = mem.fetchByteNoPosDelta(addr);
						regs.tbl[ Registers.ACCUMULATOR ] = (byte)((regs.tbl[ Registers.ACCUMULATOR ] | b)&0xFF);
					}
					// We're just OR'ing with one of the 8 bit regs
					else {
						regs.tbl[ Registers.ACCUMULATOR ] = (byte)((regs.tbl[ Registers.ACCUMULATOR ] | regs.tbl[ op1_index ])&0xFF);
					}
				}
				regs.Flags_C_CARRY = regs.Flags_N_SUBTRACT = false;
				regs.Flags_Z_ZERO = (regs.tbl[ Registers.ACCUMULATOR ] == 0);
			}
			
			public void log(StringBuilder log, short... params) {
				log.append(getMnemonic() + "\t");
				int op1_index = params[Instruction.Operand_1];
				if(op1_index == Instruction.IMMD_8) {
					byte immd8 = mem.readByte();
					log.append(StringUtils.toHexStr(immd8));
				} else  {
					log.append( ops[ op1_index ] );
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
				byte unsigned8 = mem.readByte();
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
					short immd16 = mem.readShort();
					log.append(StringUtils.toHexStr(immd16));
				}
				else if( params[Instruction.Operand_2] == Instruction.IMMD_16 ) {
					short immd16 = mem.readShort();
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
				// disable interrupts
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
					byte immd8 = mem.readByte();
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
					byte immd8 = mem.readByte();
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
		
		/* ******************************************************************************************* */
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
