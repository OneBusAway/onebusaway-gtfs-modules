release:
	git checkout main
	git pull
	mvn release:clean release:prepare release:perform -Dgoals=deploy release:clean
