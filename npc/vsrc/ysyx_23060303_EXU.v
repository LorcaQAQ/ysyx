module ysyx_23060303_EXU
#(
    WIDTH=32
)(
    input   addi,
    input   [WIDTH-1:0]     val1,
    input   [WIDTH-1:0]     val2,
    output reg  [WIDTH-1:0]     result
);
always @(*)
if(addi==1'b1)
    result=val1+val2;
else
    result=WIDTH'd0;
    
endmodule