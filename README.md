# towerofhanoi
Tower of Hanoi game, using Java and JavaFX for GUI


The project will use JavaFX as front end, and H2 Database. 
For H2 Database configuration, please use the H2-*.jar from project folder /TowerOfHanoi/lib, to run the H2 shell and create database

> java -cp h2-*.jar org.h2.tools.Shell

Welcome to H2 Shell
Exit with Ctrl+C
[Enter]   jdbc:h2:mem:2
URL       jdbc:h2:./path/to/database
[Enter]   org.h2.Driver
Driver
[Enter]   sa
User     sa
Password  
Type the same password again to confirm database creation.
Password 
Connected

And run the following to create database. The Script is also in project folder /TowerOfHanoi
sql> RUNSCRIPT ‘create_table.sql’

After database is created, type ‘quit’ to quit and close the connection.

Modify the pathToDb variable in class DbHelper to reflect the path to your newly created database.

