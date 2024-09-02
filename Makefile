release:
	mvn release:prepare release:perform -Dgoals=deploy release:clean
