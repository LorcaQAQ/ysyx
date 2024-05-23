module ysyx_23060303_cputop(
    input   clk,
    input   rst,
    input   [31:0]  inst,
    output  [31:0]  pc,
    output  [3:0]   nemu_state_stop
);
wire add_en;
wire imm_en;
wire immI;
wire rd_en;
wire [31:0] wdata;
wire [31:0] rdata1;
wire [31:0] rdata2;
wire [31:0] data2;

assign nemu_state_stop=inst==32'b0000000_00001_00000_000_00000_11100_11;

syx_23060303_IFU
#(.PCWIDTH(32)
)IFU_i0
(
    .clk(clk),
    .rst(rst),
    .pc(pc)
);

ysyx_23060303_IDU
IDU_i0(
    .inst(inst),
    .addi(add_en),
    .imm_en(imm_en),
    .imm(imm)
    .rd_en(rd_en)
);

ysyx_23060303_RegisterFile 
Reg_i0(
  .clk(clk),
  .wdata(wdata),
  .waddr(inst[11:7]),
  .raddr1(inst[19:15]),
  .raddr2(inst[24:20]),
  .wen(rd_en),
  .rdata1(rdata1),
  .rdata2(rdata2)
);


 ysyx_23060303_MuxKeyInternal 
 #( .NR_KEY(2), 
    .KEY_LEN(1), 
    .DATA_LEN (32)
 )
 mux_i0 
 (
  .out(data2),
  .key(imm_en),
  .default_out({DATA_LEN{1'b0}}),
  .lut(
    {
        1'b0,rdata2,
        1'b1,imm
    }
  )
  );

  ysyx_23060303_EXU
EXU_i0(
    .addi(add_en),
    .val1(rdata1),
    .val2(data2),
    .result(wdata)
);

endmodule
