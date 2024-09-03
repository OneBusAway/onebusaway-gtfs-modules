release:
	git checkout master
	git pull
	mvn release:clean release:prepare release:perform -Dgoals=deploy release:clean
