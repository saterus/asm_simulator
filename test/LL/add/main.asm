Prgrm	.ORIG

	.EXT	Adder
	.ENT	Rtn

	;Clear the temp registers
	AND	R1, R1, x0
	AND	R2, R2, x0

	;Store input into R1
	TRAP	x33
	OR	R1, R0,	R1

	;Store input into R2
	TRAP	x33
	OR	R2, R0, R2

	JSR	Adder
Rtn	TRAP	x25

	.END