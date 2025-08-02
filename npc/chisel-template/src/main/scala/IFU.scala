// See README.md for license details.

package ifu

import chisel3._
import chisel3.util._
// _root_ disambiguates from package chisel3.util.circt if user imports chisel3.util._
import _root_.circt.stage.ChiselStage
import iobundle._
import consts.Consts._
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
    |    instr <= 0;
    |  end
    |  else begin
    |   instr <= pmem_read(pc);
    |  end
    |end
    |endmodule
    """.stripMargin)
}

class IMEM extends Module {
  val io = IO(new Bundle {
    val in_from_ifu = Flipped(Decoupled(new ifu_to_imem))
    val out_to_ifu = Decoupled(new imem_to_ifu)
  })
  val imem = Module(new instr_fetch)
  val pc = RegInit("h80000000".U(32.W))

  pc := Mux(io.in_from_ifu.valid && io.in_from_ifu.ready, io.in_from_ifu.bits.pc, pc)
  io.in_from_ifu.ready := true.B
  imem.io.pc := pc
  imem.io.reset := reset
  
  val valid_to_ifu = RegInit(false.B)
  val s_idle :: s_wait_ready :: Nil = Enum(2)
  val state = RegInit(s_idle)
  state := MuxLookup(state, s_idle)(List(
    s_idle       -> Mux(valid_to_ifu, s_wait_ready, s_idle),
    s_wait_ready -> Mux(io.out_to_ifu.ready, s_idle, s_wait_ready)
  ))
  valid_to_ifu := Mux(io.out_to_ifu.ready && state === s_wait_ready, false.B, 
                 Mux(io.in_from_ifu.valid && !(io.out_to_ifu.ready && state === s_wait_ready), true.B, 
                 valid_to_ifu))
  io.out_to_ifu.valid := valid_to_ifu
  io.out_to_ifu.bits.instr := imem.io.instr

  io.in_from_ifu.ready := true.B

  val instr_fetch = Module(new instr_fetch)

  instr_fetch.io.pc := io.in_from_ifu.bits.pc
  instr_fetch.io.reset := reset

  io.out_to_ifu.bits.instr := instr_fetch.io.instr
  io.out_to_ifu.valid := io.in_from_ifu.valid
}

class IFU extends Module {
  val io = IO(new Bundle {
    // val alu_pc = Input(UInt(32.W))
    // val jump_en = Input(Bool())
    // val out = Decoupled(new ifu_to_idu)
    val in_from_wbu = Flipped(Decoupled(new wbu_to_ifu))
    val in_imem_to_ifu = Flipped(Decoupled(new imem_to_ifu))
    val out_ifu_to_imem = Decoupled(new ifu_to_imem)
    val out = Decoupled(new ifu_to_idu)
    val finish = Output(Bool())
    
  })

  val pc = RegInit("h80000000".U(32.W))
  val snpc = WireDefault("h80000000".U(32.W))
  val dnpc = io.in_from_wbu.bits.dnpc
  
  pc := Mux(io.in_from_wbu.valid && io.in_from_wbu.ready , dnpc, pc)

  io.out_ifu_to_imem.bits.pc := pc

  io.in_from_wbu.ready := true.B

  //TO IMEM
  val valid_to_imem = RegInit(true.B)
  val s_idle :: s_wait_ready :: Nil = Enum(2)
  val state_imem = RegInit(s_idle)
  state_imem := MuxLookup(state_imem, s_idle)(List(
    s_idle       -> Mux(valid_to_imem, s_wait_ready, s_idle),
    s_wait_ready -> Mux(io.out_ifu_to_imem.ready, s_idle, s_wait_ready)
  ))
  
  valid_to_imem := Mux(io.out_ifu_to_imem.ready && state_imem === s_wait_ready, false.B, 
                 Mux(io.in_from_wbu.valid && !(io.out_ifu_to_imem.ready && state_imem === s_wait_ready), true.B, 
                 valid_to_imem))
  io.out_ifu_to_imem.valid := valid_to_imem
  io.in_imem_to_ifu.ready := true.B

  val instr = RegInit(0.U(32.W))
  instr := Mux(io.in_imem_to_ifu.valid && io.in_imem_to_ifu.ready, io.in_imem_to_ifu.bits.instr, instr)

  //TO IDU
  val valid_to_idu =RegInit(false.B)
  // val s_idle :: s_wait_ready :: Nil = Enum(2)
  val state = RegInit(s_idle)
  state := MuxLookup(state, s_idle)(List(
    s_idle       -> Mux(valid_to_idu, s_wait_ready, s_idle),
    s_wait_ready -> Mux(io.out.ready, s_idle, s_wait_ready)
  ))
  valid_to_idu := Mux(io.out.ready && state === s_wait_ready, false.B, 
                 Mux(io.in_imem_to_ifu.valid && !(io.in_imem_to_ifu.ready && state === s_wait_ready), true.B, 
                 valid_to_idu))

  io.out.valid := valid_to_idu



val finishReg = RegInit(false.B)
val valid_in_from_wbu_reg0 = RegInit(false.B) 
val valid_in_from_wbu_reg1 = RegInit(false.B)

valid_in_from_wbu_reg0 := io.in_from_wbu.valid
valid_in_from_wbu_reg1 := valid_in_from_wbu_reg0
val valid_in_from_wbu_falling_edge = WireDefault(false.B)

valid_in_from_wbu_falling_edge := valid_in_from_wbu_reg1 && !valid_in_from_wbu_reg0
when(io.finish === true.B) {
  finishReg := false.B
} .elsewhen(  valid_in_from_wbu_falling_edge) {
  finishReg := true.B
} .otherwise {
  finishReg := false.B
}

io.finish := finishReg


snpc := pc + 4.U(32.W)
io.out.bits.instr :=instr
io.out.bits.snpc :=snpc
io.out.bits.pc :=pc

}
/**
 * Generate Verilog sources and save it in file GCD.v
 
object IFUMain extends App {
  ChiselStage.emitSystemVerilogFile(
    new IFU,
    firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info", "-o=../", "--split-verilog")
  )
}*/
