// See README.md for license details.

package mem

import chisel3._
import chisel3.util._
// _root_ disambiguates from package chisel3.util.circt if user imports chisel3.util._
import _root_.circt.stage.ChiselStage
import instructions.RV32I._
import consts.Consts._
import iobundle._
import axi_io._
import axi_io.AXI_Consts._

class MEM (data_width:Int, delay: Int) extends BlackBox (Map(
   "DATA_WIDTH" -> data_width,
   "DELAY_CYCLE" -> delay
  )) with HasBlackBoxInline {
  val io = IO(new Bundle {
    val clk = Input(Clock())
    val reset =Input(Bool())
    val mem_ren = Input(Bool())
    val mem_rdata = Output(UInt(data_width.W))
    val mem_raddr = Input(UInt(data_width.W))
    val mem_rdata_valid = Output(Bool())

    val mem_wen = Input(Bool())
    val mem_waddr = Input(UInt(data_width.W))
    val mem_wdata = Input(UInt(data_width.W))
    val mem_wstrb = Input(UInt(8.W))
    val mem_write_finished = Output(Bool())

  })
setInline("MEM.v",
    s"""
    |module MEM
    |#(
    | parameter DATA_WIDTH = 32,
    | parameter DELAY_CYCLE = 10
    |)
    |(
    |  input   clk,
    |  input   reset,
    |  input   mem_ren,
    |  input [DATA_WIDTH-1:0] mem_raddr,
    |  output reg [DATA_WIDTH-1:0] mem_rdata,
    |  output reg mem_rdata_valid,
    |
    |  input [7:0] mem_wstrb,
    |  input [DATA_WIDTH-1:0] mem_wdata,
    |  input [DATA_WIDTH-1:0] mem_waddr,
    |  input   mem_wen,
    |  output reg mem_write_finished
    |  
    |);
    |
    |import "DPI-C" function int pmem_read(input int addr);
    |import "DPI-C" function void pmem_write( input int waddr, input int wdata, input byte wmask);
    |
    |reg [3:0] delay_counter;
    |always @(posedge clk) 
    |  if (reset) 
    |   delay_counter <= 0;
    | else if (delay_counter == DELAY_CYCLE) 
    |  delay_counter <= 0;
    | else if(mem_ren || mem_wen) 
    |   delay_counter <= delay_counter + 1;
    | else
        delay_counter <= delay_counter;
    | 
    |     
    |always @(posedge clk) begin
    |  if (mem_ren && delay_counter == DELAY_CYCLE-2) begin // 有读写请求时
    |    mem_rdata <= pmem_read(mem_raddr);
    |    mem_rdata_valid <= 1;
    |  end
    |  else begin
    |    mem_rdata <= 0;
    |    mem_rdata_valid <= 0;
    |  end
    |end
    |
    |always @(posedge clk) begin
    |    if (mem_wen && delay_counter == DELAY_CYCLE) begin // 有写请求时
    |      pmem_write(mem_waddr, mem_wdata, mem_wstrb);
    |      mem_write_finished <= 1;
    |    end
    |   else begin
    |     mem_write_finished <= 0;
    |   end
    |end
    |
    |endmodule
    """.stripMargin)


}

class MEM_SLAVER extends Module{
  val io = IO(new Bundle{
    //MEM Interface
    val mem_ren = Output(Bool())
    val mem_raddr = Output(UInt(32.W))
    val mem_rdata_valid = Input(Bool())
    val mem_rdata = Input(UInt(32.W))
    
    val mem_wen = Output(Bool())
    val mem_waddr = Output(UInt(32.W))
    val mem_wdata = Output(UInt(32.W))
    val mem_wstrb = Output(UInt(8.W))
    val mem_write_finished = Input(Bool())


    //AXI interface
    val aw = Flipped(Decoupled(new axi_mst_write_address))
    val w = Flipped(Decoupled(new axi_mst_write_data))
    val b = Decoupled(new axi_mst_write_response)
    val ar = Flipped(Decoupled(new axi_mst_read_address))
    val r = Decoupled(new axi_mst_read_data)
  })

  
  val ar_ready = RegInit(true.B)
  val ar_valid = io.ar.valid
  val ar_addr_buffer = RegInit(0.U(32.W))
  val ar_port_buffer = RegInit(0.U(3.W))

