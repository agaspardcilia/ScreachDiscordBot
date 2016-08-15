# !/bin/bash

jar="$HOME/screachdiscordbot/screachdiscordbot.jar"

#Check if the jar exists.
if [ -f $jar ];
then
	echo "Launching Screach's Discord Bot..."
	java -jar $jar
else
	echo "Error : $jar not found."
fi


