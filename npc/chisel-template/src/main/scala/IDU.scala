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

    val jump_en = Output(Bool())


  })

    //defining imm_i
    val imm_i = Cat(Fill(20, io.instr(31)), io.instr(31, 20))
    val imm_u = Cat(io.instr(31, 12), Fill(12, 0.U))
    val imm_j = Cat(Fill(12, io.instr(31)), io.instr(19, 12), 
    io.instr(20), io.instr(30, 25),io.instr(24, 21), 0.U)



    val csignals =ListLookup(
      io.instr,List(REG_WRITE_X,OP1_X,OP2_RS2,REG_DATA_ALU,ALU_ADD,NO_JUMP),
      Array(
        ADDI -> List(REG_WRITE_EN,OP1_RS1,OP2_IMMI,REG_DATA_ALU,ALU_ADD,NO_JUMP),
        AUIPC -> List(REG_WRITE_EN,OP1_PC,OP2_IMMU,REG_DATA_ALU,ALU_ADD,NO_JUMP),
        LUI -> List(REG_WRITE_EN,OP1_X,OP2_IMMU,REG_DATA_ALU,ALU_ADD,NO_JUMP),
        JAL -> List(REG_WRITE_EN,OP1_PC,OP2_IMMJ,REG_DATA_PC,ALU_ADD,JUMP),
        JALR -> List(REG_WRITE_EN,OP1_RS1,OP2_IMMI,REG_DATA_PC,ALU_JALR,JUMP)
        
      )
    )

    val rf_wen::op1_sel::op2_sel::rf_wdata_sel::alu_op::jump_en::Nil= csignals

    io.rs1 :=Mux(op1_sel === OP1_RS1, io.instr(19, 15), 0.U)
    io.rs2 :=Mux(op2_sel === OP2_RS2, io.instr(24, 20), 0.U)
    io.rd :=Mux(rf_wen === REG_WRITE_EN, io.instr(11, 7), 0.U)
    io.imm := MuxCase(0.U, Array(
      (op2_sel === OP2_IMMI) -> imm_i,
      (op2_sel === OP2_IMMU) -> imm_u,
      (op2_sel === OP2_IMMJ) -> imm_j
    ))

    io.rf_wen := Mux(rf_wen === REG_WRITE_EN, true.B, false.B)


    io.alu_op1_sel := op1_sel
    io.alu_op2_sel := op2_sel
    io.alu_op := alu_op

    io.rf_wdata_sel := rf_wdata_sel
    io.jump_en := jump_en
    
}

/**
 * Generate Verilog sources and save it in file GCD.v

object IFUMain extends App {
  ChiselStage.emitSystemVerilogFile(
    new IFU,
    firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info", "-o=../", "--split-verilog")
  )
}
 */
