;21. LDI instruction test error3, memory errors

LDI3	.ORIG
Start	LDI	R1, X
X	.FILL	xFFFFFF
	.END	Start

;Expected ouput: Fatal Error: Argument type out of range