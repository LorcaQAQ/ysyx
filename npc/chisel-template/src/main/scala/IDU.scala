// See README.md for license details.

package idu

import chisel3._
import chisel3.util._
// _root_ disambiguates from package chisel3.util.circt if user imports chisel3.util._
import _root_.circt.stage.ChiselStage
import instructions.RV32I._
import consts.Consts._

class IDU extends Module {
  val io = IO(new Bundle {
    val instr   = Input(UInt(32.W))
    val rs1    = Output(UInt(5.W))
    val rs2    = Output(UInt(5.W))
    val rd   = Output(UInt(5.W))
    val imm   = Output(UInt(32.W))
    

    val rf_wen = Output(Bool())
    val rf_wdata_sel = Output(UInt(REG_DATA_LEN.W))

    val alu_op1_sel = Output(UInt(OP1_LEN.W))
    val alu_op2_sel = Output(UInt(OP2_LEN.W))
    val alu_op = Output(UInt(ALU_LEN.W))

    val jump_op = Output(UInt(JUMP_LEN.W))

    val mem_wen = Output(Bool())
    val mem_ren = Output(Bool())
    val load_store_range = Output(UInt(LOAD_LEN.W))

    val csr_r_w_ctrl=Output(UInt(CSR_R_W_CTRL_LEN.W))
    val csr_r_w_addr=Output(UInt(12.W))

    val csr_alu_op=Output(UInt(CSR_ALU_CTRL_LEN.W))
  })

    //defining imm_i
    val imm_i = Cat(Fill(20, io.instr(31)), io.instr(31, 20))
    val imm_u = Cat(io.instr(31, 12), Fill(12, 0.U))
    val imm_j = Cat(Fill(12, io.instr(31)), io.instr(19, 12), io.instr(20), io.instr(30, 25),io.instr(24, 21), 0.U)
    val imm_s = Cat(Fill(20, io.instr(31)), io.instr(31, 25), io.instr(11, 7))
    val imm_b = Cat(Fill(19, io.instr(31)), io.instr(31), io.instr(7), io.instr(30, 25), io.instr(11, 8), 0.U)
    val imm_csr =Cat(Fill(20, 0.U), io.instr(31, 20))

