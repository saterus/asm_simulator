;25. LEA instruction test error1, incorrect operand.


LEA1	.ORIG
Start	LEA	x45,Bat,R9
	.END	Start

;Expected output: Fatal Error: Invalid signature for opcode,Immediate used in place of register or vise-versa