Prgrm	.ORIG

	.EXT Hello
	.EXT World
	.EXT Cse56

space	.STRZ " "

	JSR	Hello
	
	LEA	R0, space
	TRAP	x22

	JSR	World

	LEA	R0, space
	TRAP	x22

	JSR	Cse56

	TRAP	x25

	.END

	