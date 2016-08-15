# !/bin/bash

insdir="$HOME/screachdiscordbot"
jar="$insdir/screachdiscordbot.jar"
cfg="$insdir/botcfg.json"
launcher="$insdir/Launch.sh"

#Check if the installation directory exists.
if [ -d "$insdir" ];
then
	echo "Installation directory found."
else
	mkdir ~/screachdiscordbot
fi


#Config file 
if [ -f "$cfg" ];
then
	echo -e "\nConfiguration file found.\n"
else
	echo -e "\nConfiguration file not found, downloading..."
	wget -O $cfg https://raw.githubusercontent.com/ScreachFr/ScreachDiscordBot/master/lastbuild/botcfg.json	
	echo -e "\t!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
	echo -e "\t! Don't forget to specify your bot key in ~/screachdiscordbot/botcfg.json !"
	echo -e "\t!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
fi

#Launcher
if [ -f "$launcher" ];
then
	echo -e "\nLauncher found.\n"
else
	echo -e "\nLauncher not found, downloading..."
	wget -O $launcher https://raw.githubusercontent.com/ScreachFr/ScreachDiscordBot/master/lastbuild/Launch.sh	
fi

#Check if the jar exists.
if [ -f "$jar" ];
then
	echo "Jar not found, downloading last version..."
else
	echo "Jar found, updating to last version..."
	rm -rf $jar
fi


wget -O $jar https://github.com/ScreachFr/ScreachDiscordBot/blob/master/lastbuild/screachdiscordbot.jar?raw=true 

chmod +x $launcher

echo "Installation/Update complete."