  val r_valid = RegInit(false.B)
  val r_data = RegInit(0.U(32.W))
  val r_response = RegInit(RRESP_OK)
  val r_ready = io.r.ready
  val r_en = RegInit(false.B)

  val s_idle ::s_wait_rdata::s_send_rdata::Nil = Enum(3)
  val r_state = RegInit(s_idle)
  r_state := MuxLookup(r_state, s_idle)(List(
    s_idle -> Mux(ar_valid && ar_ready, s_wait_rdata, s_idle),
    s_wait_rdata -> Mux(r_valid && r_ready, s_send_rdata, s_wait_rdata),
    s_send_rdata -> s_idle
  ))

  when(r_state === s_idle && ar_valid && ar_ready) {
    ar_ready := false.B
  } .elsewhen(r_state === s_wait_rdata && r_valid && r_ready) {
    ar_ready := true.B
  }

  when(r_state === s_idle && ar_valid && ar_ready) {
    ar_addr_buffer := io.ar.bits.araddr
    ar_port_buffer := io.ar.bits.arport
  }

  when(r_state === s_idle && ar_valid && ar_ready) {
    r_en :=true.B
  }.elsewhen(r_state === s_wait_rdata && r_valid && r_ready) {
    r_en := false.B
  }
  val rdata_valid = WireDefault(false.B)
  rdata_valid := io.mem_rdata_valid

  when(r_state=== s_wait_rdata && rdata_valid) {
    r_data := io.mem_rdata
    r_response := RRESP_OK
    r_valid := true.B
  }.elsewhen(r_state === s_wait_rdata && r_valid && r_ready) {
    r_data := 0.U
    r_valid := false.B
  }

  io.mem_ren := r_en
  io.mem_raddr := ar_addr_buffer
  io.ar.ready := ar_ready
  io.r.valid := r_valid
  io.r.bits.rdata := r_data
  io.r.bits.rresp := r_response

  //-----AXI write part------
  val aw_ready = RegInit(true.B)
  val aw_valid = io.aw.valid
  val aw_addr_buffer = RegInit(0.U(32.W))
  val w_ready = RegInit(true.B)
  val w_valid = io.w.valid
  val w_data_buffer = RegInit(0.U(32.W))
  val w_strb_buffer = RegInit(0.U(4.W))

  val mem_write_finished = io.mem_write_finished
  val mem_wen = RegInit(false.B)

  val s_idle_w :: s_wait_w :: s_send_b :: Nil = Enum(3)
  val aw_state = RegInit(s_idle_w)
  val w_state = RegInit(s_idle_w)
  aw_state := MuxLookup(aw_state, s_idle_w)(List(
    s_idle_w -> Mux(aw_valid && aw_ready, s_wait_w, s_idle_w),
    s_wait_w -> Mux( mem_write_finished , s_send_b, s_wait_w),
    s_send_b -> s_idle_w
  ))
  w_state := MuxLookup(w_state, s_idle_w)(List(
    s_idle_w -> Mux(w_valid && w_ready, s_wait_w, s_idle_w),
    s_wait_w -> Mux( mem_write_finished , s_send_b, s_wait_w),
    s_send_b -> s_idle_w
  ))

  when(aw_state === s_idle_w && aw_ready && aw_valid) {
    aw_addr_buffer := io.aw.bits.awaddr
    aw_ready := false.B
  } .elsewhen (aw_state === s_wait_w && mem_write_finished){
    aw_ready :=true.B
  }

  when(w_state === s_idle_w && w_ready && w_valid) {
    w_data_buffer := io.w.bits.wdata
    w_strb_buffer := io.w.bits.wstrb
    w_ready := false.B
  }.elsewhen (w_state === s_wait_w && mem_write_finished){
    w_ready :=true.B
  }

  when(aw_state === s_idle_w && aw_ready && aw_valid) {
    mem_wen := true.B
  }.elsewhen (aw_state === s_wait_w && mem_write_finished){
    mem_wen := false.B
  }

  io.mem_wen := mem_wen
  io.mem_waddr := aw_addr_buffer
  io.mem_wdata := w_data_buffer
  io.mem_wstrb := w_strb_buffer
  io.aw.ready := aw_ready
  io.w.ready := w_ready

