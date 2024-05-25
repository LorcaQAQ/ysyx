TOPNAME= ysyx_23060303_cputop
NXDC_FILES = constr/top.nxdc
INC_PATH ?=
VERILATOR_CFLAGS += -MMD --build -cc \
										-O3 --x-assign fast --x-initial fast --noassert --trace
VERILATOR=verilator
BUILD_DIR=./build
OBJ_DIR = $(BUILD_DIR)/obj_dir
BIN=$(BUILD_DIR)/$(TOPNAME)

default: $(BIN)

$(shell mkdir -p $(BUILD_DIR))

# search v files
VSOURCES=$(shell find $(abspath ./vsrc) -name "*.v")
# testbench cpp files
SIM_SRCS=$(shell find $(abspath ./csrc) -name "*.c" -or -name "*.cc" -or -name "*.cpp")
SIM_SRCS += $(SRC_AUTO_BIND)

# rules for verilator
INCFLAGS = $(addprefix -I, $(INC_PATH))
CXXFLAGS += $(INCFLAGS) -DTOP_NAME="\"V$(TOPNAME)\""

$(BIN): $(VSOURCES) $(SIM_SRCS)
	@rm -rf $(OBJ_DIR)
	$(VERILATOR) $(VERILATOR_CFLAGS) \
		--top-module $(TOPNAME) $^ \
		$(addprefix -CFLAGS , $(CXXFLAGS)) $(addprefix -LDFLAGS , $(LDFLAGS)) \
		--Mdir $(OBJ_DIR) --exe -o $(abspath $(BIN))


all: default
	@echo "Write this Makefile by your self."

sim: $(BIN)
	@$^
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	@echo "Write this Makefile by your self."

clean:
	rm -rf $(BUILD_DIR)
include ../Makefile

.PHONY:default all clean sim


