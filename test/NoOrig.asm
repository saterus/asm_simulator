;3. Missing .ORIG

Test1 		x30B0
count	.fill	#4
begin 	LD	ACC,count
ACC	.EQU	#1
	.END 	begin

;expected output: Fatal Error: No . ORIG record found
; (Correction: these lines are syntax errors;
; leave the record off completely to get .ORIG error)
