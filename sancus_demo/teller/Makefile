#SIMULATE = 1

CC   = sancus-cc
LD   = sancus-ld
SIM  = sancus-sim
LOAD = sancus-loader
RM   = rm -f

# ROM = 32K
# RAM = 24K
ROM = 41K
RAM = 16K

LOAD_DEV  = /dev/ttyUSB0
PRINT_DEV = /dev/ttyUSB1

SANCUS_SUPPORT_BASE = /usr/local/share/sancus-support
LIBS = -L$(SANCUS_SUPPORT_BASE)/lib -ldev -lsm-control -lloader
CFLAGS = -I$(SANCUS_SUPPORT_BASE)/include -w

OBJECTS = TellerApp.o reactive.o buttons.o putchar.o
TARGET  = TellerApp.elf

ifdef SIMULATE
CFLAGS += -DSIMULATE
endif

$(TARGET): $(OBJECTS)
	$(LD) --standalone --rom $(ROM) --ram $(RAM) -o $@ $^ $(LIBS)

run: $(TARGET)
ifdef SIMULATE
	$(SIM) --rom $(ROM) --ram $(RAM) $<
else
	$(LOAD) -device $(LOAD_DEV) -baudrate 115200 $<
endif

view:
	screen $(PRINT_DEV) 115200

clean:
	$(RM) $(TARGET) $(OBJECTS) sancus_sim.fst sim-input.bin sim-output.bin