  //AXI write response
  val b_valid = RegInit(false.B)
  val b_ready = io.b.ready
  val b_resp = RegInit(RRESP_OK)

  when(aw_state === s_wait_w && w_state === s_wait_w && mem_write_finished) {
    b_valid := true.B
    b_resp := RRESP_OK
  }.elsewhen(b_valid && b_ready) {
    b_valid := false.B
  }
  io.b.valid := b_valid
  io.b.bits.bresp := b_resp

}

class LSU extends Module {
  val io = IO(new Bundle {
    val in_from_exu = Flipped(Decoupled(new exu_to_mem))
    val out = Decoupled(new mem_to_wbu)

    //AXI interface
    val aw = Decoupled(new axi_mst_write_address)
    val w = Decoupled(new axi_mst_write_data)
    val b = Flipped(Decoupled(new axi_mst_write_response))
    val ar = Decoupled(new axi_mst_read_address)
    val r = Flipped(Decoupled(new axi_mst_read_data))

    // Some additional signals
    val write_err = Output(Bool())
  })
  // val mem = Module(new MEM(32))
  // mem.io.clk := clock

  val ready_to_exu = RegInit(true.B)
  val valid_from_exu = io.in_from_exu.valid

  val valid_to_wbu = RegInit(false.B)
  val s_idle :: s_wait_ready :: Nil = Enum(2)
  val state = RegInit(s_idle)
  state := MuxLookup(state, s_idle)(List(
    s_idle       -> Mux(valid_to_wbu, s_wait_ready, s_idle),
    s_wait_ready -> Mux(io.out.ready, s_idle, s_wait_ready)
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
  
  
  
  // -----AXI part: Address read------
  val ar_valid = RegInit(false.B)
  val ar_ready = WireDefault(false.B)
  val ar_addr = RegInit(0.U(32.W))
  //--Part for read address
  ar_ready := io.ar.ready

  ar_valid := MuxCase(ar_valid, Array(
    (ar_valid === true.B && ar_ready === true.B) -> false.B,
    (io.in_from_exu.valid && ready_to_exu && io.in_from_exu.bits.mem_ren) -> true.B,

  ))

  ar_addr := MuxCase(ar_addr, Array(
    (ar_valid === true.B && ar_ready === true.B) -> 0.U(32.W),
    (ar_valid === true.B && ar_ready === false.B) -> ar_addr,
    (io.in_from_exu.valid && ready_to_exu && io.in_from_exu.bits.mem_ren) -> io.in_from_exu.bits.result,
  ))


  io.ar.valid := ar_valid
  io.ar.bits.araddr := ar_addr
  io.ar.bits.arport := DATA_PORT

  // -----AXI part: Read data------
  val r_valid = io.r.valid
  val r_ready = true.B 
  val r_response = io.r.bits.rresp
  val r_data_valid = WireDefault(false.B)
  r_data_valid := r_valid && r_ready && (r_response === RRESP_OK)
  val r_data = RegInit(0.U(32.W))
  r_data := Mux(r_data_valid, MuxLookup(load_store_range,io.r.bits.rdata)(List(
    (Word) -> io.r.bits.rdata,
    (Half_U) -> Cat(Fill(16, 0.U), io.r.bits.rdata(15, 0)),
    (Half_S) -> Cat(Fill(16, io.r.bits.rdata(15)), io.r.bits.rdata(15, 0)),
    (BYTE_U) -> Cat(Fill(24, 0.U), io.r.bits.rdata(7, 0)),
    (BYTE_S) -> Cat(Fill(24, io.r.bits.rdata(7)), io.r.bits.rdata(7, 0))
  )), r_data)

  io.r.ready := r_ready

  // -----AXI part: Address write------
  val aw_valid = RegInit(false.B)
  val aw_ready = WireDefault(false.B)
  val aw_addr = RegInit(0.U(32.W))
  aw_ready := io.aw.ready

  aw_valid := MuxCase(aw_valid, Array(
    (aw_valid === true.B && aw_ready === true.B) -> false.B,
    (io.in_from_exu.valid && ready_to_exu && io.in_from_exu.bits.mem_wen) -> true.B,
  ))


  aw_addr := MuxCase(aw_addr, Array(
    (aw_valid === true.B && aw_ready === true.B) -> 0.U(32.W),
    (aw_valid === true.B && aw_ready === false.B) -> aw_addr,
    (io.in_from_exu.valid && ready_to_exu && io.in_from_exu.bits.mem_wen) -> io.in_from_exu.bits.result,
  ))
  io.aw.bits.awport := DATA_PORT
  io.aw.bits.awaddr := aw_addr
  io.aw.valid := aw_valid

  // -----AXI part: Write data------
  val w_valid = RegInit(false.B)
  val w_ready = WireDefault(false.B)
  val w_data = RegInit(0.U(32.W))
  val w_strb = RegInit(0.U(4.W))
  w_ready := io.w.ready

  w_valid := MuxCase(w_valid, Array(
    (w_valid === true.B && w_ready === true.B) -> false.B,
    (io.in_from_exu.valid && ready_to_exu && io.in_from_exu.bits.mem_wen) -> true.B,
  ))

  w_data := MuxCase(w_data, Array(
    (w_valid === true.B && w_ready === true.B) -> 0.U(32.W),
    (w_valid === true.B && w_ready === false.B) -> w_data,
    (io.in_from_exu.valid && ready_to_exu && io.in_from_exu.bits.mem_wen) -> io.in_from_exu.bits.rs2
  ))
  val mask = WireDefault(0.U(4.W))
  mask := MuxLookup(io.in_from_exu.bits.load_store_range, "hf".U) (List(
    (Word) -> "hf".U,
    (Half_U) -> "h3".U,   
    (BYTE_U) -> "h1".U
  ))
    
  //   MuxCase(0.U, Array(
  //   (io.in_from_exu.bits.load_store_range === Word) -> "hf".U,
  //   (io.in_from_exu.bits.load_store_range === Half_U) -> "h3".U,
  //   (io.in_from_exu.bits.load_store_range === Half_U) -> "h1".U
  // ))
  w_strb := MuxCase(w_strb, Array(
    (w_valid === true.B && w_ready === true.B) -> 0.U(4.W),
    (w_valid === true.B && w_ready === false.B) -> w_strb,
    (io.in_from_exu.valid && ready_to_exu && io.in_from_exu.bits.mem_wen) -> mask
  ))

  io.w.bits.wdata := w_data
  io.w.bits.wstrb := w_strb
  io.w.valid := w_valid

  // -----AXI part: Write response------
  val b_valid = io.b.valid
  val b_ready = true.B //TODO: should be controlled by the FSM
  val b_response = io.b.bits.bresp

  io.write_err := b_response =/= RRESP_OK

  valid_to_wbu := MuxCase(valid_to_wbu, Array(
    (r_valid && r_ready) -> true.B,
    (b_valid && b_ready) -> true.B,
    ((io.in_from_exu.valid === true.B && ready_to_exu) &&(!io.in_from_exu.bits.mem_wen && !io.in_from_exu.bits.mem_ren)) -> true.B,
    (state === s_wait_ready && io.out.ready) -> false.B
  ))
  io.b.ready := b_ready
  



  //TODO
  ready_to_exu := MuxCase(ready_to_exu, Array(
    (ar_valid === true.B && ar_ready === false.B) -> false.B,
    (aw_valid === true.B && aw_ready === false.B) -> false.B,
    (r_valid === true.B && r_ready === true.B) -> true.B,
    (b_valid === true.B && b_ready === true.B) -> true.B,
    (mem_wen === false.B && mem_ren === false.B) -> true.B
  ))
    
  io.in_from_exu.ready := ready_to_exu
  io.out.valid := valid_to_wbu
  io.out.bits.alu_op := alu_op
  io.out.bits.rf_wdata_sel := rf_wdata_sel
  io.out.bits.rd_addr := rd_addr
  io.out.bits.rf_wen := rf_wen
  io.out.bits.result := result 
  io.out.bits.mem_rdata := r_data
  io.out.bits.snpc := snpc
  io.out.bits.pc := pc
  // io.out.bits.dnpc := dnpc
  io.out.bits.csr_r_w_addr := csr_r_w_addr
  io.out.bits.csr_r_w_ctrl := csr_r_w_ctrl
  io.out.bits.csr_result := csr_result
  io.out.bits.imm := imm
  io.out.bits.jump_op := jump_op

}