// See README.md for license details.

package regfile

import chisel3._
import chisel3.util._
// _root_ disambiguates from package chisel3.util.circt if user imports chisel3.util._
import _root_.circt.stage.ChiselStage


class RegFile(data_width : Int,addr_width : Int) extends Module {
  val io = IO(new Bundle {
    val wdata = Input(UInt(data_width.W))
    val waddr = Input(UInt(addr_width.W))
    val wen = Input(Bool())

    val raddr1 = Input(UInt(addr_width.W))
    val raddr2 = Input(UInt(addr_width.W))
    val rdata1 = Output(UInt(data_width.W))
    val rdata2 = Output(UInt(data_width.W))
  })

  val regfile = Reg(Vec(1 << addr_width,UInt(data_width.W)))

  regfile(io.waddr) := Mux(io.wen, io.wdata, regfile(io.waddr))
  
  io.rdata1 := regfile(io.raddr1)
  io.rdata2 := regfile(io.raddr2)
  regfile(0) := 0.U

}

/**
 * Generate Verilog sources and save it in file GCD.v

object RegFileMain extends App {
  ChiselStage.emitSystemVerilogFile(
    new RegFile(32,5),
    firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info", "-o=../", "--split-verilog")
  )
}
 */