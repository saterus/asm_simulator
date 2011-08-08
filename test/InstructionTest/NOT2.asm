;29. Not instruction test error2. Missing operand

Not1	.ORIG
Start	NOT	R1
	.END	Start

;Expected output: Fatal Error: Incorrect number of arguments
; (Correction: this is a synthetic instruction (corresponding to NOT R1, R1))
