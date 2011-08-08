;10. Jump instruction test error 2


JUMP2	.ORIG
Start	JMP	
hot		ADD R1,R1,R1
		.END	Start


;Expected output: Fatal Error: Incorrect number of argument