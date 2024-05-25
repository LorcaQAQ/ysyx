// Verilated -*- C++ -*-
// DESCRIPTION: Verilator output: Symbol table internal header
//
// Internal details; most calling programs do not need this header,
// unless using verilator public meta comments.

#ifndef VERILATED_VADD__SYMS_H_
#define VERILATED_VADD__SYMS_H_  // guard

#include "verilated.h"

// INCLUDE MODEL CLASS

#include "Vadd.h"

// INCLUDE MODULE CLASSES
#include "Vadd___024root.h"

// DPI TYPES for DPI Export callbacks (Internal use)
using Vadd__Vcb_publicSetBool_t = void (*) (Vadd__Syms* __restrict vlSymsp, CData/*0:0*/ in_bool, CData/*0:0*/ &var_bool);

// SYMS CLASS (contains all model state)
class Vadd__Syms final : public VerilatedSyms {
  public:
    // INTERNAL STATE
    Vadd* const __Vm_modelp;
    VlDeleter __Vm_deleter;
    bool __Vm_didInit = false;

    // MODULE INSTANCE STATE
    Vadd___024root                 TOP;

    // SCOPE NAMES
    VerilatedScope __Vscope_add;

    // CONSTRUCTORS
    Vadd__Syms(VerilatedContext* contextp, const char* namep, Vadd* modelp);
    ~Vadd__Syms();

    // METHODS
    const char* name() { return TOP.name(); }
} VL_ATTR_ALIGNED(VL_CACHE_LINE_BYTES);

#endif  // guard
