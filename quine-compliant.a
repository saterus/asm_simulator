QUINE_   .ORIG   x1000           ; Quine
OUT      .EQU    x21
PUTS     .EQU    x22
HALT     .EQU    x25
bitmsk   .BLKW   #16             ;
cl       .BLKW   #1              ; current loc (cl) (filled by prog)
datSta                           ; data start
const1   .FILL   x0001           ; constants (c[.])
endl     .FILL   x000A           ;  | \n
cons58   .FILL   x003A           ;  |
constT   .FILL   x0054           ; -+ T
str      .FILL   x0048           ; string "HQUINE_"
         .FILL   x0051           ;  |
         .FILL   x0055           ;  |
         .FILL   x0049           ;  |
         .FILL   x004E           ;  |
constE   .FILL   x0045           ;  | E
         .FILL   x005F           ;  |
         .FILL   x0000           ; -+
mStart   .FILL   bitmsk          ; header start (hs)
mLen     .FILL   end             ; will be set to end - bitmsk
mEndN    .BLKW   #1              ; will be set to -end
mEx      .FILL   start           ; exec (ex)
start    LD      R0, mLen
         NOT     R0, R0
         ADD     R0, R0, #1
         ST      R0, mEndN
         LD      R0, mStart
         LD      R1, mLen
         NOT     R0, R0
         ADD     R0, R0, #1
         ADD     R0, R0, R1
         ST      R0, mLen
         LD      R0, const1      ; a = 1 
         LEA     R1, bitmsk      ; b = *m[0]; do {
loop1    STR     R0, R1, x0      ;   m[b] = a
         ADD     R1, R1, #1      ;   b++
         ADD     R0, R0, R0      ;   a *= 2
         BRnp    loop1           ; } while (a != 0)
         LEA     R0, str         ; "HQUINE_" string
         TRAP    PUTS
         LD      R6, mStart
         JSR     pHex
         LD      R6, mLen
         JSR     pHex
         LD      R0, endl
         TRAP    OUT
         LEA     R2, datSta
loop2    ST      R2, cl
         LD      R0, constT
         TRAP    OUT
         LD      R6, cl
         JSR     pHex
         LD      R2, cl
         LDR     R6, R2, x0
         JSR     pHex
         LD      R0, endl
         TRAP    OUT
         LD      R2, cl
         ADD     R2, R2, #1
         LD      R3, mEndN
         ADD     R3, R3, R2
         BRn     loop2
         LD      R0, constE
         TRAP    OUT
         LD      R6, mEx
         JSR     pHex
         LD      R0, endl
         TRAP    OUT
         TRAP    HALT
pHex                             ; hex in R6
         LEA     R1, xF ;bitmsk+15 ; b = *m[15]; do {
loop3    AND     R0, R0, #0      ;   a = 0
         ADD     R2, R0, #3      ;   c = 3; do {
loop4    LDR     R3, R1, x0      ;     d = m[b]
         AND     R4, R3, R6      ;     e = g & c
         BRz     if1             ;     if (g & c != 0) {
         LEA     R3, bitmsk      ;       d = *m[0]
         ADD     R3, R3, R2      ;       d += c
         LDR     R3, R3, x0      ;       d = m[c]
         ADD     R0, R0, R3      ;       a += m[c] }
if1      ADD     R1, R1, #-1     ;     b--
         ADD     R2, R2, #-1     ;     c--
         BRzp    loop4           ;   } while (c >= 0)
         ADD     R0, R0, #-10    ;   a -= 10
         BRn     if2             ;   if (a >= 0)
         ADD     R0, R0, #7      ;     a += 7
if2      LD      R2, cons58      ;   c = 58
         ADD     R0, R0, R2      ;   a += c
         TRAP    OUT             ;   print(a)
         NOT     R3, R1          ;   d = ~b
         AND     R3, R3, xF      ;   d = ~b & 0xf
         BRnp    loop3           ; } while (d != 0)
         RET
end
         .END    start