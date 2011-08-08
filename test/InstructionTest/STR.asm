;36. STR instruction test error 1. incorrect operands.

STR	.ORIG
Start	STR	R1,R2,R1
	.END	Start

;Expected output: Fatal Error: Invalid signature for opcode,Immediate used in place of register or vise-versa