Lab2EG	.ORIG
count	.FILL	#4
begin	LD	ACC, count
	LEA	R0,msg
loop	TRAP	x22
	ADD	ACC, ACC, #-1
	BRP	loop
	JMP	Next
msg	.STRZ	"hi! "
Next	AND	R0, R0, x0
	NOT	R0, R0
	ST	R0, Array
	LEA	R5, Array
	LD	R6, =#100
	STR	R0, R5, #1
	TRAP	x25
ACC	.EQU	#1
Array	.BLKW	#3
	.FILL	x10
	.END	begin
	
