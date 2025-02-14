// See README.md for license details.

package core
import chisel3._
import chisel3.util._
// _root_ disambiguates from package chisel3.util.circt if user imports chisel3.util._
import _root_.circt.stage.ChiselStage

import ifu._
import regfile._
import idu._
import exu._
import instructions.RV32I._
import consts.Consts._
/* 
class ebreak extends HasBlackBoxInline {
  val io = IO(new Bundle {
    val instr = Input(UInt(32.W))
  })

  setInline("ebreak.v",
    s"""
    |module ebreak(
    |  input [31:0] instr
    |);
    |  import "DPI-C" function void stop_simulation();
    |  wire nemu_state_stop;
    |  assign nemu_state_stop=instr==32'b0000000_00001_00000_000_00000_11100_11;
    |
    |  always @(nemu_state_stop)
    |  if(nemu_state_stop==1)
    |   stop_simulation();
    |endmodule
    """.stripMargin)
}

 */
class get_instruction extends HasBlackBoxInline {
  val io = IO(new Bundle {
    val instr = Input(UInt(32.W))
  })

  setInline("get_instruction.v",
    s"""
    |module get_instruction(
    |  input [31:0] instr
    |);
    |  function bit [31:0] get_instr;
    |     get_instr=instr;
    |  endfunction
    |  export "DPI-C" function get_instr;
    |endmodule
    """.stripMargin)
}
class Core extends Module {
  val io = IO(new Bundle {
    val instr   = Input(UInt(32.W))
    val pc   = Output(UInt(32.W))
    val result = Output(UInt(32.W))
  })

    val ifu = Module(new IFU)
    val idu = Module(new IDU)
    val exu = Module(new EXU)
    val regfile = Module(new RegFile(32,4))
    val get_instruction = Module(new get_instruction)

    get_instruction.io.instr := io.instr

    io.pc := ifu.io.pc
    
    ifu.io.alu_pc := exu.io.result
    ifu.io.jump_en := idu.io.jump_en

    //IDU IO
    idu.io.instr := io.instr

    //regfile IO
    regfile.io.waddr := idu.io.rd
    regfile.io.wdata := MuxCase(0.U, Array(
      (idu.io.rf_wdata_sel === REG_DATA_ALU) -> exu.io.result,
      (idu.io.rf_wdata_sel === REG_DATA_PC) -> ifu.io.snpc
    ))
    regfile.io.wen := idu.io.rf_wen
    regfile.io.raddr1 := idu.io.rs1
    regfile.io.raddr2 := idu.io.rs2
 
    
    //EXU IO
    exu.io.alu_op := idu.io.alu_op
    exu.io.val1 := MuxCase(OP1_X, Array(
      (idu.io.alu_op1_sel === OP1_X) -> regfile.io.rdata1,
      (idu.io.alu_op1_sel === OP1_RS1) -> regfile.io.rdata1,
      (idu.io.alu_op1_sel === OP1_PC) -> ifu.io.pc
    ))
    exu.io.val2 := Mux(idu.io.alu_op2_sel === OP2_RS2, regfile.io.rdata2, idu.io.imm)

    io.result := exu.io.result
}

/**
 * Generate Verilog sources and save it in file GCD.v
 */
object CoreMain extends App {
  ChiselStage.emitSystemVerilogFile(
    new Core,
    firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info", "-o=../vsrc", "--split-verilog")
  )
}


