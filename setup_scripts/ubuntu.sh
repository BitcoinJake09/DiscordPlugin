#!/bin/sh

# make sure we're running from the scripts directory
if [ $(echo `pwd` | grep setup_scripts | wc -l) -ne 0 ]; then

	echo 'Installing maven and redis if necessary'
	sudo apt-get install -y maven redis-server

	echo 'Running maven'
	cd ..
	discordplugin_dir=`pwd`
	mvn package -B

	spigot_file='spigot-1.12.2-R0.1-SNAPSHOT.jar'

	echo 'Downloading $spigot_file'

	# place spigot alongside discordplugin
	mkdir -p ../spigot
	cd ../spigot
	spigot_dir=`pwd`
	rm $spigot_file 2> /dev/null
	wget http://jenkins.discordplugin.co/job/spigot/lastSuccessfulBuild/artifact/Spigot/Spigot-Server/target/$spigot_file

	# automatically agree to the eula so we don't have to run spigot twice during setup
	echo 'Agreeing to the Mojang EULA'
	echo '#By changing the setting below to TRUE you are indicating your agreement to our EULA (https://account.mojang.com/documents/minecraft_eula).' > eula.txt
	echo `date` >> eula.txt
	echo 'eula=true' >> eula.txt

	# create the plugins directory
	echo 'Creating the plugins directory and linking the discordplugin plugin'
	mkdir -p plugins
	rm plugins/discordplugin-all.jar 2> /dev/null
	ln -s $discordplugin_dir/build/libs/discordplugin-all.jar $spigot_dir/plugins/discordplugin-all.jar

	echo 'First time setup is complete'

	echo 'Starting spigot and redis to finish file population and check the environment'
	java -jar $spigot_file &
	cd ../discordplugin
	redis-server &

	echo "Done. Don't forget to kill spigot (code 3 on *nix to quit) and redis when you are finished."
	cd setup_scripts

else
	echo 'This script must be run from the setup_scripts directory. Change directories and try again.'
fi
