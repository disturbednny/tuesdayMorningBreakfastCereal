# tuesdayMorningBreakfastCereal
fun project


#### To run locally
You must have a postgresql installed locally.
Once postgres is installed log into postgres either through pgadmin or pql and use the following as a guideline to: 
* Create a user
* Create the station database
* Grant access to the database for that user

```
    CREATE USER {database_user} WITH PASSWORD {user_password}
    CREATE DATABASE station;
    GRANT ALL PRIVILEGES ON DATABASE station TO {database_user}
```
Once all of this is done, you can add the username and password to the environment through the following env vars:
* POSTGRES_USERNAME
* POSTGRES_PASSWORD

If you don't, default user name and password will be used (see [application.yml](src/main/resources/application.yaml))

Liquibase will run on first startup and create the missing schema and tables. If after initial startup you want to seed data. There is a python script which will inject the data into the sensor and metric tables.