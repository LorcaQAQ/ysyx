
module ebreak(
  input [31:0] instr
);
  import "DPI-C" function void stop_simulation();
  wire nemu_state_stop;
  assign nemu_state_stop=instr==32'b0000000_00001_00000_000_00000_11100_11;

  always @(nemu_state_stop)
  if(nemu_state_stop==1)
   stop_simulation();
endmodule
    
