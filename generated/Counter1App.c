/*****************************************
  Emitting C Generated Code                  
*******************************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <math.h>
#include <stdbool.h>

bool x1;
int x2;
bool x30;
int x31;
bool x59;
int x60;
bool x72;
int x73;
bool x98;
int x99;
int x116;
bool x117;
bool x130;
int x131;
int x142;
void x156() {
int x143 = x142;
bool x144 = x143 == 0;
if (x144) {
x116 = 0;
x142 = 1;
} else {
}
x98 = false;
x1 = false;
x30 = false;
x130 = false;
x59 = false;
x72 = false;
};
void x29(uint8_t* x3,int x4) {
int x7 = 0;
int x8 = 0;
int x6 = x4;
uint8_t* x5 = x3;
for (;;) {
int x9 = x8;
bool x10 = x9 < x6;
if (!x10) break;
int x12 = x8;
uint8_t x13 = x5[x12];
int x17 = x7;
int x14 = (int ) x13;
int x15 = x12 * 8;
int x16 = x14 << x15;
int x18 = x17 + x16;
x7 = x18;
int x20 = x12 + 1;
x8 = x20;
}
x1 = true;
int x25 = x7;
int x26 = (int ) x25;
x2 = x26;
};
void x97() {
bool x74 = x1;
bool x75 = x59;
bool x76 = x74 && x75;
if (x76) {
x72 = true;
int x78 = x2;
int x79 = x60;
int x80 = x78 + x79;
x73 = x80;
} else {
if (x74) {
x72 = true;
int x78 = x2;
x73 = x78;
} else {
if (x75) {
x72 = true;
int x79 = x60;
x73 = x79;
} else {
x72 = false;
}
}
}
};
void x115() {
bool x100 = x72;
if (x100) {
int x101 = x73;
int x102 = fabs(x101);
bool x103 = x102 < 10;
if (x103) {
x98 = true;
x99 = x101;
} else {
x98 = false;
}
} else {
x98 = false;
}
};
void x129() {
bool x118 = x98;
if (x118) {
x117 = true;
int x120 = x99;
int x121 = x116;
int x122 = x121 + x120;
x116 = x122;
} else {
x117 = false;
}
};
void x141() {
bool x132 = x117;
if (x132) {
x130 = true;
int x134 = x116;
x131 = x134;
} else {
x130 = false;
}
};
void x176(uint8_t* x169,int x170) {
uint8_t* x171 = x169;
uint8_t x173 = *x171;
printf("%u\n",x173);
};
void x182() {
bool x168 = x130;
if (x168) {
int x177 = x131;
x176((uint8_t*)&x177, sizeof(x177));
} else {
}
};
void x185(uint8_t* x157,int x158) { //top1
x156();
uint8_t* x159 = x157;
int x160 = x158;
x29(x159,x160);
x97();
x115();
x129();
x141();
x182();
};
void x58(uint8_t* x32,int x33) {
int x36 = 0;
int x37 = 0;
int x35 = x33;
uint8_t* x34 = x32;
for (;;) {
int x38 = x37;
bool x39 = x38 < x35;
if (!x39) break;
int x41 = x37;
uint8_t x42 = x34[x41];
int x46 = x36;
int x43 = (int ) x42;
int x44 = x41 * 8;
int x45 = x43 << x44;
int x47 = x46 + x45;
x36 = x47;
int x49 = x41 + 1;
x37 = x49;
}
x30 = true;
int x54 = x36;
int x55 = (int ) x54;
x31 = x55;
};
void x71() {
bool x61 = x30;
if (x61) {
x59 = true;
int x63 = x31;
int x64 = 0 - x63;
x60 = x64;
} else {
x59 = false;
}
};
void x201(uint8_t* x187,int x188) { //top2
x156();
uint8_t* x189 = x187;
int x190 = x188;
x58(x189,x190);
x71();
x97();
x115();
x129();
x141();
x182();
};
/*****************************************
  End of C Generated Code                  
*******************************************/
