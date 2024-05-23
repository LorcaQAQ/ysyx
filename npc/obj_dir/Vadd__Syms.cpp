// Verilated -*- C++ -*-
// DESCRIPTION: Verilator output: Symbol table implementation internals

#include "Vadd__Syms.h"
#include "Vadd.h"
#include "Vadd___024root.h"

void Vadd___024root____Vdpiexp_add__DOT__publicSetBool_TOP(Vadd__Syms* __restrict vlSymsp, CData/*0:0*/ in_bool, CData/*0:0*/ &var_bool);

// FUNCTIONS
Vadd__Syms::~Vadd__Syms()
{
}

Vadd__Syms::Vadd__Syms(VerilatedContext* contextp, const char* namep, Vadd* modelp)
    : VerilatedSyms{contextp}
    // Setup internal state of the Syms class
    , __Vm_modelp{modelp}
    // Setup module instances
    , TOP{this, namep}
{
    // Configure time unit / time precision
    _vm_contextp__->timeunit(-12);
    _vm_contextp__->timeprecision(-12);
    // Setup each module's pointers to their submodules
    // Setup each module's pointer back to symbol table (for public functions)
    TOP.__Vconfigure(true);
    // Setup scopes
    __Vscope_add.configure(this, name(), "add", "add", -12, VerilatedScope::SCOPE_OTHER);
    // Setup export functions
    for (int __Vfinal = 0; __Vfinal < 2; ++__Vfinal) {
        __Vscope_add.exportInsert(__Vfinal, "publicSetBool", (void*)(&Vadd___024root____Vdpiexp_add__DOT__publicSetBool_TOP));
    }
}
