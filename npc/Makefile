TOPNAME=light
NXDC_FILES=costr/top.nxdc
INC_PATH ?=

VFLAGS=-MMD --build -cc	\
	-O3 --x-assign fast --x-initial fast --noassert
VERILATOR=verilator

BUILD_DIR=./build
OBJ_DIR=$(BUILD_DIR)/Obj_dir
BIN=$(BUILD_DIR)/$(TOPNAME)

default: $(BIN)

$(shell mkdir -p $(BUILD_DIR))

# constraint file
SRC_AUTO_BIND = $(abspath $(BUILD_DIR)/auto_bind.cpp)
$(SRC_AUTO_BIND): $(NXDC_FILES)
	python3 $(NVBOARD_HOME)/scripts/auto_pin_bind.py $^ $@

# search v files
SOURCES=$(wildcard vsrc/*.v)
# testbench cpp files
SIM_SRC=$(wildcard csrc/*main.cpp)
SIM_SRC +=$(SRC_AUTO_BIND)

#rules for NVBOARD	
include $(NVBOARD_HOME)/scripts/nvboard.mk

# rules for verilator
INCFLAGS = $(addprefix -I, $(INC_PATH))
CXXFLAGS += $(INCFLAGS) -DTOP_NAME="\"V$(TOPNAME)\""

all:
	@echo "Write this Makefile by your self."

sim:
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	@echo "Write this Makefile by your self."
	@echo "-- VERILATE & BUILD --------"
	$(VERILATOR) $(VFLAGS) \
	--top-module $(TOPNAME)\
	$(SOURCES) $(SIM_SRC) $(NVBOARD_ARCHIVE) \
	$(addprefix -CFLAGS , $(CXXFLAGS)) $(addprefix -LDFLAGS , $(LDFLAGS))\
	--Mdir $(OBJ_DIR) --exe -o $(abspath $(BIN))
	@echo "-- RUN ---------------------"
	@echo "-- DONE --------------------"

clean:
	rm -rf $(BUILD_DIR)
.PHONY: default all clean sim
include ../Makefile

