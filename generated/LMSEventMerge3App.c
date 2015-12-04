%%%%%%%%%%%%%%%%%%%%%%%%%%
MergeApp3:
Creating flow graph...
Create InputEvent(ID:1): Set(1)
Create InputEvent(ID:2): Set(2)
Create MergeEvent(ID:3): Set(1, 2). Left: Set(1), Right: Set(2)
Create ConstantEvent(ID:4): Set(2)
Create MergeEvent(ID:5): Set(1, 2). Left: Set(1, 2), Right: Set(2)
Create MapEvent(ID:6): Set(1, 2)
MergeEvent(ID:5) ID=1: Disjoint
MergeEvent(ID:3) ID=1: Disjoint
MergeEvent(ID:5) ID=2: Non-Disjoint


/* FILE: top1.c */
void top1(void);
int32_t mapfun6(int32_t);
int32_t constantfun4(int32_t);
int32_t inputfun2(void);
void top2(void);
int32_t inputfun1(void);
int32_t mergefun5(int32_t, int32_t);
/*****************************************
  Emitting C Generated Code                  
*******************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
void top1(void  x0) {
int32_t x1 = inputfun1();
int32_t x2 = mapfun6(x1);
}
/*****************************************
  End of C Generated Code                  
*******************************************/
/* FILE: mapfun6.c */
void top1(void);
int32_t mapfun6(int32_t);
int32_t constantfun4(int32_t);
int32_t inputfun2(void);
void top2(void);
int32_t inputfun1(void);
int32_t mergefun5(int32_t, int32_t);
/*****************************************
  Emitting C Generated Code                  
*******************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
int32_t mapfun6(int32_t  x4) {
int32_t x5 = x4 * 2;
return x5;
}
/*****************************************
  End of C Generated Code                  
*******************************************/
/* FILE: constantfun4.c */
void top1(void);
int32_t mapfun6(int32_t);
int32_t constantfun4(int32_t);
int32_t inputfun2(void);
void top2(void);
int32_t inputfun1(void);
int32_t mergefun5(int32_t, int32_t);
/*****************************************
  Emitting C Generated Code                  
*******************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
int32_t constantfun4(int32_t  x6) {
return 2;
}
/*****************************************
  End of C Generated Code                  
*******************************************/
/* FILE: inputfun2.c */
void top1(void);
int32_t mapfun6(int32_t);
int32_t constantfun4(int32_t);
int32_t inputfun2(void);
void top2(void);
int32_t inputfun1(void);
int32_t mergefun5(int32_t, int32_t);
/*****************************************
  Emitting C Generated Code                  
*******************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
int32_t inputfun2(void  x7) {
return 10;
}
/*****************************************
  End of C Generated Code                  
*******************************************/
/* FILE: top2.c */
void top1(void);
int32_t mapfun6(int32_t);
int32_t constantfun4(int32_t);
int32_t inputfun2(void);
void top2(void);
int32_t inputfun1(void);
int32_t mergefun5(int32_t, int32_t);
/*****************************************
  Emitting C Generated Code                  
*******************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
void top2(void  x8) {
int32_t x9 = inputfun2();
int32_t x10 = constantfun4(x9);
int32_t x11 = mergefun5(x9,x10);
int32_t x12 = mapfun6(x11);
}
/*****************************************
  End of C Generated Code                  
*******************************************/
/* FILE: inputfun1.c */
void top1(void);
int32_t mapfun6(int32_t);
int32_t constantfun4(int32_t);
int32_t inputfun2(void);
void top2(void);
int32_t inputfun1(void);
int32_t mergefun5(int32_t, int32_t);
/*****************************************
  Emitting C Generated Code                  
*******************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
int32_t inputfun1(void  x14) {
return 5;
}
/*****************************************
  End of C Generated Code                  
*******************************************/
/* FILE: mergefun5.c */
void top1(void);
int32_t mapfun6(int32_t);
int32_t constantfun4(int32_t);
int32_t inputfun2(void);
void top2(void);
int32_t inputfun1(void);
int32_t mergefun5(int32_t, int32_t);
/*****************************************
  Emitting C Generated Code                  
*******************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
int32_t mergefun5(int32_t  x15, int32_t  x16) {
int32_t x17 = x15 + x16;
return x17;
}
/*****************************************
  End of C Generated Code                  
*******************************************/
%%%%%%%%%%%%%%%%%%%%%%%%%%



