package org.disturbednny.cereal.cerealSpringBootRest.service

import org.aspectj.lang.reflect.CatchClauseSignature
import org.disturbednny.cereal.cerealSpringBootRest.db.Sensors
import org.disturbednny.cereal.cerealSpringBootRest.db.WeatherData
import org.disturbednny.cereal.cerealSpringBootRest.db.model.Sensor
import org.disturbednny.cereal.cerealSpringBootRest.db.model.WeatherMetric
import org.disturbednny.cereal.cerealSpringBootRest.domain.SensorInputRequest
import org.disturbednny.cereal.cerealSpringBootRest.domain.SensorInputResponse
import org.disturbednny.cereal.cerealSpringBootRest.pojo.PressureSensor
import org.disturbednny.cereal.cerealSpringBootRest.pojo.TemperatureSensor
import org.disturbednny.cereal.cerealSpringBootRest.pojo.WindSensor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.time.ZonedDateTime

@Service
class SensorCollectionService {
    private static final Logger log = LoggerFactory.getLogger(SensorCollectionService.class)

    private WeatherData metricRepository
    private Sensors sensors

    @Autowired
    SensorCollectionService(WeatherData metricRepository, Sensors sensors) {
        this.metricRepository = metricRepository
        this.sensors = sensors
    }

