// Verilated -*- C++ -*-
// DESCRIPTION: Verilator output: Prototypes for DPI import and export functions.
//
// Verilator includes this file in all generated .cpp files that use DPI functions.
// Manually include this file where DPI .c import functions are declared to ensure
// the C functions match the expectations of the DPI imports.

#ifndef VERILATED_VADD__DPI_H_
#define VERILATED_VADD__DPI_H_  // guard

#include "svdpi.h"

#ifdef __cplusplus
extern "C" {
#endif


    // DPI EXPORTS
    // DPI export at vsrc/add.v:4:6
    extern void publicSetBool(svBit in_bool, svLogic* var_bool);

#ifdef __cplusplus
}
#endif

#endif  // guard
