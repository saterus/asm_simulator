WTF	.ORIG
X	.BLKW	X + 5
Y	.FILL	Y * 2
Z	.BLKW	J*2 + 2 - J - J
start	XOR	R0, X, R5, R1
	SHL	R0, Y
halt	TRAP	x25
	.END	start
J	.EQU	=4