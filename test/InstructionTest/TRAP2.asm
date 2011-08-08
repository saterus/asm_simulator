;39.TRAP instruction test2. Nonexistent trap calls

TRAP1	.ORIG
Start	TRAP	x26
	.END	Start

;Expected output: Fatal Error: Argument type out of range
; (Correction: this is valid assembly, just not valid on
; our simulator. The simulator could (in theory) be extended
; with the other trap calls, but the operation itself is
; no syntax error.)
