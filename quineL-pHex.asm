pHex	.ORIG	; hex in R6
	.EXT	bitmsk
OUT	.EQU	x21
	LEA	R1, bitmsk	; b = *m[15]
	INC	R1, 15		; do {
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
