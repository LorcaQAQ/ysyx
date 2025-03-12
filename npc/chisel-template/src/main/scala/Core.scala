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
import mem._
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
class instr_fetch extends HasBlackBoxInline {
  val io = IO(new Bundle {
    val pc = Input(UInt(32.W))
    val reset = Input(Bool())
    val instr = Output(UInt(32.W))
  })

  setInline("instr_fetch.v",
    s"""
    |module instr_fetch(
    |  input [31:0] pc,
    |  input reset,
    |  output reg [31:0]  instr
    |);
    |import "DPI-C" function int pmem_read(input int addr); 
    |
    |always @(pc or reset) begin
    |  if(reset) begin
    |    instr = 0;
    |  end
    |  else begin
    |   instr = pmem_read(pc);
    |  end
    |end
    |endmodule
    """.stripMargin)
}
class Core extends Module {
  val io = IO(new Bundle {
    val instr   = Output(UInt(32.W))
    val pc   = Output(UInt(32.W))
    val result = Output(UInt(32.W))
  })

    val ifu = Module(new IFU)
    val idu = Module(new IDU)
    val exu = Module(new EXU)
    val regfile = Module(new RegFile(32,4))
    val get_instruction = Module(new get_instruction)
    val instr_fetch = Module(new instr_fetch)
    val mem = Module(new MEM(32))

    get_instruction.io.instr := io.instr

    instr_fetch.io.reset := reset
    instr_fetch.io.pc := ifu.io.pc
    io.instr := instr_fetch.io.instr

    io.pc := ifu.io.pc
    
    //jump logic/branch logic
    val jal_en = (idu.io.jump_op === JUMP)
    val branch_en = (idu.io.jump_op === JUMP_COND && exu.io.result =/= 0.U)

    ifu.io.alu_pc :=  MuxCase(ifu.io.pc, Array(
      (idu.io.jump_op === JUMP) -> exu.io.result,
      (idu.io.jump_op === NO_JUMP) -> ifu.io.pc,
      (idu.io.jump_op === JUMP_COND) -> Mux(branch_en , ifu.io.pc+idu.io.imm, exu.io.result)
    ))
    ifu.io.jump_en := jal_en|| branch_en
    //IDU IO
    idu.io.instr := io.instr

    //regfile IO
    regfile.io.waddr := idu.io.rd
    regfile.io.wdata := MuxCase(0.U, Array(
      (idu.io.rf_wdata_sel === REG_DATA_ALU) -> exu.io.result,
      (idu.io.rf_wdata_sel === REG_DATA_PC) -> ifu.io.snpc,
      (idu.io.rf_wdata_sel === REG_DATA_MEM) -> mem.io.mem_rdata
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
    exu.io.val2 := Mux(idu.io.alu_op2_sel === OP2_RS2_IMMB || idu.io.alu_op2_sel === OP2_RS2, regfile.io.rdata2, idu.io.imm)

    //MEM IO
    mem.io.valid := idu.io.mem_valid
    mem.io.mem_wen := idu.io.mem_wen
    mem.io.mem_waddr := exu.io.result
    mem.io.mem_wdata := regfile.io.rdata2
    mem.io.mem_raddr := exu.io.result
    mem.io.clk := clock
    io.result := exu.io.val1 
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


