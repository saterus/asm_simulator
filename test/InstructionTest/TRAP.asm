;38. TRAP instruction test1. Incorrect operand

TRAP1	.ORIG
Start	TRAP	R1,#3
	.END	Start

;Expected output: Fatal Error: Incorrect number of argument