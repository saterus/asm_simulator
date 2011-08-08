;41. Test .EQU error without label.

EQU1	.ORIG
		.EQU	x25
Start	ADD	R1,R1,R1
		.END	Start

;Expected Output: Fatal error: .EQU requires a label
