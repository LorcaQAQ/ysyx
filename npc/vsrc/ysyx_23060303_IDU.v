module ysyx_23060303_IDU(
    input   [31:0]   inst,
    output  [31:0]  imm,
    output          addi,
    output          imm_en,
    output          rd_en
);

//immI
wire immI_en;
wire [31:0] immI;
assign immI_en=~inst[6]&~inst[5]&inst[4]&~inst[3]&~inst[2]&inst[1]&inst[0];//opcode=0010011
assign immI={{20{inst[31]}},inst[31:20]};

//immR
wire immR_en;
//immB
wire immB_en;
//immU
wire immU_en;
//immS
wire immS_en;
//immJ
wire immJ_en;

//Imm branch
wire [5:0] imm_branch;
assign imm_branch={immR_en,immI_en,immS_en,immB_en,immU_en,immJ_en};

ysyx_23060303_MuxKeyInternal 
#(  .NR_KEY (1), 
    .KEY_LEN (6), 
    .DATA_LEN  (32), 
    .HAS_DEFAULT (1)
) 
Mux_imm_i0
(
  .out(imm),
  .key(imm_branch),
  .default_out(32'b0),
  .lut(
    {
        6'b01_0000,immI
    }
  )
);


//addi
assign addi=immI_en&~inst[14]&~inst[13]&~inst[12];

//immediate enable
assign imm_en=immR_en|immI_en|immS_en|immB_en|immU_en|immJ_en;

//destination register enable
assign rd_en=immR_en|immI_en|~immS_en|~immB_en|immU_en|immJ_en;

endmodule
