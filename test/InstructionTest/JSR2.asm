;12. JSR instruction test error 2

JTEST2	.ORIG
Start	JSR	P
hot	ADD R1,R1,R1
	.END	Start


;Expected output:Fatal Error:Undefined Symbol