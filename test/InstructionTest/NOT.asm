;28. Not instruction test error1. incorrect operands

Not1	.ORIG
Start	NOT	x33,Idon'tlikeyou
		.END	Start

;Expected output: Fatal Error: Invalid signature for opcode,Immediate used in place of register or vise-versa