package consts

import chisel3._
import chisel3.util._
object Consts{
  //instruction decoder
  //REG WRITE ENABLE
    val REG_LEN=1
    val REG_WRITE_X = 0.U(REG_LEN.W)
    val REG_WRITE_EN = 1.U(REG_LEN.W)
  //opcode1
    val OP1_LEN=3
    val OP1_X = 0.U(OP1_LEN.W)
    val OP1_RS1 = 1.U(OP1_LEN.W)
    val OP1_PC = 2.U(OP1_LEN.W)
  //opcode2
    val OP2_LEN=3
    val OP2_RS2 = 0.U(OP2_LEN.W)
    val OP2_IMMI = 1.U(OP2_LEN.W)
    val OP2_IMMU = 2.U(OP2_LEN.W)
    val OP2_IMMJ = 3.U(OP2_LEN.W)
    val OP2_IMMS = 4.U(OP2_LEN.W)
    val OP2_RS2_IMMB = 5.U(OP2_LEN.W)
    val OP2_IMMS_RS2 = 6.U(OP2_LEN.W)
  //REG WRITE DATA
    val REG_DATA_LEN=2
    val REG_DATA_ALU = 1.U(REG_DATA_LEN.W)
    val REG_DATA_PC = 2.U(REG_DATA_LEN.W)
    val REG_DATA_X = 0.U(REG_DATA_LEN.W)
    val REG_DATA_MEM = 3.U(REG_DATA_LEN.W)
  //ALU OPERATION
    val ALU_LEN=4
    val ALU_ADD = 1.U(ALU_LEN.W)
    val ALU_JALR = 2.U(ALU_LEN.W)
    val ALU_SLTU = 3.U(ALU_LEN.W)
    val ALU_BNE = 4.U(ALU_LEN.W)
    val ALU_SUB = 5.U(ALU_LEN.W)
    val ALU_XOR = 6.U(ALU_LEN.W)
    val ALU_SRA = 7.U(ALU_LEN.W)
    val ALU_X= 0.U(ALU_LEN.W)
    val ALU_SLL = 8.U(ALU_LEN.W)  
    val ALU_AND = 9.U(ALU_LEN.W)
    val ALU_OR = 10.U(ALU_LEN.W)
    //JUMP
    val JUMP_LEN=2
    val JUMP = 1.U(JUMP_LEN.W)
    val NO_JUMP = 0.U(JUMP_LEN.W)
    val JUMP_COND = 2.U(JUMP_LEN.W)
    //Memory
    val MEM_LEN=2
    val MEM_X = 0.U(MEM_LEN.W)
    val MEM_WRITE = 1.U(MEM_LEN.W)
    val MEM_READ = 2.U(MEM_LEN.W)
    //Load and Store
    val LOAD_LEN=2
    val LOAD_Store_X = 0.U(LOAD_LEN.W)
    val Word = 1.U(LOAD_LEN.W)
    val BYTE_U = 2.U(LOAD_LEN.W)
    val Half_U = 3.U(LOAD_LEN.W)
}
