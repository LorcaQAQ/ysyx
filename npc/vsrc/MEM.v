
module MEM
#(
 parameter DATA_WIDTH = 32
)
(
  input   clk,
  input   valid,
  input [DATA_WIDTH-1:0] mem_wdata,
  input [DATA_WIDTH-1:0] mem_waddr,
  input   mem_wen,
  input [DATA_WIDTH-1:0] mem_raddr,
  output reg [DATA_WIDTH-1:0] mem_rdata
);

import "DPI-C" function int pmem_read(input int addr);
import "DPI-C" function void pmem_write(
  input int waddr, input int wdata);
always @(valid or mem_wdata or mem_waddr or mem_wen or mem_raddr) begin
  if (valid) begin // 有读写请求时
    mem_rdata = pmem_read(mem_raddr);
    if (mem_wen) begin // 有写请求时
      pmem_write(mem_waddr, mem_wdata);
    end
  end
  else begin
    mem_rdata = 0;
  end
end
endmodule
    
