Prgrm	.ORIG

	.EXT Hello
	.EXT World

space	.STRZ " "

	JSR	Hello
	
	LEA	R0, space
	TRAP	x22

	JSR	World

	TRAP	x25

	.END

	