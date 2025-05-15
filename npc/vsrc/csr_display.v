
module csr_display
#(
 parameter DATA_WIDTH = 32,
 parameter CSR_NUM = 4
)
(
  input [CSR_NUM*(DATA_WIDTH)-1:0] csrfile
);

function bit [DATA_WIDTH-1:0] get_csr(int index);
  if (index >= 0 && index < CSR_NUM) begin
   get_csr=csrfile[DATA_WIDTH*index +: DATA_WIDTH];
  end
  else begin
   get_csr={DATA_WIDTH{1'b0}};
  end
endfunction
export "DPI-C" function get_csr;
endmodule
    
