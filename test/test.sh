#!/bin/sh

print () {

    echo "";
    echo "Test: $1";
    echo "==============================";
    echo "";
    java -cp ../bin edu.osu.cse.mmxi.asm.Assembler "$1" -i;
    echo "";


}

for i in *.asm; do
    print $i;
done

for i in */*.asm; do
    print $i;
done
