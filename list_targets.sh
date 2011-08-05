#!/bin/sh
grep \<target build.xml | ruby -ne '$_ =~ /name="([^"]+)"/; puts $1;'
