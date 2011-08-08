;6. Test for commenting error

Test4	.ORIG	
Run		.EQU	x25
start	ld	R1,=#100	R1<---100
		.END	start

;Expected output: Fatal Error: Invalid Parameter Count
; (Correction: there are only two parameters (separated
; by a comma), but the second has a syntax error.
