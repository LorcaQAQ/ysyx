module ysyx_23060303_RegisterFile #(ysyx_23060303_ADDR_WIDTH = 5, ysyx_23060303_DATA_WIDTH = 32) (
  input clk,
  input [ysyx_23060303_DATA_WIDTH-1:0]    wdata,
  input [ysyx_23060303_ADDR_WIDTH-1:0]    waddr,
  input [ysyx_23060303_ADDR_WIDTH-1:0]    raddr1,
  input [ysyx_23060303_ADDR_WIDTH-1:0]    raddr2,
  //input rs1_en,
  //input rs2_en,
  input wen,
  output [ysyx_23060303_DATA_WIDTH-1:0]   rdata1,
  output [ysyx_23060303_DATA_WIDTH-1:0]   rdata2
);
reg [ysyx_23060303_DATA_WIDTH-1:0] rf [2**ysyx_23060303_ADDR_WIDTH-1:0];

always @(posedge clk) begin
    if (wen) rf[waddr] <= wdata;
end
//register[0]=0
//assign rf[0]=32'b0;
always @(*)
  rf[0]=32'd0;

assign rdata1=rf[raddr1];
assign rdata2=rf[raddr2];

endmodule