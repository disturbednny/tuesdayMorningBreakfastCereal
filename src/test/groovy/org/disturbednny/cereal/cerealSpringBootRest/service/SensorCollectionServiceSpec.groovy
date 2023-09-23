package org.disturbednny.cereal.cerealSpringBootRest.service

import org.disturbednny.cereal.cerealSpringBootRest.db.Sensors
import org.disturbednny.cereal.cerealSpringBootRest.db.WeatherData
import org.disturbednny.cereal.cerealSpringBootRest.db.model.Sensor
import org.disturbednny.cereal.cerealSpringBootRest.db.model.WeatherMetric
import org.disturbednny.cereal.cerealSpringBootRest.domain.SensorInputRequest
import spock.lang.Shared
import spock.lang.Specification

class SensorCollectionServiceSpec extends Specification {
    @Shared
    final Sensor testTempSensor = new Sensor().with {
        it.name = "test-temp1"
        it.type = "temperature"
        it.location = "base"
        it.id = 1
        return it
    }
    @Shared
    final Sensor testPressureSensor = new Sensor().with {
        it.name = "test-pressure1"
        it.type = "pressure"
        it.location = "base"
        it.id = 2
        return it
    }
    @Shared
    final Sensor testWindSensor = new Sensor().with {
        it.name = "test-wind1"
        it.type = "wind"
        it.location = "base"
        it.id = 3
        return it
    }

    @Shared
    final SensorInputRequest validTempRequest = new SensorInputRequest().with {
        it.sensorType = "temperature"
        it.sensorLocation = "test"
        it.sensorName = "test-temp1"
        it.metrics = new HashMap<String,String>().with {
            it.put("temperature","65.3F")
            it.put("humidity", "50.0%")
            return it
        }
        return it
    }

    @Shared
    final SensorInputRequest validPressureRequest = new SensorInputRequest().with {
        it.sensorType = "pressure"
        it.sensorLocation = "test"
        it.sensorName = "test-pressure1"
        it.metrics = new HashMap<String,String>().with {
            it.put("pressure","455.36")
            it.put("unit", "bar")
            return it
        }
        return it
    }

    @Shared
    final SensorInputRequest ValidWindRequest = new SensorInputRequest().with {
        it.sensorType = "wind"
        it.sensorLocation = "test"
        it.sensorName = "test-wind1"
        it.metrics = new HashMap<String,String>().with {
            it.put("speed","65.35")
            it.put("unit", "mph")
            return it
        }
        return it
    }

    def setupSpec() {
    }

    def 'send metric data to be persisted, sensor not in database yet'() {
        given:
        SensorInputRequest request = validTempRequest
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorCollectionService service = new SensorCollectionService(mockedWeatherData, mockedSensors)
        when:
        service.saveSensorData(request)
        then:
        1 * mockedSensors.findByName("test-temp1") >> null
        1 * mockedSensors.save(_ as Sensor)
        1 * mockedWeatherData.saveAll(_ as List<WeatherMetric>) >> {
            args ->
                List<WeatherMetric> test = (List<WeatherMetric>)args[0]
                assert test.size() == 2
                assert test.first().name.equalsIgnoreCase("temperature")
                assert test.first().unitOfMeasure.equalsIgnoreCase("c")
                assert test.last().name.equalsIgnoreCase("humidity")
                assert test.last().unitOfMeasure.equalsIgnoreCase("%")
        }
    }

    def 'send metric data to be persisted, exception thrown on save of sensor'() {
        given:
        SensorInputRequest request = validTempRequest
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorCollectionService service = new SensorCollectionService(mockedWeatherData, mockedSensors)
        when:
        def response = service.saveSensorData(request)
        then:
        1 * mockedSensors.findByName("test-temp1") >> null
        1 * mockedSensors.save(_ as Sensor) >> { throw new Exception("test") }
        0 * mockedWeatherData.saveAll(_ as List<WeatherMetric>)
        !response.success
    }

