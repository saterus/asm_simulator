;42. Test .STRZ error

STRZ1	.ORIG
	.STRZ	x45
Start	ADD	R1,R1,R1
	.END	Start

;Expected Output: Fatal Error: Argument must be a string
