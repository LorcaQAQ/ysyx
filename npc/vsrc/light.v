module light(
		input clk,
		input rst,
  output reg [15:0] led
);
reg [31:0] count;
always @(posedge clk)
	if(rst)
	begin
		led<=16'd1;
		count<=32'd0;
	end
	else
	begin
		if(count==0) led<={led[14:0],led[15]};
		count<=(count==(32'd5000000)?32'b0:count+1'd1);
	end

endmodule;



