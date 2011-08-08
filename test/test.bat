for /R %%i in (*.asm) do (
java -cp ../bin edu.osu.cse.mmxi.asm.Assembler %%i -i 
pause
)
