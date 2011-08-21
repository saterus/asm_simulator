QUINE	.ORIG	; x1000		; Quine
	.EXT	pHex
	.ENT	bitmsk
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
end	.END	ex
