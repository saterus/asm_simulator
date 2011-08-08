;8. Testing nonexistent instruction 2

Test5 .ORIG
Run	.EQU	x25
start	ld	R1,=#100	
X	.FIL	x2
blk	.blkw	#3
	.END	start

;Expected output: Fatal Error: Unknown Op-code