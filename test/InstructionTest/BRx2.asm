;6.BRx instruction test 2.


BR2		.ORIG
Start	BRN	x9999F
		.END	Start

;Expected output: Fatal Error: Argument type out of range
; (Correction: numbers outside of reasonable short range
; are not considered numbers, so are candidates as symbols.
; So this is actually an undefined symbol error.)
