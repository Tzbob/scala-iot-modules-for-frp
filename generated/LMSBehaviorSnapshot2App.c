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
bool x73;
int x74;
bool x85;
int x86;
bool x97;
int x98;
bool x109;
int x110;
bool x121;
int x122;
bool x135;
int x136;
int x147;
void x156() {
int x148 = x147;
bool x149 = x148 == 0;
if (x149) {
x60 = 0;
x122 = 0;
x147 = 1;
} else {
}
};
void x167() {
x85 = false;
x1 = false;
x97 = false;
x135 = false;
x109 = false;
x30 = false;
x73 = false;
x59 = false;
x121 = false;
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
void x72() {
bool x61 = x1;
if (x61) {
x59 = true;
int x63 = x2;
int x64 = x60;
int x65 = x63 + x64;
x60 = x65;
} else {
x59 = false;
}
};
void x178(uint8_t* x168,int x169) { //top1
x156();
x167();
uint8_t* x170 = x168;
int x171 = x169;
x29(x170,x171);
x72();
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
void x84() {
bool x75 = x30;
if (x75) {
x73 = true;
int x77 = x31;
x74 = 1;
} else {
x73 = false;
}
};
void x96() {
bool x87 = x73;
if (x87) {
x85 = true;
int x89 = x74;
x86 = 2;
} else {
x85 = false;
}
};
void x108() {
bool x99 = x85;
if (x99) {
x97 = true;
int x101 = x86;
x98 = 3;
} else {
x97 = false;
}
};
void x120() {
bool x111 = x97;
if (x111) {
x109 = true;
int x113 = x60;
x110 = x113;
} else {
x109 = false;
}
};
void x134() {
bool x123 = x109;
if (x123) {
x121 = true;
int x125 = x110;
int x126 = x122;
int x127 = x125 + x126;
x122 = x127;
} else {
x121 = false;
}
};
void x146() {
bool x137 = x121;
if (x137) {
x135 = true;
int x139 = x122;
x136 = x139;
} else {
x135 = false;
}
};
void x202(uint8_t* x195,int x196) {
uint8_t* x197 = x195;
uint8_t x199 = *x197;
printf("%u\n",x199);
};
void x208() {
bool x194 = x135;
if (x194) {
int x203 = x136;
x202((uint8_t*)&x203, sizeof(x203));
} else {
}
};
void x211(uint8_t* x180,int x181) { //top3
x156();
x167();
uint8_t* x182 = x180;
int x183 = x181;
x58(x182,x183);
x84();
x96();
x108();
x120();
x134();
x146();
x208();
};
/*****************************************
  End of C Generated Code                  
*******************************************/