    val csignals =ListLookup(
      io.instr,List(REG_WRITE_X,OP1_X,OP2_RS2,REG_DATA_X,ALU_X,NO_JUMP,MEM_X,LOAD_Store_X,CSR_R_W_X,CSR_ALU_X),
      Array(
        ADDI -> List(REG_WRITE_EN,OP1_RS1,OP2_IMMI,REG_DATA_ALU,ALU_ADD,NO_JUMP,MEM_X,LOAD_Store_X,CSR_R_W_X,CSR_ALU_X),
        AUIPC -> List(REG_WRITE_EN,OP1_PC,OP2_IMMU,REG_DATA_ALU,ALU_ADD,NO_JUMP,MEM_X,LOAD_Store_X,CSR_R_W_X,CSR_ALU_X),
        LUI -> List(REG_WRITE_EN,OP1_X,OP2_IMMU,REG_DATA_ALU,ALU_ADD,NO_JUMP,MEM_X,LOAD_Store_X,CSR_R_W_X,CSR_ALU_X),
        JAL -> List(REG_WRITE_EN,OP1_PC,OP2_IMMJ,REG_DATA_PC,ALU_ADD,JUMP,MEM_X,LOAD_Store_X,CSR_R_W_X,CSR_ALU_X),
        JALR -> List(REG_WRITE_EN,OP1_RS1,OP2_IMMI,REG_DATA_PC,ALU_JALR,JUMP,MEM_X,LOAD_Store_X,CSR_R_W_X,CSR_ALU_X),
        LW -> List(REG_WRITE_EN,OP1_RS1,OP2_IMMI,REG_DATA_MEM,ALU_ADD,NO_JUMP,MEM_READ,Word,CSR_R_W_X,CSR_ALU_X),
        SW -> List(REG_WRITE_X,OP1_RS1,OP2_IMMS,REG_DATA_X,ALU_ADD,NO_JUMP,MEM_WRITE,Word,CSR_R_W_X,CSR_ALU_X),
        SLTIU -> List(REG_WRITE_EN,OP1_RS1,OP2_IMMI,REG_DATA_ALU,ALU_SLTU,NO_JUMP,MEM_X,LOAD_Store_X,CSR_R_W_X,CSR_ALU_X),
        BNE -> List(REG_WRITE_X,OP1_RS1,OP2_RS2_IMMB,REG_DATA_X,ALU_BNE,JUMP_COND,MEM_X,LOAD_Store_X,CSR_R_W_X,CSR_ALU_X),
        SUB -> List(REG_WRITE_EN,OP1_RS1,OP2_RS2,REG_DATA_ALU,ALU_SUB,NO_JUMP,MEM_X,LOAD_Store_X,CSR_R_W_X,CSR_ALU_X),
        ADD -> List(REG_WRITE_EN,OP1_RS1,OP2_RS2,REG_DATA_ALU,ALU_ADD,NO_JUMP,MEM_X,LOAD_Store_X,CSR_R_W_X,CSR_ALU_X),
        XOR -> List(REG_WRITE_EN,OP1_RS1,OP2_RS2,REG_DATA_ALU,ALU_XOR,NO_JUMP,MEM_X,LOAD_Store_X,CSR_R_W_X,CSR_ALU_X),
        SLTU -> List(REG_WRITE_EN,OP1_RS1,OP2_RS2,REG_DATA_ALU,ALU_SLTU,NO_JUMP,MEM_X,LOAD_Store_X,CSR_R_W_X,CSR_ALU_X),
        SRAI -> List(REG_WRITE_EN,OP1_RS1,OP2_IMMI,REG_DATA_ALU,ALU_SRA,NO_JUMP,MEM_X,LOAD_Store_X,CSR_R_W_X,CSR_ALU_X),
        LBU -> List(REG_WRITE_EN,OP1_RS1,OP2_IMMI,REG_DATA_MEM,ALU_ADD,NO_JUMP,MEM_READ,BYTE_U,CSR_R_W_X,CSR_ALU_X),
        SH -> List(REG_WRITE_X,OP1_RS1,OP2_IMMS,REG_DATA_X,ALU_ADD,NO_JUMP,MEM_WRITE,Half_U,CSR_R_W_X,CSR_ALU_X),
        SLL -> List(REG_WRITE_EN,OP1_RS1,OP2_RS2,REG_DATA_ALU,ALU_SLL,NO_JUMP,MEM_X,LOAD_Store_X,CSR_R_W_X,CSR_ALU_X),
        AND -> List(REG_WRITE_EN,OP1_RS1,OP2_RS2,REG_DATA_ALU,ALU_AND,NO_JUMP,MEM_X,LOAD_Store_X,CSR_R_W_X,CSR_ALU_X),
        ANDI -> List(REG_WRITE_EN,OP1_RS1,OP2_IMMI,REG_DATA_ALU,ALU_AND,NO_JUMP,MEM_X,LOAD_Store_X,CSR_R_W_X,CSR_ALU_X),
        XORI -> List(REG_WRITE_EN,OP1_RS1,OP2_IMMI,REG_DATA_ALU,ALU_XOR,NO_JUMP,MEM_X,LOAD_Store_X,CSR_R_W_X,CSR_ALU_X),
        OR -> List(REG_WRITE_EN,OP1_RS1,OP2_RS2,REG_DATA_ALU,ALU_OR,NO_JUMP,MEM_X,LOAD_Store_X,CSR_R_W_X,CSR_ALU_X),
        SB -> List(REG_WRITE_X,OP1_RS1,OP2_IMMS,REG_DATA_X,ALU_ADD,NO_JUMP,MEM_WRITE,BYTE_U,CSR_R_W_X,CSR_ALU_X),
        BGE -> List(REG_WRITE_X,OP1_RS1,OP2_RS2_IMMB,REG_DATA_X,ALU_BGE,JUMP_COND,MEM_X,LOAD_Store_X,CSR_R_W_X,CSR_ALU_X),
        BEQ -> List(REG_WRITE_X,OP1_RS1,OP2_RS2_IMMB,REG_DATA_X,ALU_BEQ,JUMP_COND,MEM_X,LOAD_Store_X,CSR_R_W_X,CSR_ALU_X),
        SRLI -> List(REG_WRITE_EN,OP1_RS1,OP2_IMMI,REG_DATA_ALU,ALU_SRL,NO_JUMP,MEM_X,LOAD_Store_X,CSR_R_W_X,CSR_ALU_X),
        SLLI -> List(REG_WRITE_EN,OP1_RS1,OP2_IMMI,REG_DATA_ALU,ALU_SLL,NO_JUMP,MEM_X,LOAD_Store_X,CSR_R_W_X,CSR_ALU_X),
        BGEU -> List(REG_WRITE_X,OP1_RS1,OP2_RS2_IMMB,REG_DATA_X,ALU_BGEU,JUMP_COND,MEM_X,LOAD_Store_X,CSR_R_W_X,CSR_ALU_X),
        BLTU -> List(REG_WRITE_X,OP1_RS1,OP2_RS2_IMMB,REG_DATA_X,ALU_BLTU,JUMP_COND,MEM_X,LOAD_Store_X,CSR_R_W_X,CSR_ALU_X),
        SLT -> List(REG_WRITE_EN,OP1_RS1,OP2_RS2,REG_DATA_ALU,ALU_SLT,NO_JUMP,MEM_X,LOAD_Store_X,CSR_R_W_X,CSR_ALU_X),
        BLT -> List(REG_WRITE_X,OP1_RS1,OP2_RS2_IMMB,REG_DATA_X,ALU_BLT,JUMP_COND,MEM_X,LOAD_Store_X,CSR_R_W_X,CSR_ALU_X),
        LH -> List(REG_WRITE_EN,OP1_RS1,OP2_IMMI,REG_DATA_MEM,ALU_ADD,NO_JUMP,MEM_READ,Half_S,CSR_R_W_X,CSR_ALU_X),
        LHU -> List(REG_WRITE_EN,OP1_RS1,OP2_IMMI,REG_DATA_MEM,ALU_ADD,NO_JUMP,MEM_READ,Half_U,CSR_R_W_X,CSR_ALU_X),
        SRA -> List(REG_WRITE_EN,OP1_RS1,OP2_RS2,REG_DATA_ALU,ALU_SRA,NO_JUMP,MEM_X,LOAD_Store_X,CSR_R_W_X,CSR_ALU_X),
        SRL -> List(REG_WRITE_EN,OP1_RS1,OP2_RS2,REG_DATA_ALU,ALU_SRL,NO_JUMP,MEM_X,LOAD_Store_X,CSR_R_W_X,CSR_ALU_X),
        LB -> List(REG_WRITE_EN,OP1_RS1,OP2_IMMI,REG_DATA_MEM,ALU_ADD,NO_JUMP,MEM_READ,BYTE_S,CSR_R_W_X,CSR_ALU_X),
        ORI -> List(REG_WRITE_EN,OP1_RS1,OP2_IMMI,REG_DATA_ALU,ALU_OR,NO_JUMP,MEM_X,LOAD_Store_X,CSR_R_W_X,CSR_ALU_X),
        SLTI -> List(REG_WRITE_EN,OP1_RS1,OP2_IMMI,REG_DATA_ALU,ALU_SLT,NO_JUMP,MEM_X,LOAD_Store_X,CSR_R_W_X,CSR_ALU_X),
        CSRRW -> List(REG_WRITE_EN,OP1_RS1,OP2_CSR,REG_DATA_ALU,ALU_COPY_CSR,NO_JUMP,MEM_X,LOAD_Store_X,CSR_R_W_EN,CSR_ALU_COPY),
        CSRRS -> List(REG_WRITE_EN,OP1_RS1,OP2_CSR,REG_DATA_ALU,ALU_COPY_CSR,NO_JUMP,MEM_X,LOAD_Store_X,CSR_R_W_EN,CSR_ALU_OR),
        ECALL -> List(REG_WRITE_X,OP1_X,OP2_RS2,REG_DATA_X,ALU_X,JUMP_CSR,MEM_X,LOAD_Store_X,CSR_ECALL,CSR_ALU_X),
        MRET -> List(REG_WRITE_X,OP1_X,OP2_RS2,REG_DATA_X,ALU_X,JUMP_CSR,MEM_X,LOAD_Store_X,CSR_MRET,CSR_ALU_X),
      )
    )


