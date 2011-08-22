Prgrm	.ORIG

	.EXT	Adder
	.EXT	Negat
	.ENT	Rtn

	;Clear the temp registers
	AND	R1, R1, x0
	AND	R2, R2, x0

	;Store input into R1
	TRAP	x33
	OR	R1, R0,	R1

	;Get input
	TRAP	x33

	;negate 
	JSR	Negat	

	;move R0 into R2
	OR	R2, R0, R2

	;add values and output
	JSR	Adder

Rtn	TRAP	x25

	.END