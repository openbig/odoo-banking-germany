#!/bin/sh

# Linux Start-Script fuer Server-Betrieb.
# Jameica wird hierbei OHNE GUI gestartet.

link=$(readlink -f "$0")
dir=$(dirname "$link")
cd "$dir" 

#_JCONSOLE="-Dcom.sun.management.jmxremote"

# https://www.willuhn.de/bugzilla/show_bug.cgi?id=798

java -Xmx256m $_JCONSOLE -jar jameica-linux.jar -d $@
