// Verilated -*- C++ -*-
// DESCRIPTION: Verilator output: Implementation of DPI export functions.
//
#include "Vadd.h"
#include "Vadd__Syms.h"
#include "verilated_dpi.h"


void Vadd::publicSetBool(svBit in_bool, svLogic* var_bool) {
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vadd___024root::publicSetBool\n"); );
    // Init
    CData/*0:0*/ in_bool__Vcvt;
    in_bool__Vcvt = 0;
    CData/*0:0*/ var_bool__Vcvt;
    var_bool__Vcvt = 0;
    // Body
    static int __Vfuncnum = -1;
    if (VL_UNLIKELY(__Vfuncnum == -1)) __Vfuncnum = Verilated::exportFuncNum("publicSetBool");
    const VerilatedScope* __Vscopep = Verilated::dpiScope();
    Vadd__Vcb_publicSetBool_t __Vcb = (Vadd__Vcb_publicSetBool_t)(VerilatedScope::exportFind(__Vscopep, __Vfuncnum));
    in_bool__Vcvt = (1U & in_bool);
    (*__Vcb)((Vadd__Syms*)(__Vscopep->symsp()), in_bool__Vcvt, var_bool__Vcvt);
    for (size_t var_bool__Vidx = 0; var_bool__Vidx < 1; ++var_bool__Vidx) *var_bool = var_bool__Vcvt;
}
