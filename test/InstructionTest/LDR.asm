;22. LDR instruction test error1, incorrect operand


LDR1	.ORIG
Start	LDR	R1,R2,R9
	.END	Start 

;Expected output: Fatal Error: Invalid signature for opcode,Immediate used in place of register or vise-versa
; (Note: R9 is a valid symbol, but is not defined in this case.)
