// Verilated -*- C++ -*-
// DESCRIPTION: Verilator output: Design implementation internals
// See Vadd.h for the primary calling header

#include "verilated.h"
#include "verilated_dpi.h"

#include "Vadd___024root.h"

VL_ATTR_COLD void Vadd___024root___eval_static(Vadd___024root* vlSelf) {
    if (false && vlSelf) {}  // Prevent unused
    Vadd__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vadd___024root___eval_static\n"); );
}

VL_ATTR_COLD void Vadd___024root___eval_initial(Vadd___024root* vlSelf) {
    if (false && vlSelf) {}  // Prevent unused
    Vadd__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vadd___024root___eval_initial\n"); );
}

VL_ATTR_COLD void Vadd___024root___eval_final(Vadd___024root* vlSelf) {
    if (false && vlSelf) {}  // Prevent unused
    Vadd__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vadd___024root___eval_final\n"); );
}

VL_ATTR_COLD void Vadd___024root___eval_settle(Vadd___024root* vlSelf) {
    if (false && vlSelf) {}  // Prevent unused
    Vadd__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vadd___024root___eval_settle\n"); );
}

#ifdef VL_DEBUG
VL_ATTR_COLD void Vadd___024root___dump_triggers__act(Vadd___024root* vlSelf) {
    if (false && vlSelf) {}  // Prevent unused
    Vadd__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vadd___024root___dump_triggers__act\n"); );
    // Body
    if ((1U & (~ (IData)(vlSelf->__VactTriggered.any())))) {
        VL_DBG_MSGF("         No triggers active\n");
    }
}
#endif  // VL_DEBUG

#ifdef VL_DEBUG
VL_ATTR_COLD void Vadd___024root___dump_triggers__nba(Vadd___024root* vlSelf) {
    if (false && vlSelf) {}  // Prevent unused
    Vadd__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vadd___024root___dump_triggers__nba\n"); );
    // Body
    if ((1U & (~ (IData)(vlSelf->__VnbaTriggered.any())))) {
        VL_DBG_MSGF("         No triggers active\n");
    }
}
#endif  // VL_DEBUG

VL_ATTR_COLD void Vadd___024root___ctor_var_reset(Vadd___024root* vlSelf) {
    if (false && vlSelf) {}  // Prevent unused
    Vadd__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vadd___024root___ctor_var_reset\n"); );
}
