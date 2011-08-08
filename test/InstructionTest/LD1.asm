;16. Load instruction test error 1, missing address 

Load	.ORIG
Start	LD	R1,X
		.END	Start

;Expected output: Fatal Error: Undefined Symbol