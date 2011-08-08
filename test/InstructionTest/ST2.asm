;32. ST instruction test error2. Missing address

ST2	.ORIG
Start	ST	R1,X
	.END	Start

;Expected output: Fatal Error: Label dereference to incorrect page
; (Correction: Um, X isn't defined?)
