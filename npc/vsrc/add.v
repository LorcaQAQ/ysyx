module add();
export "DPI-C" task publicSetBool;

task publicSetBool;
   input bit in_bool;
   var_bool = in_bool;
endtask

endmodule
