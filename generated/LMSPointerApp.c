/*****************************************
  Emitting C Generated Code                  
*******************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
void x0 (int32_t* x1) {
*x1 = 20;
int32_t x3 = 5;
int32_t x4 = x3;
int32_t* x5 = &x4;
int32_t x6 = *x5;
printf("%d\n",x6);
*x5 = 3;
int32_t x9 = *x5;
printf("%d\n",x9);
};
void x12 () {
int32_t x13 = 5;
int32_t x14 = x13;
int32_t* x15 = &x14;
printf("%s","Before call: ");
int32_t x17 = *x15;
printf("%d\n",x17);
x0(x15);
printf("%s","After call: ");
int32_t x21 = *x15;
printf("%d\n",x21);
};
/*****************************************
  End of C Generated Code                  
*******************************************/
