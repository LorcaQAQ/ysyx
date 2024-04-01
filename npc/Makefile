VFLAGS=--cc --exe --build -j 0 -Wall $(OPTFLAGS)
VERILATOR=verilator
# search v files
SOURCES=$(wildcard vsrc/*.v)
# testbench cpp files
SIM_SRC=$(wildcard csrc/*main.cpp)

#create top module
TOP=$(patsubst %.v,%,$(SOURCES))
VTOP=$(patsubst vsrc/%,V%,$(TOP))
all:
	@echo "Write this Makefile by your self."

sim:
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	@echo "Write this Makefile by your self."
	@echo "-- VERILATE & BUILD --------"
	$(VERILATOR) $(VFLAGS) $(SOURCES) $(SIM_SRC)
	@echo "-- RUN ---------------------"
	obj_dir/$(VTOP)
	@echo "-- DONE --------------------"

clean:
	rm -rf obj_dir
include ../Makefile
