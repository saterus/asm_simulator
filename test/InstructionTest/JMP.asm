;9. Jump instruction test error 1

JUMP	.ORIG
Start	JMP	P
hot		ADD R1,R1,R1
	.END	Start

;Expected output: Undefined Symbol