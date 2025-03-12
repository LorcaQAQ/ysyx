
module instr_fetch(
  input [31:0] pc,
  input reset,
  output reg [31:0]  instr
);
import "DPI-C" function int pmem_read(input int addr); 

always @(pc or reset) begin
  if(reset) begin
    instr = 0;
  end
  else begin
   instr = pmem_read(pc);
  end
end
endmodule
    
