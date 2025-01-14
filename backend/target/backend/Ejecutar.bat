SETLOCAL ENABLEEXTENSIONS
SET JAVA_HOME="./jre"
start http://localhost:8080/login
"./jre/bin/java.exe" -jar "BarTunesVote-0.0.1-SNAPSHOT.jar"
pause