    def 'send metric data to be persisted, exception thrown on save of metrics'() {
        given:
        SensorInputRequest request = validTempRequest
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorCollectionService service = new SensorCollectionService(mockedWeatherData, mockedSensors)
        when:
        def response = service.saveSensorData(request)
        then:
        1 * mockedSensors.findByName("test-temp1") >> null
        1 * mockedSensors.save(_ as Sensor)
        1 * mockedWeatherData.saveAll(_ as List<WeatherMetric>) >> { throw new Exception("test") }
        !response.success
    }

    def 'temp sensor exists, but missing metrics'() {
        given:
        SensorInputRequest request = new SensorInputRequest().with {
            it.sensorType = "temperature"
            it.sensorLocation = "test"
            it.sensorName = "test-temp1"
            it.metrics = new HashMap<String,String>()
            return it
        }
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorCollectionService service = new SensorCollectionService(mockedWeatherData, mockedSensors)
        when:
        def response = service.saveSensorData(request)
        then:
        0 * mockedSensors.findByName("test-temp1") >> testTempSensor
        0 * mockedWeatherData.saveAll(_ as List<WeatherMetric>)
        !response.success
        response.message.contains("metrics list is missing one or more required fields")
    }


    def 'send metric data to be persisted, pressure sensor not in database yet'() {
        given:
        SensorInputRequest request = validPressureRequest
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorCollectionService service = new SensorCollectionService(mockedWeatherData, mockedSensors)
        when:
        service.saveSensorData(request)
        then:
        1 * mockedSensors.findByName("test-pressure1") >> null
        1 * mockedSensors.save(_ as Sensor)
        1 * mockedWeatherData.saveAll(_ as List<WeatherMetric>) >> {
            args ->
                List<WeatherMetric> test = (List<WeatherMetric>)args[0]
                assert test.size() == 1
                assert test.first().name.equalsIgnoreCase("pressure")
                assert test.first().unitOfMeasure.equalsIgnoreCase("bar")
        }
    }

    def 'send metric data to be persisted, exception thrown on save of pressure sensor'() {
        given:
        SensorInputRequest request = validPressureRequest
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorCollectionService service = new SensorCollectionService(mockedWeatherData, mockedSensors)
        when:
        def response = service.saveSensorData(request)
        then:
        1 * mockedSensors.findByName("test-pressure1") >> null
        1 * mockedSensors.save(_ as Sensor) >> { throw new Exception("test") }
        0 * mockedWeatherData.saveAll(_ as List<WeatherMetric>)
        !response.success
    }

    def 'send metric data to be persisted, exception thrown on save of pressure metrics'() {
        given:
        SensorInputRequest request = validPressureRequest
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorCollectionService service = new SensorCollectionService(mockedWeatherData, mockedSensors)
        when:
        def response = service.saveSensorData(request)
        then:
        1 * mockedSensors.findByName("test-pressure1") >> null
        1 * mockedSensors.save(_ as Sensor)
        1 * mockedWeatherData.saveAll(_ as List<WeatherMetric>) >> { throw new Exception("test") }
        !response.success
    }

    def 'pressure sensor exists, but wrong unit of measure'() {
        given:
        SensorInputRequest request = new SensorInputRequest().with {
            it.sensorType = "pressure"
            it.sensorLocation = "test"
            it.sensorName = "test-pressure1"
            it.metrics = new HashMap<String,String>().with {
                it.put("pressure","455.36")
                it.put("unit", "psi")
                return it
            }
            return it
        }
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorCollectionService service = new SensorCollectionService(mockedWeatherData, mockedSensors)
        when:
        def response = service.saveSensorData(request)
        then:
        0 * mockedSensors.findByName("test-pressure1") >> testPressureSensor
        0 * mockedWeatherData.saveAll(_ as List<WeatherMetric>)
        !response.success
        response.message.contains("currently only bar is a supported unit")
    }

    def 'pressure sensor exists, but missing metrics'() {
        given:
        SensorInputRequest request = new SensorInputRequest().with {
            it.sensorType = "pressure"
            it.sensorLocation = "test"
            it.sensorName = "test-pressure1"
            it.metrics = new HashMap<String,String>()
            return it
        }
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorCollectionService service = new SensorCollectionService(mockedWeatherData, mockedSensors)
        when:
        def response = service.saveSensorData(request)
        then:
        0 * mockedSensors.findByName("test-pressure1") >> testPressureSensor
        0 * mockedWeatherData.saveAll(_ as List<WeatherMetric>)
        !response.success
        response.message.contains("metrics list is missing one or more required fields")
    }

