// Verilated -*- C++ -*-
// DESCRIPTION: Verilator output: Design implementation internals
// See Vadd.h for the primary calling header

#include "verilated.h"
#include "verilated_dpi.h"

#include "Vadd__Syms.h"
#include "Vadd___024root.h"

void Vadd___024root___ctor_var_reset(Vadd___024root* vlSelf);

Vadd___024root::Vadd___024root(Vadd__Syms* symsp, const char* v__name)
    : VerilatedModule{v__name}
    , vlSymsp{symsp}
 {
    // Reset structure values
    Vadd___024root___ctor_var_reset(this);
}

void Vadd___024root::__Vconfigure(bool first) {
    if (false && first) {}  // Prevent unused
}

Vadd___024root::~Vadd___024root() {
}