    val rf_wen::op1_sel::op2_sel::rf_wdata_sel::alu_op::jump_op::mem_op::load_store_range::csr_r_w_ctrl::csr_alu_op::Nil=csignals

    io.rs1 :=io.instr(19, 15)
    io.rs2 :=io.instr(24, 20)
    io.rd :=Mux(rf_wen === REG_WRITE_EN, io.instr(11, 7), 0.U)
    io.imm := MuxCase(0.U, Array(
      (op2_sel === OP2_IMMI) -> imm_i,
      (op2_sel === OP2_IMMU) -> imm_u,
      (op2_sel === OP2_IMMJ) -> imm_j,
      (op2_sel === OP2_IMMS) -> imm_s,
      (op2_sel === OP2_RS2_IMMB) -> imm_b
    ))

    io.rf_wen := Mux(rf_wen === REG_WRITE_EN, true.B, false.B)


    io.alu_op1_sel := op1_sel
    io.alu_op2_sel := op2_sel
    io.alu_op := alu_op

    io.rf_wdata_sel := rf_wdata_sel
    io.jump_op := jump_op

    io.mem_wen := mem_op === MEM_WRITE
    io.mem_ren := mem_op === MEM_READ
    io.load_store_range := load_store_range

    io.csr_r_w_addr := MuxCase(0.U, Array(
      (csr_r_w_ctrl === CSR_R_W_EN) -> imm_csr,
    ))
  io.csr_r_w_ctrl :=csr_r_w_ctrl
  io.csr_alu_op :=csr_alu_op
}

