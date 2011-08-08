;3. ADD instruction test 2, incorrect usage of arguments

ADD2	.ORIG
Start	ADD	R2,2, =#1
	.END	start

;Expected output:  Invalid signature for opcode,Immediate used in place of register or vise-versa