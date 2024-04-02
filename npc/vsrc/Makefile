TOPNAME=light
NXDC_FILES=constr/top.nxdc
INC_PATH ?=

VERILATOR_CFLAGS +=-MMD --build -cc	\
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
SOURCES=$(shell find $(abspath ./vsrc) -name "*.v")
# testbench cpp files
SIM_SRC=$(shell find $(abspath ./csrc) -name ".c" -or -name ".cc" -or -name "*.cpp)
SIM_SRC +=$(SRC_AUTO_BIND)

#rules for NVBOARD	
include $(NVBOARD_HOME)/scripts/nvboard.mk

# rules for verilator
INCFLAGS = $(addprefix -I, $(INC_PATH))
CXXFLAGS += $(INCFLAGS) -DTOP_NAME="\"V$(TOPNAME)\""

$(BIN):$(SOURCES) $(SIM_SRC) $(NVBOARD_ARCHIVE) 
	@rm -rf $(OBJ_DIR)
	$(VERILATOR) $(VERILATOR_CFLAGS) \
	--top-module $(TOPNAME) $^\
	$(addprefix -CFLAGS , $(CXXFLAGS)) $(addprefix -LDFLAGS , $(LDFLAGS))\
	--Mdir $(OBJ_DIR) --exe -o $(abspath $(BIN))




all:default
	@echo "Write this Makefile by your self."

sim:$(BIN)
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	@echo "Write this Makefile by your self."
	@$^

clean:
	rm -rf $(BUILD_DIR)
.PHONY: default all clean sim
include ../Makefile

