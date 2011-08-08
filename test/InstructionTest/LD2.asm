;17. Load instruction test error 2, incorrect operands


Load2	.ORIG
Start	LD	=#45,%4462
		.END	Start

;Expected output: Fatal Error: Invalid signature for opcode,Immediate used in place of register or vise-versa
; (Correction: %4462 is an error because % is the mod operator)
