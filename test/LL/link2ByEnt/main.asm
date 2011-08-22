Prgrm	.ORIG

	.EXT HeEnt
	.EXT WoEnt

space	.STRZ " "

	JSR	HeEnt
	
	LEA	R0, space
	TRAP	x22

	JSR	WoEnt

	TRAP	x25

	.END

	