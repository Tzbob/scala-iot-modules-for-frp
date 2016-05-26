/*****************************************
  Emitting C Generated Code                  
*******************************************/

#include <sancus/sm_support.h>

#include <sancus_support/uart.h>
#include <sancus_support/pmodcls.h>
#include <sancus_support/tsc.h>
#include <sancus_support/sm_control.h>

#include <msp430.h>

#include "reactive.h"
#include "buttons.h"
#include "string.h"
#include <stdbool.h>

static int lcd_printf(const char* fmt, ...)
{
    va_list va;
    va_start(va, fmt);
    int result = vuprintf(pmodcls_putchar, fmt, va);
    va_end(va);
  return result;
}

static void __attribute__((noinline)) lcd_clear()
{
  lcd_printf("%s","                                ");
}static void __attribute__((noinline)) lcd_printf_int(const char* fmt, int i)
{
    lcd_printf(fmt, i);
}

static void __attribute__((noinline)) lcd_printf_string(char* s)
{
    lcd_printf("%s", s);
}

static void __attribute__((noinline)) printf_int(const char* fmt, int i)
{
  printf(fmt, i);
}

SM_DATA(mod1) int x166;
SM_DATA(mod1) int x183;
SM_FUNC(mod1) void x182 () {
lcd_clear();
int x179 = x166;
pmodcls_set_cursor_position(0,0);
lcd_printf_int("Counter: %d", x179);
}
SM_FUNC(mod1) void x192 () {
int x184 = x183;
bool x185 = x184 == 0;
if (x185) {
x166 = 0;
x182();
x183 = 1;
} else {
}
}
SM_FUNC(mod1) void x31 (uint8_t* x1,int x2,int* x3,bool* x4) {
int x9 = 0;
int x10 = 0;
int x6 = x2;
uint8_t* x5 = x1;
for (;;) {
int x11 = x10;
bool x12 = x11 < x6;
if (!x12) break;
int x14 = x10;
uint8_t x15 = x5[x14];
int x19 = x9;
int x16 = (int ) x15;
int x17 = x14 * 8;
int x18 = x16 << x17;
int x20 = x19 + x18;
x9 = x20;
int x22 = x14 + 1;
x10 = x22;
}
bool* x8 = x4;
*x8 = true;
int x27 = x9;
int* x7 = x3;
int x28 = (int ) x27;
*x7 = x28;
}
SM_FUNC(mod1) void x78 (int x63,bool x64,int* x65,bool* x66) {
bool x68 = x64;
if (x68) {
bool* x70 = x66;
*x70 = true;
int* x69 = x65;
*x69 = 5;
} else {
bool* x70 = x66;
*x70 = false;
}
}
SM_FUNC(mod1) void x143 (int x112,bool x113,int x114,bool x115,int* x116,bool* x117) {
bool x119 = x113;
bool x121 = x115;
bool x124 = x119 && x121;
if (x124) {
bool* x123 = x117;
*x123 = true;
int* x122 = x116;
int x118 = x112;
int x120 = x114;
int x126 = x118 + x120;
*x122 = x126;
} else {
if (x119) {
bool* x123 = x117;
*x123 = true;
int* x122 = x116;
int x118 = x112;
*x122 = x118;
} else {
if (x121) {
bool* x123 = x117;
*x123 = true;
int* x122 = x116;
int x120 = x114;
*x122 = x120;
} else {
bool* x123 = x117;
*x123 = false;
}
}
}
}
SM_FUNC(mod1) void x165 (int x144,bool x145,int* x146,bool* x147) {
bool x149 = x145;
if (x149) {
int x148 = x144;
int x152 = abs(x148);
bool x153 = x152 < 10;
if (x153) {
bool* x151 = x147;
*x151 = true;
int* x150 = x146;
*x150 = x148;
} else {
bool* x151 = x147;
*x151 = false;
}
} else {
bool* x151 = x147;
*x151 = true;
}
}
SM_FUNC(mod1) void x177 (int x167,bool x168) {
bool x170 = x168;
if (x170) {
int x171 = x166;
int x169 = x167;
int x172 = x171 + x169;
x166 = x172;
} else {
}
}
SM_INPUT(mod1,x238,x193,x194) { //top1
x192();
bool x198 = false;
int x199;
int x200 = x199;
int* x201 = &x200;
bool x202 = x198;
bool* x203 = &x202;
uint8_t* x195 = x193;
int x196 = x194;
x31(x195,x196,x201,x203);
bool x206 = false;
int x207;
int x208 = x207;
int* x209 = &x208;
bool x210 = x206;
bool* x211 = &x210;
x78(x200,x202,x209,x211);
bool x214 = false;
int x215;
int x216;
bool x217 = false;
int x218 = x216;
bool x219 = x217;
int x220 = x215;
int* x221 = &x220;
bool x222 = x214;
bool* x223 = &x222;
x143(x208,x210,x218,x219,x221,x223);
bool x226 = false;
int x227;
int x228 = x227;
int* x229 = &x228;
bool x230 = x226;
bool* x231 = &x230;
x165(x220,x222,x229,x231);
x177(x228,x230);
x182();
}
SM_FUNC(mod1) void x62 (uint8_t* x32,int x33,int* x34,bool* x35) {
int x40 = 0;
int x41 = 0;
int x37 = x33;
uint8_t* x36 = x32;
for (;;) {
int x42 = x41;
bool x43 = x42 < x37;
if (!x43) break;
int x45 = x41;
uint8_t x46 = x36[x45];
int x50 = x40;
int x47 = (int ) x46;
int x48 = x45 * 8;
int x49 = x47 << x48;
int x51 = x50 + x49;
x40 = x51;
int x53 = x45 + 1;
x41 = x53;
}
bool* x39 = x35;
*x39 = true;
int x58 = x40;
int* x38 = x34;
int x59 = (int ) x58;
*x38 = x59;
}
SM_FUNC(mod1) void x94 (int x79,bool x80,int* x81,bool* x82) {
bool x84 = x80;
if (x84) {
bool* x86 = x82;
*x86 = true;
int* x85 = x81;
*x85 = 1;
} else {
bool* x86 = x82;
*x86 = false;
}
}
SM_FUNC(mod1) void x111 (int x95,bool x96,int* x97,bool* x98) {
bool x100 = x96;
if (x100) {
bool* x102 = x98;
*x102 = true;
int* x101 = x97;
int x99 = x95;
int x104 = 0 - x99;
*x101 = x104;
} else {
bool* x102 = x98;
*x102 = false;
}
}
SM_INPUT(mod1,x293,x240,x241) { //top3
x192();
bool x245 = false;
int x246;
int x247 = x246;
int* x248 = &x247;
bool x249 = x245;
bool* x250 = &x249;
uint8_t* x242 = x240;
int x243 = x241;
x62(x242,x243,x248,x250);
bool x253 = false;
int x254;
int x255 = x254;
int* x256 = &x255;
bool x257 = x253;
bool* x258 = &x257;
x94(x247,x249,x256,x258);
bool x261 = false;
int x262;
int x263 = x262;
int* x264 = &x263;
bool x265 = x261;
bool* x266 = &x265;
x111(x255,x257,x264,x266);
bool x269 = false;
int x270;
int x271;
bool x272 = false;
int x273 = x271;
bool x274 = x272;
int x275 = x270;
int* x276 = &x275;
bool x277 = x269;
bool* x278 = &x277;
x143(x273,x274,x263,x265,x276,x278);
bool x281 = false;
int x282;
int x283 = x282;
int* x284 = &x283;
bool x285 = x281;
bool* x286 = &x285;
x165(x275,x277,x284,x286);
x177(x283,x285);
x182();
}
DECLARE_SM(mod1, 0x1234);
void timer_handler() {
tsc_t time = tsc_read();
static int intervals_5s = 0;
int currTime = (float)time / 100000000;

if(currTime > intervals_5s){

intervals_5s = currTime;

int x315 = 5;
int x316 = x315;
uint8_t x317 = (uint8_t ) x316;
uint8_t* x318 = &x317;
size_t x319 = sizeof(x317);
x238(x318,x319);
}

};
static void x311 () {
  //INIT FUNCTION
  WDTCTL = WDTHOLD | WDTPW;
  uart_init();
  pmodcls_init();
  pmodcls_set_wrap_mode(PmodClsWrapAt16);
  buttons_init();
  asm("eint");
}
static void x314 () {
  //DEPLOY FUNCTION
  sancus_enable(&mod1);
  sm_register_existing(&mod1);

}
static void x308 (int x296) {
bool x297 = x296 == 1;
if (x297) {
int x298 = 1;
int x299 = x298;
uint8_t x300 = (uint8_t ) x299;
uint8_t* x301 = &x300;
size_t x302 = sizeof(x300);
x293(x301,x302);
} else {
}
}
int main() {
x311();
puts("main started");
x314();
buttons_register_callback(Button1,x308);
while(1) {
  buttons_handle_events();
  timer_handler();
}
return 0;
};
/*****************************************
  End of C Generated Code                  
*******************************************/