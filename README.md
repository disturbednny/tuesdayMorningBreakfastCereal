# tuesdayMorningBreakfastCereal
This service is built with a combination of groovy and java. since Groovy is a language built on top of java, the two can be used interchangeably during development in order to speed up some code creation.

The backing database is currently postgres, but it could be changed to a different database if it is deemed that the performance isn't there in high traffic scenarios. for the purposes of this POC, I decided to use what I have been using the last few years.
Influx may be a potential replacement due to grouping and time sharding, however my expertise on using this type of database is limited.
The database has to exist, but the database schema, and its tables, are managed by Liquibase in order to make sure the state of the database is tracked for any changes made.

## Purpose
The purpose of the service is to receive sensor data from sensors and persist them to a database for future querying.

## Using the service
### Sending data to the service
In order for the service to handle the sensor data properly, the data has to be sent via a PUT command with the body formatted in JSON. Each sensor type has a json schema, and the examples are provided below:

<table>
<tr><td> Sensor Type</td><td>Schema</td><td>Notes</td></tr>
<tr><td>Temperature</td><td>

```json
{
  "sensorName": "sensorName",
  "sensorType": "temperature",
  "sensorLocation" : "sensorLocation",
  "metrics" : {
    "temperature": "68.5F", // or "68.5 F" or "-32.0 F" 
    "humidity": "50.0%" // or "50%" or "50.0 %"
  }
}
```
</td>
<td>Temperature can be Celcius or Fahrenheit, but will be stored as Celcius in the database. Currently only one space is allowed between the number value and the unit.</td></tr>
<tr><td>Pressure</td><td>

```json
{
  "sensorName": "sensorName",
  "sensorType": "pressure",
  "sensorLocation" : "sensorLocation",
  "metrics" : {
    "pressure": "450",
    "unit": "bar"
  }
}
```
</td>
<td>Pressure is currently only supported as bar. Conversion should happen before the sensor sends the metrics to the service.</td></tr>
<tr><td>Wind Speed</td><td>

```json
{
  "sensorName": "sensorName",
  "sensorType": "wind",
  "sensorLocation" : "sensorLocation",
  "metrics" : {
    "speed": "12.0",
    "unit": "mph"
  }
}
```
</td>
<td>Currently only mph is supported for wind speed. Conversion should happen before the sensor sends the metrics to the service.</td></tr>
</table>

### Querying the service for data

You can query the service for sensor data. Below is the json schema for sending the request to the service:

```json
{
  "statisticType": "all", // min, max, average
  "metrics": "temperature,humidity", // either all or comma delimited list
  "sensorName": "sensorName",
  "daysBack": 1 // can be up to a month(30 days)
}
```


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

If you don't, default username and password will be used (see [application.yml](src/main/resources/application.yaml))

Liquibase will run on first startup and create the missing schema and tables. If after initial startup you want to seed data. There is a python script which will inject the data into the sensor and metric tables.