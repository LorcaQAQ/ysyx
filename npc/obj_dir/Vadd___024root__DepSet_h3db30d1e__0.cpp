// Verilated -*- C++ -*-
// DESCRIPTION: Verilator output: Design implementation internals
// See Vadd.h for the primary calling header

#include "verilated.h"
#include "verilated_dpi.h"

#include "Vadd__Syms.h"
#include "Vadd___024root.h"

void Vadd___024root____Vdpiexp_add__DOT__publicSetBool_TOP(Vadd__Syms* __restrict vlSymsp, CData/*0:0*/ in_bool, CData/*0:0*/ &var_bool) {
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vadd___024root____Vdpiexp_add__DOT__publicSetBool_TOP\n"); );
    // Init
    // Body
    var_bool = in_bool;
}

#ifdef VL_DEBUG
VL_ATTR_COLD void Vadd___024root___dump_triggers__act(Vadd___024root* vlSelf);
#endif  // VL_DEBUG

void Vadd___024root___eval_triggers__act(Vadd___024root* vlSelf) {
    if (false && vlSelf) {}  // Prevent unused
    Vadd__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vadd___024root___eval_triggers__act\n"); );
    // Body
#ifdef VL_DEBUG
    if (VL_UNLIKELY(vlSymsp->_vm_contextp__->debug())) {
        Vadd___024root___dump_triggers__act(vlSelf);
    }
#endif
}
