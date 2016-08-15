# !/bin/bash

#Check if the jar exists.
if [ -f "~/screachdiscordbot/screachsdiscordbot.jar" ];
then
	echo "Launching Screach's Discord Bot..."
	java -jar ~/screachdiscordbot/screachdiscordbot.jar
else
	echo "Error : ~/screachdiscordbot/screachsdiscordbot.jar not found."
fi


