;4. AND instruction test, nonexistent Ab label

AND1	.ORIG
Start	AND	Ab,R2,r8
	.END	start


;Expected output:  Invalid signature for opcode,Immediate used in place of register or vise-versa