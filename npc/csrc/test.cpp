#include "svdpi.h"
#include "Vadd__Dpi.h"
#include "Vadd.h"
#include "verilated.h"
extern void publicSetBool(svBit in_bool);

int main(int argc, char** argv) {
    VerilatedContext* contextp = new VerilatedContext;
    contextp->commandArgs(argc, argv);
    Vadd* top = new Vadd{contextp};
    
    int val=1;
    void *var_bool;
    while (!contextp->gotFinish()) 
    { 
    	top->eval(); 
    	var_bool=publicSetBool(val);
    	printf("%d",&var_bool);
    }
    delete top;
    delete contextp;
    return 0;
}
int add(int a, int b) { return a+b; }
