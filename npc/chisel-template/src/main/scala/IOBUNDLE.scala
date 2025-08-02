package iobundle
import chisel3._
import chisel3.util._
import consts.Consts._
class ifu_to_idu extends Bundle {
  val instr= Output(UInt(32.W))
  val pc   = Output(UInt(32.W))
  val snpc = Output(UInt(32.W))
}

class idu_to_exu extends Bundle {
  val alu_op = Output(UInt(ALU_LEN.W))
  val alu_op1_sel = Output(UInt(OP1_LEN.W))
  val alu_op2_sel = Output(UInt(OP2_LEN.W))
  val rf_wdata_sel = Output(UInt(REG_DATA_LEN.W))
  val rf_wen = Output(Bool())
  val imm   = Output(UInt(32.W))
  val rd_addr   = Output(UInt(5.W))
  val mem_wen = Output(Bool())
  val mem_ren = Output(Bool()) 
  val csr_r_w_addr=Output(UInt(12.W)) 
  val csr_r_w_ctrl=Output(UInt(CSR_R_W_CTRL_LEN.W))
  
  val pc = Output(UInt(32.W))
  val snpc = Output(UInt(32.W))

  val jump_op = Output(UInt(JUMP_LEN.W))
  val load_store_range = Output(UInt(LOAD_LEN.W))
    
  val csr_alu_op=Output(UInt(CSR_ALU_CTRL_LEN.W))
}
class idu_to_regfile extends Bundle {
  val rs1_addr    = Output(UInt(5.W))
  val rs2_addr    = Output(UInt(5.W))
}

class regfile_to_exu extends Bundle {
  val rs1 = Output(UInt(32.W))
  val rs2 = Output(UInt(32.W))
}

class idu_to_csr extends Bundle {
  val csr_r_addr    = Output(UInt(12.W))
}

class csr_to_exu extends Bundle {
  val csr = Output(UInt(32.W))
}

class exu_to_mem extends Bundle {
  val alu_op = Output(UInt(ALU_LEN.W)) 
  val rf_wdata_sel = Output(UInt(REG_DATA_LEN.W))
  val rf_wen = Output(Bool())
  val rd_addr = Output(UInt(32.W))
  val result = Output(UInt(32.W))
  
  //mem control signals
  val load_store_range = Output(UInt(LOAD_LEN.W))
  val mem_wen = Output(Bool())
  val mem_ren = Output(Bool())
  val rs2 = Output(UInt(32.W))
  val snpc = Output(UInt(32.W))
  val pc = Output(UInt(32.W))
  // val dnpc = Output(UInt(32.W))
  
  val imm = Output(UInt(32.W))
  //jump control signal
  val jump_op = Output(UInt(JUMP_LEN.W))
  val csr_result = Output(UInt(32.W))
  val csr_r_w_addr = Output(UInt(12.W))
  val csr_r_w_ctrl = Output(UInt(CSR_R_W_CTRL_LEN.W))
}

class mem_to_wbu extends Bundle {
  val alu_op = Output(UInt(ALU_LEN.W))
  //write back control signals
  val rf_wen = Output(Bool())
  val rf_wdata_sel = Output(UInt(REG_DATA_LEN.W))
  val rd_addr = Output(UInt(5.W))
  val result = Output(UInt(32.W))
  val mem_rdata = Output(UInt(32.W))
  val snpc =Output(UInt(32.W))
  val pc = Output(UInt(32.W))
  // val dnpc = Output(UInt(32.W))

  val csr_r_w_addr = Output(UInt(32.W))
  val csr_result = Output(UInt(32.W))
  val csr_r_w_ctrl = Output(UInt(CSR_R_W_CTRL_LEN.W))

  val imm = Output(UInt(32.W))
  //jump control signal
  val jump_op = Output(UInt(JUMP_LEN.W))
  
  
}
class wbu_to_ifu extends Bundle {
  val alu_op = Output(UInt(ALU_LEN.W))
  val jump_op = Output(UInt(JUMP_LEN.W))
  val result =Output(UInt(32.W))
  val imm = Output(UInt(32.W))
  val csr_pc = Output(UInt(32.W))
  val dnpc = Output(UInt(32.W))
}
class ifu_to_imem extends Bundle {
  val pc = Output(UInt(32.W))
}

class imem_to_ifu extends Bundle {
  val instr = Output(UInt(32.W))
}