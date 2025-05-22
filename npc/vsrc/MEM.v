
module MEM
#(
 parameter DATA_WIDTH = 32
)
(
  input   clk,
  input   mem_ren,
  input [DATA_WIDTH-1:0] mem_wdata,
  input [DATA_WIDTH-1:0] mem_waddr,
  input   mem_wen,
  input [DATA_WIDTH-1:0] mem_raddr,
  input [7:0] mem_wmask,
  output reg [DATA_WIDTH-1:0] mem_rdata
);

import "DPI-C" function int pmem_read(input int addr);
import "DPI-C" function void pmem_write( input int waddr, input int wdata, input byte wmask);
always @(mem_ren or mem_wdata or mem_waddr or mem_wen or mem_raddr) begin
  if (mem_ren) begin // 有读写请求时
    mem_rdata = pmem_read(mem_raddr);
  end
  else begin
    mem_rdata = 0;
  end
end

always @( mem_wdata or mem_waddr or mem_wen) begin
    if (mem_wen) begin // 有写请求时
      pmem_write(mem_waddr, mem_wdata, mem_wmask);
    end
end

endmodule
    
