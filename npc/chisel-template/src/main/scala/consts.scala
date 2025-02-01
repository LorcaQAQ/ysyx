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
  //REG WRITE DATA
    val REG_DATA_LEN=1
    val REG_DATA_ALU = 0.U(REG_DATA_LEN.W)
    val REG_DATA_PC = 1.U(REG_DATA_LEN.W)
  //ALU OPERATION
    val ALU_LEN=4
    val ALU_ADD = 0.U(ALU_LEN.W)
    val ALU_JALR = 1.U(ALU_LEN.W)
    //JUMP
    val JUMP_LEN=1
    val JUMP = 1.U(JUMP_LEN.W)
    val NO_JUMP = 0.U(JUMP_LEN.W)
}
