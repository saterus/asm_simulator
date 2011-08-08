;10. Premature .END

PMEnd	.ORIG	x0
Run	.EQU	x25
start	ld	R1,=#100
X	.FILL	x2
blk	.blkw	#3
	.END	start
	ST	R2,x2

;Expected output: Fatal Error: "End of File reached prematurely."
; (Correction: this is not a problem)
