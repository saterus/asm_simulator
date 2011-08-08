;20. LDI instruction test error2, incorrect operands

LDI2	.ORIG
Start	LDI	=#45,%4462
		.END	Start

;Expected ouput: Fatal Error: Invalid signature for opcode,Immediate used in place of register or vise-versa