QUINE	.ORIG	; x1000		; Quine
OUT	.EQU	x21
PUTS	.EQU	x22
HALT	.EQU	x25
start
bitmsk	.BLKW	16		;
cl	.BLKW	1		; current loc (cl) (filled by prog)
datSta	; data start
str	.STRZ	"HQUINE "
constE	.EQU	str + 5
mStart	.FILL	start		; header start (hs)
mLen	.FILL	end - start	; will be set to end - bitmsk
mEndN	.FILL	~(-end)		; will be set to -end
mEx	.FILL	ex		; exec (ex)
ex	LD	R0, =1		; a = 1 
	LEA	R1, bitmsk	; b = *m[0]; do {
loop1	PUSH	R0, R1		;   m[b++] = a
	DBL	R0		;   a *= 2
	BRnp	loop1		; } while (a != 0)
	PRNTS	str		; "HQUINE_" string
	LD	R6, mStart
	JSR	pHex
	LD	R6, mLen
	JSR	pHex
	PRNT	=xA
	LEA	R2, datSta
loop2	ST	R2, cl
	PRNT	=x54		; 'T'
	LD	R6, cl
	JSR	pHex
	LD	R2, cl
	LDR	R6, R2
	JSR	pHex
	PRNT	=xA
	LD	R2, cl
	INC	R2
	LD	R3, mEndN
	NOT	R3
	INC	R3, R2
	BRn	loop2
	PRNT	constE
	LD	R6, mEx
	JSR	pHex
	PRNT	=xA
	TRAP	HALT
pHex	; hex in R6
	LEA	R1, bitmsk+15	; b = *m[15]; do {
loop3	CLR	R0		;   a = 0
	ADD	R2, R0, 3	;   c = 3; do {
loop4	LDR	R3, R1		;     d = m[b]
	AND	R4, R3, R6	;     e = g & c
	BRz	if1		;     if (g & c != 0) {
	LEA	R3, bitmsk	;       d = *m[0]
	INC	R3, R2		;       d += c
	LDR	R3, R3		;       d = m[c]
	INC	R0, R3		;       a += m[c] }
if1	DEC	R1		;     b--
	DEC	R2		;     c--
	BRzp	loop4		;   } while (c >= 0)
	DEC	R0, 10		;   a -= 10
	BRn	if2		;   if (a >= 0)
	INC	R0, 'A'-'0'-10	;     a += 'A' - '0' - 10
if2	LD	R2, =(10+'0')	;   c = 10 + '0'
	INC	R0, R2		;   a += c
	TRAP	OUT		;   print(a)
	NOT	R3, R1		;   d = ~b
	AND	R3, xF		;   d &= 0xF
	BRnp	loop3		; } while (d != 0)
	RET
end	.END	ex
