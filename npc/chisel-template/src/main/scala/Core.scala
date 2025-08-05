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
import csr._
import iobundle._
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
        val finish = Output(Bool())
      })
  
  val ifu = Module(new IFU)
  val imem = Module(new IMEM)
  val idu = Module(new IDU)
  val exu = Module(new EXU)
  val regs = Module(new Reg_CSR(32,4))
  val lsu = Module(new LSU)
  val mem_slaver = Module(new MEM_SLAVER)
  val mem = Module(new MEM(32,10))
  val get_instruction = Module(new get_instruction)

  ifu.io.out <> idu.io.in_from_ifu
  ifu.io.out_ifu_to_imem <> imem.io.in_from_ifu
  imem.io.out_to_ifu <> ifu.io.in_imem_to_ifu
  idu.io.out_to_exu <> exu.io.in_from_idu
  idu.io.out_to_regfile <> regs.io.in_idu_to_regfile
  idu.io.out_to_csr <> regs.io.in_idu_to_csr
  exu.io.in_from_regfile <> regs.io.out_regfile_to_exu
  exu.io.in_from_csr <> regs.io.out_csr_to_exu
  exu.io.out<> lsu.io.in_from_exu
  lsu.io.r<>mem_slaver.io.r
  lsu.io.ar<>mem_slaver.io.ar
  lsu.io.aw<>mem_slaver.io.aw
  lsu.io.w<>mem_slaver.io.w
  lsu.io.b<>mem_slaver.io.b

  mem_slaver.io.mem_ren <> mem.io.mem_ren
  mem_slaver.io.mem_raddr <> mem.io.mem_raddr
  mem_slaver.io.mem_rdata_valid <> mem.io.mem_rdata_valid
  mem_slaver.io.mem_rdata <> mem.io.mem_rdata

  mem_slaver.io.mem_wen <> mem.io.mem_wen
  mem_slaver.io.mem_waddr <> mem.io.mem_waddr
  mem_slaver.io.mem_wdata <> mem.io.mem_wdata
  mem_slaver.io.mem_wstrb <> mem.io.mem_wstrb
  mem_slaver.io.mem_write_finished <> mem.io.mem_write_finished

  mem.io.clk := clock
  mem.io.reset := reset

  lsu.io.out <> regs.io.in_from_mem
  regs.io.out_to_ifu <> ifu.io.in_from_wbu

  get_instruction.io.instr := Mux(ifu.io.finish, ifu.io.out.bits.instr, 0.U)
  

  io.instr := ifu.io.out.bits.instr
  io.pc := ifu.io.out.bits.pc
  io.result := exu.io.out.bits.result

  io.finish := ifu.io.finish
}
/**
 * Generate Verilog sources and save it in file GCD.v
 */
object CoreMain extends App {
  ChiselStage.emitSystemVerilogFile(
    new Core,
    firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info", "-o=../vsrc", "--split-verilog","--lowering-options=disallowLocalVariables, disallowPackedArrays,locationInfoStyle=wrapInAtSquareBracket")
  )
  }


