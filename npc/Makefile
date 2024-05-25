TOPNAME= light
NXDC_FILES = constr/top.nxdc
INC_PATH ?=
VERILATOR_CFLAGS += -MMD --build -cc \
										-O3 --x-assign fast --x-initial fast --noassert
VERILATOR=verilator
BUILD_DIR=./build
OBJ_DIR = $(BUILD_DIR)/obj_dir
BIN=$(BUILD_DIR)/$(TOPNAME)

default: $(BIN)

$(shell mkdir -p $(BUILD_DIR))

# constraint file
SRC_AUTO_BIND = $(abspath $(BUILD_DIR)/auto_bind.cpp)
$(SRC_AUTO_BIND): $(NXDC_FILES)
	python3 $(NVBOARD_HOME)/scripts/auto_pin_bind.py $^ $@

# search v files
VSOURCES=$(shell find $(abspath ./vsrc) -name "*.v")
# testbench cpp files
SIM_SRCS=$(shell find $(abspath ./csrc) -name "*.c" -or -name "*.cc" -or -name "*.cpp")

# rules for NVBoard
include $(NVBOARD_HOME)/scripts/nvboard.mk

# rules for verilator
INCFLAGS = $(addprefix -I, $(INC_PATH))
CXXFLAGS += $(INCFLAGS) -DTOP_NAME="\"V$(TOPNAME)\""

#rule for BIN with respect to command goal
DEPS=$(VSOURCES) $(SIM_SRCS)

ifeq ($(MAKECMDGOALS), run)
  DEPS += $(NVBOARD_ARCHIVE)
  SIM_SRCS += $(SRC_AUTO_BIND)
endif

#to perform simulation, we need to  add "--trace"
ifeq ($(MAKECMDGOALS), sim)
  VERILATOR_CFLAGS +=--trace
endif

$(BIN): $(DEPS)
	@rm -rf $(OBJ_DIR)
	$(VERILATOR) $(VERILATOR_CFLAGS) \
		--top-module $(TOPNAME) $^ \
		$(addprefix -CFLAGS , $(CXXFLAGS)) $(addprefix -LDFLAGS , $(LDFLAGS)) \
		--Mdir $(OBJ_DIR) --exe -o $(abspath $(BIN))

all: default
	@echo "Write this Makefile by your self."

run: $(BIN)
	@$^
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	@echo "Run on nvboard."
	@echo "Write this Makefile by your self."

sim: $(BIN)
	@$^
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	@echo "Simulation in verilator."
	@echo "Write this Makefile by your self."

clean:
	rm -rf $(BUILD_DIR)
include ../Makefile

.PHONY:default all clean sim run


