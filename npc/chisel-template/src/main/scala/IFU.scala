// See README.md for license details.

package ifu

import chisel3._
import chisel3.util._
// _root_ disambiguates from package chisel3.util.circt if user imports chisel3.util._
import _root_.circt.stage.ChiselStage


class IFU extends Module {
  val io = IO(new Bundle {
    val alu_pc = Input(UInt(32.W))
    val jump_en = Input(Bool())
    val pc   = Output(UInt(32.W))
    val snpc = Output(UInt(32.W))
  })

  val pc = RegInit("h80000000".U(32.W))
  val snpc = pc + 4.U(32.W)
  val dnpc = WireDefault(0.U(32.W))

  dnpc := Mux(io.jump_en, io.alu_pc, snpc)
  pc := dnpc

  io.pc := pc
  io.snpc := snpc

}

/**
 * Generate Verilog sources and save it in file GCD.v
 
object IFUMain extends App {
  ChiselStage.emitSystemVerilogFile(
    new IFU,
    firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info", "-o=../", "--split-verilog")
  )
}*/
