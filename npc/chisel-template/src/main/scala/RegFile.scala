// See README.md for license details.

package regfile

import chisel3._
import chisel3.util._
// _root_ disambiguates from package chisel3.util.circt if user imports chisel3.util._
import _root_.circt.stage.ChiselStage

class reg_display(data_width : Int,addr_width : Int) extends BlackBox(Map(
   "DATA_WIDTH" -> data_width, 
   "ADDR_WIDTH" -> addr_width
  ))with HasBlackBoxInline {
  val io = IO(new Bundle {
    val regfile = Input(UInt(((1<<addr_width)*data_width).W))

  })

  setInline("reg_display.v",
    s"""
    |module reg_display
    |#(
    | parameter DATA_WIDTH = 32,
    | parameter ADDR_WIDTH = 5
    |)
    |(
    |  input [(1<<ADDR_WIDTH)*(DATA_WIDTH)-1:0] regfile
    |);
    |
    |function bit [DATA_WIDTH-1:0] get_reg(int index);
    |  if (index >= 0 && index <(1<<ADDR_WIDTH)) begin
    |   get_reg=regfile[DATA_WIDTH*index +: DATA_WIDTH];
    |  end
    |  else begin
    |   get_reg={DATA_WIDTH{1'b0}};
    |  end
    |endfunction
    |export "DPI-C" function get_reg;
    |endmodule
    """.stripMargin)
}


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
  val reg_display = Module(new reg_display(data_width,addr_width))


   reg_display.io.regfile := regfile.asUInt
  
  

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