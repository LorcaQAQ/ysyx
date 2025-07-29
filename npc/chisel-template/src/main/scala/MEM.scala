// See README.md for license details.

package mem

import chisel3._
import chisel3.util._
// _root_ disambiguates from package chisel3.util.circt if user imports chisel3.util._
import _root_.circt.stage.ChiselStage
import instructions.RV32I._
import consts.Consts._
import iobundle._

class MEM (data_width:Int) extends BlackBox (Map(
   "DATA_WIDTH" -> data_width
  )) with HasBlackBoxInline {
  val io = IO(new Bundle {
    val clk = Input(Clock())
    val mem_ren = Input(Bool())
    val mem_wen = Input(Bool())
    val mem_waddr = Input(UInt(data_width.W))
    val mem_wdata = Input(UInt(data_width.W))
    val mem_rdata = Output(UInt(data_width.W))
    val mem_raddr = Input(UInt(data_width.W))
    val mem_wmask = Input(UInt(8.W))

  })
setInline("MEM.v",
    s"""
    |module MEM
    |#(
    | parameter DATA_WIDTH = 32
    |)
    |(
    |  input   clk,
    |  input   mem_ren,
    |  input [DATA_WIDTH-1:0] mem_wdata,
    |  input [DATA_WIDTH-1:0] mem_waddr,
    |  input   mem_wen,
    |  input [DATA_WIDTH-1:0] mem_raddr,
    |  input [7:0] mem_wmask,
    |  output reg [DATA_WIDTH-1:0] mem_rdata
    |);
    |
    |import "DPI-C" function int pmem_read(input int addr);
    |import "DPI-C" function void pmem_write( input int waddr, input int wdata, input byte wmask);
    |always @(mem_ren or mem_wdata or mem_waddr or mem_wen or mem_raddr) begin
    |  if (mem_ren) begin // 有读写请求时
    |    mem_rdata = pmem_read(mem_raddr);
    |  end
    |  else begin
    |    mem_rdata = 0;
    |  end
    |end
    |
    |always @( mem_wdata or mem_waddr or mem_wen) begin
    |    if (mem_wen) begin // 有写请求时
    |      pmem_write(mem_waddr, mem_wdata, mem_wmask);
    |    end
    |end
    |
    |endmodule
    """.stripMargin)


}

class mem_wrapper extends Module {
  val io = IO(new Bundle {
    val in_from_exu = Flipped(Decoupled(new exu_to_mem))
    val out = Decoupled(new mem_to_wbu)
  })
  val mem = Module(new MEM(32))
  mem.io.clk := clock

  io.in_from_exu.ready := true.B
  val valid_to_wbu = RegInit(false.B)
  val s_idle :: s_wait_ready :: Nil = Enum(2)
  val state = RegInit(s_idle)
  state := MuxLookup(state, s_idle)(List(
    s_idle       -> Mux(valid_to_wbu, s_wait_ready, s_idle),
    s_wait_ready -> Mux(io.out.ready, s_idle, s_wait_ready)
  ))
  valid_to_wbu := MuxCase(0.U, Array(
    (io.in_from_exu.valid === true.B) -> true.B,
    (state === s_wait_ready && io.out.ready) -> false.B
  ))

  val result = RegInit(0.U(32.W))
 
  val load_store_range = RegInit(0.U(LOAD_LEN.W))
  val mem_wen = RegInit(false.B)
  val mem_ren = RegInit(false.B)
  val rf_wen = RegInit(false.B)
  val rf_wdata_sel = RegInit(0.U(REG_DATA_LEN.W))
  val rd_addr = RegInit(0.U(5.W))
  val jump_op = RegInit(0.U(JUMP_LEN.W))
  val alu_op = RegInit(0.U(ALU_LEN.W))
  val snpc = RegInit(0.U(32.W))
  val pc = RegInit(0.U(32.W))
  // val dnpc = RegInit(0.U(32.W))
  val imm =RegInit(0.U(32.W))
  val rs2 = RegInit(0.U(32.W))
  val csr_result = RegInit(0.U(32.W))
  val csr_r_w_addr = RegInit(0.U(12.W))
  val csr_r_w_ctrl = RegInit(0.U(CSR_R_W_CTRL_LEN.W))