    /**
     * this method takes in the sensor input request, does some validation per sensor type, and then persists the data to the database.
     * If this is the first time a sensor has phoned home, it also persists its metadata to the sensors table in the database
     * @param sensorInputRequest
     * @return result on success or failure
     */
    SensorInputResponse saveSensorData(SensorInputRequest sensorInputRequest) {
        SensorInputResponse result = new SensorInputResponse()
        List<WeatherMetric> metricsToSave = new ArrayList<>()
        Sensor sensordb = null
        switch (sensorInputRequest.sensorType.toLowerCase()) {
            case "temperature": {
                log.debug("persisting temperature and humidity from sensor ${sensorInputRequest.sensorName}")
                if(!sensorInputRequest.metrics.containsKey("temperature") || !sensorInputRequest.metrics.containsKey("humidity")) {
                    result.message = "metrics list is missing one or more required fields for sensor type ${sensorInputRequest.sensorType}. required metrics are temperature and humidity."
                    result.success = false
                    return result
                }
                TemperatureSensor sensor = new TemperatureSensor(sensorInputRequest.sensorName, sensorInputRequest.sensorLocation)
                try {
                    sensor.temperature = sensorInputRequest.metrics["temperature"]
                    sensor.relativeHumidity = sensorInputRequest.metrics["humidity"]
                }
                catch(Exception ex) {
                    result.message = "error in parsing metrics for temperature sensor. check your inputs"
                    result.success = false
                    log.error(result.message)
                    return result
                }
                //check to see if the sensor is in the list of sensors already
                try {
                    sensordb = sensors.findByName(sensor.name)
                    if (sensordb == null) {
                        sensordb = new Sensor()
                        sensordb.location = sensor.location
                        sensordb.type = sensor.type
                        sensordb.name = sensor.name
                        sensordb = sensors.save(sensordb)
                    }
                }
                catch (Exception ex) {
                    result.message = "error looking up sensor in database. ${ex.message}"
                    result.success = false
                    log.error(result.message)
                    return result
                }

                try {
                    // now persist all metrics
                    WeatherMetric temperature = new WeatherMetric()
                    temperature.setValue(sensor.getTemperature())
                    temperature.setUnitOfMeasure(sensor.temperatureUoM)
                    temperature.setDateTime(ZonedDateTime.now())
                    temperature.setSensor(sensordb)
                    temperature.setName("temperature")
                    metricsToSave.add(temperature)
                    WeatherMetric humidity = new WeatherMetric()
                    humidity.setValue(sensor.getRelativeHumidity())
                    humidity.setUnitOfMeasure("%")
                    humidity.setDateTime(ZonedDateTime.now())
                    humidity.setSensor(sensordb)
                    humidity.setName("humidity")
                    metricsToSave.add(humidity)

                    metricRepository.saveAll(metricsToSave)
                }
                catch (Exception ex) {
                    result.message = "error in persisting metrics to database ${ex}"
                    result.success = false
                    log.error(result.message)
                    return result
                }
                break
            }
            case "wind": {
                log.debug("persisting wind speed from sensor ${sensorInputRequest.sensorName}")
                if(!sensorInputRequest.metrics.containsKey("speed") || !sensorInputRequest.metrics.containsKey("unit")) {
                    result.message = "metrics list is missing one or more required fields for sensor type ${sensorInputRequest.sensorType}. required metrics are speed and unit."
                    result.success = false
                    return result
                }
                WindSensor sensor = new WindSensor(sensorInputRequest.sensorName, sensorInputRequest.sensorLocation)
                sensor.speed = Double.parseDouble(sensorInputRequest.metrics["speed"])
                sensor.uoM = sensorInputRequest.metrics["unit"]
                //check to see if the sensor is in the list of sensors already
                try {
                    sensordb = sensors.findByName(sensor.name)
                    // if sensor isn't already in database, save it
                    if (sensordb == null) {
                        sensordb = new Sensor()
                        sensordb.location = sensor.location
                        sensordb.type = sensor.type
                        sensordb.name = sensor.name
                        sensordb = sensors.save(sensordb)
                    }
                }
                catch (Exception ex) {
                    result.message = "error looking up sensor in database. ${ex.message}"
                    result.success = false
                    log.error(result.message)
                    return result
                }

                try {
                    // now persist all metrics
                    WeatherMetric windSpeed = new WeatherMetric()
                    windSpeed.setValue(sensor.getSpeed())
                    windSpeed.setUnitOfMeasure(sensor.uoM)
                    windSpeed.setDateTime(ZonedDateTime.now())
                    windSpeed.setSensor(sensordb)
                    windSpeed.setName("wind speed")
                    metricsToSave.add(windSpeed)
                    metricRepository.saveAll(metricsToSave)
                }
                catch (Exception ex) {
                    result.message = "error in persisting metrics to database ${ex}"
                    result.success = false
                    log.error(result.message)
                    return result
                }
                break
            }
            case "pressure": {
                log.debug("persisting atmospheric pressure from sensor ${sensorInputRequest.sensorName}")
                if(!sensorInputRequest.metrics.containsKey("pressure") || !sensorInputRequest.metrics.containsKey("unit")) {
                    result.message = "metrics list is missing one or more required fields for sensor type ${sensorInputRequest.sensorType}. required metrics are pressure and unit."
                    return result
                }
                PressureSensor sensor = new PressureSensor(sensorInputRequest.sensorName, sensorInputRequest.sensorLocation)
                sensor.pressure = Double.parseDouble(sensorInputRequest.metrics["pressure"])
                sensor.uoM = sensorInputRequest.metrics["unit"]
                //check to see if the sensor is in the list of sensors already
                try {
                    sensordb = sensors.findByName(sensor.name)
                    // if sensor isn't already in database, save it
                    if (sensordb == null) {
                        sensordb = new Sensor()
                        sensordb.location = sensor.location
                        sensordb.type = sensor.type
                        sensordb.name = sensor.name
                        sensordb = sensors.save(sensordb)
                    }
                }
                catch (Exception ex) {
                    result.message = "error looking up sensor in database. ${ex.message}"
                    result.success = false
                    log.error(result.message)
                    return result                }

                try {
                    // now persist all metrics
                    WeatherMetric pressure = new WeatherMetric()
                    pressure.setValue(sensor.getPressure())
                    pressure.setUnitOfMeasure(sensor.uoM)
                    pressure.setDateTime(ZonedDateTime.now())
                    pressure.setSensor(sensordb)
                    pressure.setName("pressure")
                    metricsToSave.add(pressure)
                    metricRepository.saveAll(metricsToSave)
                }
                catch (Exception ex) {
                    result.message = "error in persisting metrics to database ${ex}"
                    result.success = false
                    log.error(result.message)
                    return result
                }
                break
            }
            default:
                String message = "sensor type ${sensorInputRequest.sensorType} is not defined. file a feature request to have this sensor type added"
                log.error(message)
                result.success = false
                result.message = message
                return result
        }
        result.success = true
        result.message = "successfully persisted ${metricsToSave.size()} metrics to the database for Sensor ${sensorInputRequest.sensorName}, location ${sensorInputRequest.sensorLocation}"
        return result
    }
}
