;18.Load instruction test error , memory error

Load	.ORIG
Start	LD	R1,X
X		.FILL	xFFFFFF
		.END	Start

;Expected output: Fatal Error: Argument out of range for type
