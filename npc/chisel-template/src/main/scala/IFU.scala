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
    |    instr = 0;
    |  end
    |  else begin
    |   instr = pmem_read(pc);
    |  end
    |end
    |endmodule
    """.stripMargin)
}

class IFU extends Module {
  val io = IO(new Bundle {
    // val alu_pc = Input(UInt(32.W))
    // val jump_en = Input(Bool())
    // val out = Decoupled(new ifu_to_idu)
    val in_from_wbu = Flipped(Decoupled(new wbu_to_ifu))
    val out = Decoupled(new ifu_to_idu)
    val finish = Output(Bool())
    
  })
  val instr_fetch =Module(new instr_fetch)

  val pc = RegInit("h80000000".U(32.W))
  val snpc = WireDefault("h80000000".U(32.W))


  val alu_op = io.in_from_wbu.bits.alu_op
  val jump_op = io.in_from_wbu.bits.jump_op
  val result = io.in_from_wbu.bits.result
  val imm = io.in_from_wbu.bits.imm
  val csr_pc = io.in_from_wbu.bits.csr_pc
  val dnpc = io.in_from_wbu.bits.dnpc

  // val jal_en = jump_op === JUMP || jump_op === JUMP_CSR

  // val branch_en = jump_op === JUMP_COND&&
  //                   result === 0.U &&
  //                   ( alu_op ===ALU_BNE  ||   
  //                     alu_op ===ALU_BGE  ||
  //                     alu_op ===ALU_BEQ  ||
  //                     alu_op ===ALU_BGEU ||
  //                     alu_op ===ALU_BLTU ||
  //                     alu_op ===ALU_BLT  )
  // val alu_pc = MuxCase(pc, Array(
  //   (jump_op === JUMP) -> result,
  //   (jump_op === NO_JUMP) -> pc,
  //   (jump_op === JUMP_COND) -> Mux(branch_en , pc+imm, pc),
  //   (jump_op === JUMP_CSR) -> csr_pc
  // ))
  // val jump_en = jal_en || branch_en
  
  pc := Mux(io.in_from_wbu.valid && io.in_from_wbu.ready , dnpc, pc)

  io.in_from_wbu.ready := true.B
  instr_fetch.io.pc := pc
  instr_fetch.io.reset := reset


  val valid_to_idu =RegInit(true.B)
  val s_idle :: s_wait_ready :: Nil = Enum(2)
  val state = RegInit(s_idle)
  state := MuxLookup(state, s_idle)(List(
    s_idle       -> Mux(valid_to_idu, s_wait_ready, s_idle),
    s_wait_ready -> Mux(io.out.ready, s_idle, s_wait_ready)
  ))
  valid_to_idu := Mux(io.out.ready && state === s_wait_ready, false.B, 
                 Mux(io.in_from_wbu.valid && !(io.out.ready && state === s_wait_ready), true.B, 
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

  // val instr = RegEnable(instr_fetch.io.instr, instr, io.out.ready && io.out.valid)
  val instr = instr_fetch.io.instr

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