  alu_op :=Mux(io.in_from_exu.valid && io.in_from_exu.ready, io.in_from_exu.bits.alu_op, alu_op)
  rf_wen := Mux(io.in_from_exu.valid && io.in_from_exu.ready, io.in_from_exu.bits.rf_wen, rf_wen)
  rf_wdata_sel := Mux(io.in_from_exu.valid && io.in_from_exu.ready, io.in_from_exu.bits.rf_wdata_sel, rf_wdata_sel)
  rd_addr := Mux(io.in_from_exu.valid && io.in_from_exu.ready, io.in_from_exu.bits.rd_addr, rd_addr)
  result := Mux(io.in_from_exu.valid && io.in_from_exu.ready, io.in_from_exu.bits.result, result)
  mem_wen := Mux(io.in_from_exu.valid && io.in_from_exu.ready, io.in_from_exu.bits.mem_wen, mem_wen)
  mem_ren := Mux(io.in_from_exu.valid && io.in_from_exu.ready, io.in_from_exu.bits.mem_ren, mem_ren)
  load_store_range := Mux(io.in_from_exu.valid && io.in_from_exu.ready, io.in_from_exu.bits.load_store_range, load_store_range)
  snpc := Mux(io.in_from_exu.valid && io.in_from_exu.ready, io.in_from_exu.bits.snpc, snpc)
  pc := Mux(io.in_from_exu.valid && io.in_from_exu.ready, io.in_from_exu.bits.pc, pc)
  // dnpc := Mux(io.in_from_exu.valid && io.in_from_exu.ready, io.in_from_exu.bits.dnpc, dnpc)
  imm := Mux(io.in_from_exu.valid && io.in_from_exu.ready, io.in_from_exu.bits.imm, imm)
  jump_op := Mux(io.in_from_exu.valid && io.in_from_exu.ready, io.in_from_exu.bits.jump_op, jump_op)
  rs2 := Mux(io.in_from_exu.valid && io.in_from_exu.ready, io.in_from_exu.bits.rs2, rs2)
  csr_result := Mux(io.in_from_exu.valid && io.in_from_exu.ready, io.in_from_exu.bits.csr_result, csr_result)
  csr_r_w_addr := Mux(io.in_from_exu.valid && io.in_from_exu.ready, io.in_from_exu.bits.csr_r_w_addr, csr_r_w_addr)
  csr_r_w_ctrl := Mux(io.in_from_exu.valid && io.in_from_exu.ready, io.in_from_exu.bits.csr_r_w_ctrl, csr_r_w_ctrl)
  
  val mem_wdata = Wire(UInt(32.W))
  val mem_rdata = Wire(UInt(32.W))
    mem_wdata := MuxCase(0.U, Array(
      (load_store_range === Word) -> rs2,
      (load_store_range === Half_U) -> Cat(Fill(16,0.U),rs2(15,0)),
      (load_store_range === BYTE_U) -> Cat(Fill(24,0.U),rs2(7,0))
    ))
    mem.io.mem_ren := mem_ren
    mem.io.mem_wen := mem_wen
    mem.io.mem_waddr := result
    mem.io.mem_wdata := mem_wdata
    mem.io.mem_raddr := result
    mem.io.mem_wmask := MuxCase(0.U, Array(
      (load_store_range === Word) -> "h0f".U,
      (load_store_range === Half_U) -> "h03".U,
      (load_store_range === BYTE_U) -> "h01".U
    ))

    mem_rdata := MuxCase(0.U, Array(
      (load_store_range === Word) ->  mem.io.mem_rdata,
      (load_store_range === BYTE_U) ->  Cat(Fill(24,0.U),mem.io.mem_rdata(7,0)),
      (load_store_range === Half_U) ->  Cat(Fill(16,0.U),mem.io.mem_rdata(15,0)),
      (load_store_range === Half_S) ->  Cat(Fill(16,mem.io.mem_rdata(15)),mem.io.mem_rdata(15,0)),
      (load_store_range === BYTE_S) ->  Cat(Fill(24,mem.io.mem_rdata(7)),mem.io.mem_rdata(7,0))
    ))  
    

  io.out.valid := valid_to_wbu
  io.out.bits.alu_op := alu_op
  io.out.bits.rf_wdata_sel := rf_wdata_sel
  io.out.bits.rd_addr := rd_addr
  io.out.bits.rf_wen := rf_wen
  io.out.bits.result := result 
  io.out.bits.mem_rdata := mem_rdata
  io.out.bits.snpc := snpc
  io.out.bits.pc := pc
  // io.out.bits.dnpc := dnpc
  io.out.bits.csr_r_w_addr := csr_r_w_addr
  io.out.bits.csr_r_w_ctrl := csr_r_w_ctrl
  io.out.bits.csr_result := csr_result
  io.out.bits.imm := imm
  io.out.bits.jump_op := jump_op

}