package org.disturbednny.cereal.cerealSpringBootRest.service

import org.disturbednny.cereal.cerealSpringBootRest.db.Sensors
import org.disturbednny.cereal.cerealSpringBootRest.db.WeatherData
import org.disturbednny.cereal.cerealSpringBootRest.db.model.Sensor
import org.disturbednny.cereal.cerealSpringBootRest.db.model.WeatherMetric
import org.disturbednny.cereal.cerealSpringBootRest.domain.SensorQueryRequest
import org.disturbednny.cereal.cerealSpringBootRest.domain.SensorQueryResponse
import spock.lang.Shared
import spock.lang.Specification

import java.time.ZonedDateTime

class SensorQueryServiceSpec extends Specification{
    @Shared
    List<WeatherMetric> tempMetrics

    @Shared
    List<WeatherMetric> humidityMetrics

    @Shared
    final Sensor testSensor = new Sensor().with {
        it.name = "test-temp1"
        it.type = "temperature"
        it.location = "base"
        it.id = 1
        return it
    }
    def setupSpec() {
        tempMetrics = new ArrayList<>()
        humidityMetrics = new ArrayList<>()
        tempMetrics.add(new WeatherMetric().with {
            name = "temperature"
            value = 10.0
            unitOfMeasure = "c"
            dateTime = ZonedDateTime.parse("2023-09-18T07:45:00Z")
            sensor = testSensor
            return it
        })
        tempMetrics.add(new WeatherMetric().with {
            name = "temperature"
            value = 20.0
            unitOfMeasure = "c"
            dateTime = ZonedDateTime.parse("2023-09-18T13:45:00Z")
            sensor = testSensor
            return it
        })
        humidityMetrics.add(new WeatherMetric().with {
            name = "humidity"
            value = 75.0
            unitOfMeasure = "%"
            dateTime = ZonedDateTime.parse("2023-09-18T13:45:00Z")
            sensor = testSensor
            return it
        })
        humidityMetrics.add(new WeatherMetric().with {
            name = "humidity"
            value = 25.0
            unitOfMeasure = "%"
            dateTime = ZonedDateTime.parse("2023-09-18T13:45:00Z")
            sensor = testSensor
            return it
        })
    }

    def 'one temp sensor, average'() {
        given:
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorQueryRequest request = new SensorQueryRequest().with{
            query = "give me average temperature and humidity for sensor test-temp-1 for the last week"
            return it
        }

        SensorQueryService service = new SensorQueryService(mockedWeatherData, mockedSensors)

        when:
        service.processQuery(request)
        then:
        1 * mockedSensors.findByName("test-temp-1") >> testSensor
        1 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "temperature", _ as ZonedDateTime, _ as ZonedDateTime) >> tempMetrics
        1 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "humidity", _ as ZonedDateTime, _ as ZonedDateTime) >> humidityMetrics
    }

    def 'one temp sensor, min'() {
        given:
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorQueryRequest request = new SensorQueryRequest().with{
            query = "give me min temperature and humidity for sensor test-temp-1 for the last week"
            return it
        }

        SensorQueryService service = new SensorQueryService(mockedWeatherData, mockedSensors)

        when:
        service.processQuery(request)
        then:
        1 * mockedSensors.findByName("test-temp-1") >> testSensor
        1 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "temperature", _ as ZonedDateTime, _ as ZonedDateTime) >> tempMetrics
        1 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "humidity", _ as ZonedDateTime, _ as ZonedDateTime) >> humidityMetrics
    }

    def 'one temp sensor, max'() {
        given:
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorQueryRequest request = new SensorQueryRequest().with{
            query = "give me max temperature and humidity for sensor test-temp-1 for the last week"
            return it
        }

        SensorQueryService service = new SensorQueryService(mockedWeatherData, mockedSensors)

        when:
        service.processQuery(request)
        then:
        1 * mockedSensors.findByName("test-temp-1") >> testSensor
        1 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "temperature", _ as ZonedDateTime, _ as ZonedDateTime) >> tempMetrics
        1 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "humidity", _ as ZonedDateTime, _ as ZonedDateTime) >> humidityMetrics
    }

    def 'one temp sensor, min,max,avg'() {
        given:
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorQueryRequest request = new SensorQueryRequest().with{
            query = "give me max min average temperature and humidity for sensor test-temp-1 for the last week"
            return it
        }

        SensorQueryService service = new SensorQueryService(mockedWeatherData, mockedSensors)

        when:
        SensorQueryResponse result = service.processQuery(request)
        then:
        1 * mockedSensors.findByName("test-temp-1") >> testSensor
        1 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "temperature", _ as ZonedDateTime, _ as ZonedDateTime) >> tempMetrics
        1 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "humidity", _ as ZonedDateTime, _ as ZonedDateTime) >> humidityMetrics
        assert result.message.containsIgnoreCase("min")
        assert result.message.containsIgnoreCase("max")
        assert result.message.containsIgnoreCase("avg")
    }

    def 'one temp sensor, latest'() {
        given:
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorQueryRequest request = new SensorQueryRequest().with{
            query = "give me latest temperature and humidity for sensor test-temp-1"
            return it
        }

        SensorQueryService service = new SensorQueryService(mockedWeatherData, mockedSensors)

        when:
        SensorQueryResponse result = service.processQuery(request)
        then:
        1 * mockedSensors.findByName("test-temp-1") >> testSensor
        1 * mockedWeatherData.findFirstBySensorAndNameOrderByDateTimeDesc(_ as Sensor, "temperature") >> tempMetrics[0]
        1 * mockedWeatherData.findFirstBySensorAndNameOrderByDateTimeDesc(_ as Sensor, "humidity") >> humidityMetrics[0]

    }
}
