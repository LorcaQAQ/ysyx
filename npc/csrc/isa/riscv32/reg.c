#include <isa.h>
#include "local-include/reg.h"
#include "svdpi.h"
#include "VCore__Dpi.h"

extern svBitVecVal get_reg(int index);

const char *regs[] = {
	"$0", "ra", "sp", "gp", "tp", "t0", "t1", "t2",
	"s0", "s1", "a0", "a1", "a2", "a3", "a4", "a5",
	"a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7",
	"s8", "s9", "s10", "s11", "t3", "t4", "t5", "t6"
  };

void isa_reg_display() {
	int count=sizeof(regs)/sizeof(regs[0]);
	//set the scope to the register file
	const svScope scope = svGetScopeFromName("TOP.Core.regfile.reg_display");
  	assert(scope);  // Check for nullptr if scope not found
 	svSetScope(scope);
	printf("---Print all the register---\n");
	for(int i=0;i<count;i++){
		printf("$%s=0x%08x\t",regs[i],get_reg(i));
		if((i+1)%4==0){ printf("\n"); }
	}
}

uint32_t isa_reg_str2val(const char *s, bool *success) {
	int count=sizeof(regs)/sizeof(regs[0]);
	int i=0;
	for(i=0;i<count;i++){
		char name[3];
		strncpy(name,s+1,2);
		name[2]='\0';
		if(strcmp(name,regs[i])==0){
			break;
		}
	}
	if(i<count){
		*success=true;
	}
	else{
		*success=false;
	}
	//set the scope to the register file
	const svScope scope = svGetScopeFromName("TOP.Core.regfile.reg_display");
	assert(scope);  // Check for nullptr if scope not found
	svSetScope(scope);
	return get_reg(i);
}
