module ysyx_23060303_IFU
#(PCWIDTH=32)
(
    input           clk,
    input           rst,
    output  [PCWIDTH-1:0]  pc
);

wire [PCWIDTH-1:0]snpc;
//assgin static next "PC" value
assign snpc=pc+4;

ysyx_23060303_Reg  
#(  .WIDTH(32), 
    .RESET_VAL(32'h8000_0000)
)Reg_i0 
(
  .clk(clk),
  .rst(rst),
  .din(snpc),
  .dout(pc),
  .wen(1'b1)
);
endmodule