VFLAGS=-cc --exe --build -j $(OPTFLAGS)

SOURCES=$(wildcard vsrc/*.v)
SIM_SRC=$(wildcard csrc/*_main.cpp)
all:
	@echo "Write this Makefile by your self."

sim:
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	@echo "Write this Makefile by your self."
	@echo "-- VERILATE & BUILD --------"
	$(VERILATOR) $(VFLAGS) $(SOURCES) $(SIM_SRC)

include ../Makefile
