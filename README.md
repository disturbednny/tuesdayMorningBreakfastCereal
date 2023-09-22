# tuesdayMorningBreakfastCereal
This service is built with a combination of Groovy and Java. Because Groovy is a language built on top of Java, the two can be used interchangeably during development in order to speed up some code creation.

The backing database is currently Postgres, but it could be changed to a different database if it is deemed that the performance isn't there in high traffic scenarios. For the purposes of this POC, I decided to use what I have been using the last few years.
Influx may be a potential replacement due to grouping and time sharding, however my expertise on using this type of database is limited.
The database has to exist, but the database schema, and its tables, are managed by Liquibase in order to make sure the state of the database is tracked for any changes made.

## Purpose
The purpose of the service is to receive sensor data from sensors and persist them to a database for future querying.

## Using the service
In order to interact with the service, you will need to either use curl or postman if you prefer a gui.
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
  "query": "give me the min max and average temperature and humidity from sensor temp-1 for the last three days"
}
```
Words that the service will pick up on are in the below table:

| type         | words                                           | function                                                                                                                                         |
|--------------|-------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------|
| statistic    | **min** or **max** or **average**               | gives the min/max/average metric for the value                                                                                                   |
| sensor       | **sensor {sensorName}**                         | the name of the senosr(s) you want to get the values for. can be one or multiple. must preface name with "sensor " in order for it to pick it up |
| temporal     | last/past **{number}** **day(s)/week(s)/month** | number is optional. will default to 1. limited to one months worth of data                                                                       |
| range        | between YYYY-MM-DD and YYYY-MM-DD               | date range. cannot be longer than one month. must be in ISO date format                                                                          |
| specifc date | YYYY-MM-DD                                      | must be in ISO date format                                                                                                                       |
| metric       | **temperature humidity pressure speed**         | currently supported metrics from the supported sensor types                                                                                      |
If no temporal or range is given, the default behavior is to ignore the statistic words and return only the latest values.
If no sensors are given, or if the sensor is not found, the service will return the metric values for all sensors that have those metrics
If no metrics are given, then it will not perform the statistic on any of the metrics for the sensor

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

If you use intelliJ, you can set up the run parameters and add in the environment variables there. otherwise set them in your environment before running the following
```shell
./gradlew bootRun
```