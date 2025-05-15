

package csr

import chisel3._
import chisel3.util._
// _root_ disambiguates from package chisel3.util.circt if user imports chisel3.util._
import _root_.circt.stage.ChiselStage
import instructions.RV32I._
import consts.Consts._

class csr_display(data_width : Int,csr_num : Int) extends BlackBox(Map(
   "DATA_WIDTH" -> data_width, 
   "CSR_NUM" -> csr_num
  ))with HasBlackBoxInline {
  val io = IO(new Bundle {
    val csrfile = Input(UInt((csr_num*data_width).W))

  })

  setInline("csr_display.v",
    s"""
    |module csr_display
    |#(
    | parameter DATA_WIDTH = 32,
    | parameter CSR_NUM = 4
    |)
    |(
    |  input [CSR_NUM*(DATA_WIDTH)-1:0] csrfile
    |);
    |
    |function bit [DATA_WIDTH-1:0] get_csr(int index);
    |  if (index >= 0 && index < CSR_NUM) begin
    |   get_csr=csrfile[DATA_WIDTH*index +: DATA_WIDTH];
    |  end
    |  else begin
    |   get_csr={DATA_WIDTH{1'b0}};
    |  end
    |endfunction
    |export "DPI-C" function get_csr;
    |endmodule
    """.stripMargin)
}

class CSRs(data_width : Int) extends Module {
  val io = IO(new Bundle {
    val pc =Input(UInt(data_width.W))
    val csr_w_data = Input(UInt(data_width.W))
    val csr_addr = Input(UInt(12.W))
    val csr_r_w_ctrl = Input(UInt(CSR_R_W_CTRL_LEN.W))
    val csr_r_data = Output(UInt(data_width.W))
    val csr_pc = Output(UInt(data_width.W))
  })

  val csr_display =Module(new csr_display(data_width,4))
  
  val csr_file = RegInit(VecInit(
    0x1800.U(data_width.W),  // mstatus (初始值0x1800: MPP=3, MPIE=1)
    0.U(data_width.W),       // mtvec
    0.U(data_width.W),       // mepc
    0.U(data_width.W)        // mcause
  ))

  csr_display.io.csrfile :=csr_file.asUInt

  val addr_map = MuxLookup(io.csr_addr, 0.U(2.W))(
    Seq(
      "h300".U -> 0.U(2.W),
      "h305".U -> 1.U(2.W),
      "h341".U -> 2.U(2.W),
      "h342".U -> 3.U(2.W)
    )
  )
  val csr_wen = (io.csr_r_w_ctrl=== CSR_R_W_EN)

  val mstatus =csr_file(0)
  val mstatus_ecall = (mstatus & 
                      (~(1.U << 7)).asUInt |
                      (mstatus(3)<< 7) &
                      (~(1.U << 3)).asUInt |
                      (0x3.U<<11))

  val mstatus_mret= mstatus &
                   (~(1.U <<3 ).asUInt) |
                   (mstatus(7) <<3) |
                   ((1.U <<7 ).asUInt) &
                   (~(0x3.U << 11).asUInt)

  when(io.csr_r_w_ctrl=== CSR_R_W_EN){
    csr_file(addr_map) := io.csr_w_data
  }.elsewhen(io.csr_r_w_ctrl=== CSR_ECALL){
    csr_file(0) := mstatus_ecall
    csr_file(2) :=io.pc
    csr_file(3) :=11.U
  }.elsewhen(io.csr_r_w_ctrl===CSR_MRET){
    csr_file(0) :=mstatus_mret
  }


  io.csr_r_data := csr_file(addr_map)

  io.csr_pc := MuxCase(0.U, Array(
    (io.csr_r_w_ctrl=== CSR_ECALL) -> csr_file(1),
    (io.csr_r_w_ctrl=== CSR_MRET) -> csr_file(2)
  ))


}
