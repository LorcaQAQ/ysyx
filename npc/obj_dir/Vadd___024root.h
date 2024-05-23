// Verilated -*- C++ -*-
// DESCRIPTION: Verilator output: Design internal header
// See Vadd.h for the primary calling header

#ifndef VERILATED_VADD___024ROOT_H_
#define VERILATED_VADD___024ROOT_H_  // guard

#include "verilated.h"

class Vadd__Syms;

class Vadd___024root final : public VerilatedModule {
  public:

    // DESIGN SPECIFIC STATE
    CData/*0:0*/ __VactContinue;
    IData/*31:0*/ __VactIterCount;
    VlTriggerVec<0> __VactTriggered;
    VlTriggerVec<0> __VnbaTriggered;

    // INTERNAL VARIABLES
    Vadd__Syms* const vlSymsp;

    // CONSTRUCTORS
    Vadd___024root(Vadd__Syms* symsp, const char* v__name);
    ~Vadd___024root();
    VL_UNCOPYABLE(Vadd___024root);

    // INTERNAL METHODS
    void __Vconfigure(bool first);
} VL_ATTR_ALIGNED(VL_CACHE_LINE_BYTES);


#endif  // guard
