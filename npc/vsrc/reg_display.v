
module reg_display
#(
 parameter DATA_WIDTH = 32,
 parameter ADDR_WIDTH = 5
)
(
  input [(1<<ADDR_WIDTH)*(DATA_WIDTH)-1:0] regfile
);

function bit [DATA_WIDTH-1:0] get_reg(int index);
  if (index >= 0 && index <(1<<ADDR_WIDTH)) begin
   get_reg=regfile[DATA_WIDTH*index +: DATA_WIDTH];
  end
  else begin
   get_reg={DATA_WIDTH{1'b0}};
  end
endfunction
export "DPI-C" function get_reg;
endmodule
    
