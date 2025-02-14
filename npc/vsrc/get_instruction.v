
module get_instruction(
  input [31:0] instr
);
  function bit [31:0] get_instr;
     get_instr=instr;
  endfunction
  export "DPI-C" function get_instr;
endmodule
    
