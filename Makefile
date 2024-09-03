release:
	mvn release:clean release:prepare release:perform -Dgoals=deploy release:clean