    def 'send metric data to be persisted, wind speed sensor not in database yet'() {
        given:
        SensorInputRequest request = validWindRequest
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorCollectionService service = new SensorCollectionService(mockedWeatherData, mockedSensors)
        when:
        service.saveSensorData(request)
        then:
        1 * mockedSensors.findByName("test-wind1") >> null
        1 * mockedSensors.save(_ as Sensor)
        1 * mockedWeatherData.saveAll(_ as List<WeatherMetric>) >> {
            args ->
                List<WeatherMetric> test = (List<WeatherMetric>)args[0]
                assert test.size() == 1
                assert test.first().name.equalsIgnoreCase("wind speed")
                assert test.first().unitOfMeasure.equalsIgnoreCase("mph")
        }
    }

    def 'send metric data to be persisted, exception thrown on save of wind sensor'() {
        given:
        SensorInputRequest request = validWindRequest
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorCollectionService service = new SensorCollectionService(mockedWeatherData, mockedSensors)
        when:
        def response = service.saveSensorData(request)
        then:
        1 * mockedSensors.findByName("test-wind1") >> null
        1 * mockedSensors.save(_ as Sensor) >> { throw new Exception("test") }
        0 * mockedWeatherData.saveAll(_ as List<WeatherMetric>)
        !response.success
    }

    def 'send metric data to be persisted, exception thrown on save of pressure metrics'() {
        given:
        SensorInputRequest request = validWindRequest
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorCollectionService service = new SensorCollectionService(mockedWeatherData, mockedSensors)
        when:
        def response = service.saveSensorData(request)
        then:
        1 * mockedSensors.findByName("test-wind1") >> null
        1 * mockedSensors.save(_ as Sensor)
        1 * mockedWeatherData.saveAll(_ as List<WeatherMetric>) >> { throw new Exception("test") }
        !response.success
    }

    def 'wind sensor exists, but wrong unit'() {
        given:
        SensorInputRequest request = new SensorInputRequest().with {
            it.sensorType = "wind"
            it.sensorLocation = "test"
            it.sensorName = "test-wind1"
            it.metrics = new HashMap<String,String>().with {
                it.put("speed","65.35")
                it.put("unit", "kph")
                return it
            }
            return it
        }
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorCollectionService service = new SensorCollectionService(mockedWeatherData, mockedSensors)
        when:
        def response = service.saveSensorData(request)
        then:
        0 * mockedSensors.findByName("test-pressure1") >> testWindSensor
        0 * mockedWeatherData.saveAll(_ as List<WeatherMetric>)
        !response.success
        response.message.contains("currently only mph is a supported unit")
    }

    def 'wind sensor exists, but missing metrics'() {
        given:
        SensorInputRequest request = new SensorInputRequest().with {
            it.sensorType = "wind"
            it.sensorLocation = "test"
            it.sensorName = "test-wind1"
            it.metrics = new HashMap<String,String>()
            return it
        }
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorCollectionService service = new SensorCollectionService(mockedWeatherData, mockedSensors)
        when:
        def response = service.saveSensorData(request)
        then:
        0 * mockedSensors.findByName("test-pressure1") >> testWindSensor
        0 * mockedWeatherData.saveAll(_ as List<WeatherMetric>)
        !response.success
        response.message.contains("metrics list is missing one or more required fields")
    }

    def 'unsupported sensor type'() {
        given:
        SensorInputRequest request = new SensorInputRequest().with {
            it.sensorType = "air quality"
            it.sensorLocation = "test"
            it.sensorName = "test-wind1"
            it.metrics = new HashMap<String,String>()
            return it
        }
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorCollectionService service = new SensorCollectionService(mockedWeatherData, mockedSensors)
        when:
        def response = service.saveSensorData(request)
        then:
        0 * mockedSensors.findByName("test-pressure1") >> testWindSensor
        0 * mockedWeatherData.saveAll(_ as List<WeatherMetric>)
        !response.success
        response.message.contains("sensor type ${request.sensorType} is not defined. file a feature request to have this sensor type added")
    }
}
