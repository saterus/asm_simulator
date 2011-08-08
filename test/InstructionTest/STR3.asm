;37. STR instruction test error 3. Memory fail

STR2	.ORIG
Start	STR	R1,R2,#566666608
	.END	Start


;Expected output:  Fatal Error: Argument type out of range