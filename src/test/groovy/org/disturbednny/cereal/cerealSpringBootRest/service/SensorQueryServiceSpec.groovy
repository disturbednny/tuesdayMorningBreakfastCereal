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
    List<WeatherMetric> pressureMetrics
    @Shared
    List<WeatherMetric> windSpeedMetrics

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
    def setupSpec() {
        tempMetrics = new ArrayList<>()
        humidityMetrics = new ArrayList<>()
        pressureMetrics = new ArrayList<>()
        windSpeedMetrics = new ArrayList<>()
        tempMetrics.add(new WeatherMetric().with {
            name = "temperature"
            value = 10.0
            unitOfMeasure = "c"
            dateTime = ZonedDateTime.parse("2023-09-18T07:45:00Z")
            sensor = testTempSensor
            return it
        })
        tempMetrics.add(new WeatherMetric().with {
            name = "temperature"
            value = 20.0
            unitOfMeasure = "c"
            dateTime = ZonedDateTime.parse("2023-09-18T13:45:00Z")
            sensor = testTempSensor
            return it
        })
        humidityMetrics.add(new WeatherMetric().with {
            name = "humidity"
            value = 75.0
            unitOfMeasure = "%"
            dateTime = ZonedDateTime.parse("2023-09-18T13:45:00Z")
            sensor = testTempSensor
            return it
        })
        humidityMetrics.add(new WeatherMetric().with {
            name = "humidity"
            value = 25.0
            unitOfMeasure = "%"
            dateTime = ZonedDateTime.parse("2023-09-18T13:45:00Z")
            sensor = testTempSensor
            return it
        })
        windSpeedMetrics.add(new WeatherMetric().with {
            name = "wind"
            value = 25.0
            unitOfMeasure = "mph"
            dateTime = ZonedDateTime.parse("2023-09-18T13:45:00Z")
            sensor = testWindSensor
            return it
        })
        pressureMetrics.add(new WeatherMetric().with {
            name = "pressure"
            value = 500.56
            unitOfMeasure = "bar"
            dateTime = ZonedDateTime.parse("2023-09-18T13:45:00Z")
            sensor = testPressureSensor
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
        1 * mockedSensors.findByName("test-temp-1") >> testTempSensor
        1 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "temperature", _ as ZonedDateTime, _ as ZonedDateTime) >> tempMetrics
        1 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "humidity", _ as ZonedDateTime, _ as ZonedDateTime) >> humidityMetrics
    }

    def 'sensor not found'() {
        given:
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorQueryRequest request = new SensorQueryRequest().with{
            query = "give me average temperature and humidity for sensor test-temp-2 for the last week"
            return it
        }

        SensorQueryService service = new SensorQueryService(mockedWeatherData, mockedSensors)

        when:
        service.processQuery(request)
        then:
        1 * mockedSensors.findByName("test-temp-2") >> null
        0 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "temperature", _ as ZonedDateTime, _ as ZonedDateTime) >> tempMetrics
        0 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "humidity", _ as ZonedDateTime, _ as ZonedDateTime) >> humidityMetrics
    }

    def 'no sensors defined, so grab all sensors'() {
        given:
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorQueryRequest request = new SensorQueryRequest().with{
            query = "give me average temperature and humidity for the last week"
            return it
        }

        SensorQueryService service = new SensorQueryService(mockedWeatherData, mockedSensors)
        List<Sensor> testSensorList = new ArrayList<Sensor>()
        testSensorList.add(testTempSensor)

        when:
        service.processQuery(request)
        then:
        1 * mockedSensors.findAll() >> testSensorList
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
        1 * mockedSensors.findByName("test-temp-1") >> testTempSensor
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
        1 * mockedSensors.findByName("test-temp-1") >> testTempSensor
        1 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "temperature", _ as ZonedDateTime, _ as ZonedDateTime) >> tempMetrics
        1 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "humidity", _ as ZonedDateTime, _ as ZonedDateTime) >> humidityMetrics
    }

    def 'one temp sensor, min,max,avg, last day'() {
        given:
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorQueryRequest request = new SensorQueryRequest().with{
            query = "give me max min average temperature and humidity for sensor test-temp-1 for the last day"
            return it
        }

        SensorQueryService service = new SensorQueryService(mockedWeatherData, mockedSensors)

        when:
        SensorQueryResponse result = service.processQuery(request)
        then:
        1 * mockedSensors.findByName("test-temp-1") >> testTempSensor
        1 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "temperature", _ as ZonedDateTime, _ as ZonedDateTime) >> tempMetrics
        1 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "humidity", _ as ZonedDateTime, _ as ZonedDateTime) >> humidityMetrics
        assert result.message.containsIgnoreCase("min")
        assert result.message.containsIgnoreCase("max")
        assert result.message.containsIgnoreCase("avg")
    }

    def 'one temp sensor, min,max,avg, last 0 day, corrects to 1 day'() {
        given:
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorQueryRequest request = new SensorQueryRequest().with{
            query = "give me max min average temperature and humidity for sensor test-temp-1 for the last 0 days"
            return it
        }

        SensorQueryService service = new SensorQueryService(mockedWeatherData, mockedSensors)

        when:
        SensorQueryResponse result = service.processQuery(request)
        then:
        1 * mockedSensors.findByName("test-temp-1") >> testTempSensor
        1 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "temperature", _ as ZonedDateTime, _ as ZonedDateTime) >> tempMetrics
        1 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "humidity", _ as ZonedDateTime, _ as ZonedDateTime) >> humidityMetrics
        assert result.message.containsIgnoreCase("min")
        assert result.message.containsIgnoreCase("max")
        assert result.message.containsIgnoreCase("avg")
    }

    def 'one temp sensor, min,max,avg, last week'() {
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
        1 * mockedSensors.findByName("test-temp-1") >> testTempSensor
        1 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "temperature", _ as ZonedDateTime, _ as ZonedDateTime) >> tempMetrics
        1 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "humidity", _ as ZonedDateTime, _ as ZonedDateTime) >> humidityMetrics
        assert result.message.containsIgnoreCase("min")
        assert result.message.containsIgnoreCase("max")
        assert result.message.containsIgnoreCase("avg")
    }

    def 'one temp sensor, min,max,avg, last month'() {
        given:
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorQueryRequest request = new SensorQueryRequest().with{
            query = "give me max min average temperature and humidity for sensor test-temp-1 for the last month"
            return it
        }

        SensorQueryService service = new SensorQueryService(mockedWeatherData, mockedSensors)

        when:
        SensorQueryResponse result = service.processQuery(request)
        then:
        1 * mockedSensors.findByName("test-temp-1") >> testTempSensor
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
        1 * mockedSensors.findByName("test-temp-1") >> testTempSensor
        1 * mockedWeatherData.findFirstBySensorAndNameOrderByDateTimeDesc(_ as Sensor, "temperature") >> tempMetrics[0]
        1 * mockedWeatherData.findFirstBySensorAndNameOrderByDateTimeDesc(_ as Sensor, "humidity") >> humidityMetrics[0]

    }

    def 'one temp sensor, max, specific date not found'() {
        given:
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorQueryRequest request = new SensorQueryRequest().with{
            query = "give me max temperature and humidity for sensor test-temp-1 for 2023-09-01"
            return it
        }

        SensorQueryService service = new SensorQueryService(mockedWeatherData, mockedSensors)

        when:
        def result = service.processQuery(request)
        then:
        1 * mockedSensors.findByName("test-temp-1") >> testTempSensor
        1 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "temperature", _ as ZonedDateTime, _ as ZonedDateTime) >> null
        1 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "humidity", _ as ZonedDateTime, _ as ZonedDateTime) >> null
        result.message.containsIgnoreCase("no metrics for this time range")
    }

    def 'one temp sensor, max, specific date format error'() {
        given:
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorQueryRequest request = new SensorQueryRequest().with{
            query = "give me max temperature and humidity for sensor test-temp-1 for 2023-14-09"
            return it
        }

        SensorQueryService service = new SensorQueryService(mockedWeatherData, mockedSensors)

        when:
        def result = service.processQuery(request)
        then:
        1 * mockedSensors.findByName("test-temp-1") >> testTempSensor
        0 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "temperature", _ as ZonedDateTime, _ as ZonedDateTime)
        0 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "humidity", _ as ZonedDateTime, _ as ZonedDateTime)
        result.message.containsIgnoreCase("Unable to get begin and end dates.")
    }

    def 'one temp sensor, max, between date format error'() {
        given:
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorQueryRequest request = new SensorQueryRequest().with{
            query = "give me max temperature and humidity for sensor test-temp-1 between 2023-14-09 and 2023-09-24"
            return it
        }

        SensorQueryService service = new SensorQueryService(mockedWeatherData, mockedSensors)

        when:
        def result = service.processQuery(request)
        then:
        1 * mockedSensors.findByName("test-temp-1") >> testTempSensor
        0 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "temperature", _ as ZonedDateTime, _ as ZonedDateTime)
        0 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "humidity", _ as ZonedDateTime, _ as ZonedDateTime)
        result.message.containsIgnoreCase("Unable to get begin and end dates.")
    }

    def 'one temp sensor, max, temporal range over 31 days error'() {
        given:
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorQueryRequest request = new SensorQueryRequest().with{
            query = "give me max temperature and humidity for sensor test-temp-1 for the last 32 days"
            return it
        }

        SensorQueryService service = new SensorQueryService(mockedWeatherData, mockedSensors)

        when:
        def result = service.processQuery(request)
        then:
        1 * mockedSensors.findByName("test-temp-1") >> testTempSensor
        0 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "temperature", _ as ZonedDateTime, _ as ZonedDateTime)
        0 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "humidity", _ as ZonedDateTime, _ as ZonedDateTime)
        result.message.containsIgnoreCase("you can only request up to one month of data")
    }

    def 'one temp sensor, max, temporal range over 4 weeks error'() {
        given:
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorQueryRequest request = new SensorQueryRequest().with{
            query = "give me max temperature and humidity for sensor test-temp-1 for the last 5 weeks"
            return it
        }

        SensorQueryService service = new SensorQueryService(mockedWeatherData, mockedSensors)

        when:
        def result = service.processQuery(request)
        then:
        1 * mockedSensors.findByName("test-temp-1") >> testTempSensor
        0 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "temperature", _ as ZonedDateTime, _ as ZonedDateTime)
        0 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "humidity", _ as ZonedDateTime, _ as ZonedDateTime)
        result.message.containsIgnoreCase("you can only request up to one month of data")
    }

    def 'one temp sensor, max, temporal range over 1 month error'() {
        given:
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorQueryRequest request = new SensorQueryRequest().with{
            query = "give me max temperature and humidity for sensor test-temp-1 for the last 2 months"
            return it
        }

        SensorQueryService service = new SensorQueryService(mockedWeatherData, mockedSensors)

        when:
        def result = service.processQuery(request)
        then:
        1 * mockedSensors.findByName("test-temp-1") >> testTempSensor
        0 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "temperature", _ as ZonedDateTime, _ as ZonedDateTime)
        0 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "humidity", _ as ZonedDateTime, _ as ZonedDateTime)
        result.message.containsIgnoreCase("you can only request up to one month of data")
    }

    def 'all sensors all metrics between range'() {
        given:
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorQueryRequest request = new SensorQueryRequest().with{
            query = "give me max min average of all metrics between 2023-09-10 and 2023-09-22"
            return it
        }

        SensorQueryService service = new SensorQueryService(mockedWeatherData, mockedSensors)
        List<Sensor> testSensors = new ArrayList<Sensor>(Arrays.asList(testPressureSensor,testWindSensor,testTempSensor))
        testSensors
        when:
        def result = service.processQuery(request)
        then:
        1 * mockedSensors.findAll() >> testSensors
        1 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "temperature", _ as ZonedDateTime, _ as ZonedDateTime) >> tempMetrics
        1 * mockedWeatherData.findAllBySensorAndNameAndDateTimeBetween(_ as Sensor, "humidity", _ as ZonedDateTime, _ as ZonedDateTime) >> humidityMetrics
        1 * mockedWeatherData.findAllBySensorAndDateTimeBetween(testPressureSensor as Sensor, _ as ZonedDateTime, _ as ZonedDateTime) >> pressureMetrics
        1 * mockedWeatherData.findAllBySensorAndDateTimeBetween(testWindSensor as Sensor, _ as ZonedDateTime, _ as ZonedDateTime) >> windSpeedMetrics

        assert result.message.contains("test-temp1")
        assert result.message.contains("test-pressure1")
        assert result.message.contains("test-wind1")
        assert result.message.count("min") == 4
        assert result.message.count("max") == 4
        assert result.message.count("avg") == 4
    }
    def 'all sensors all metrics between range'() {
        given:
        WeatherData mockedWeatherData = Mock(WeatherData)
        Sensors mockedSensors = Mock(Sensors)
        SensorQueryRequest request = new SensorQueryRequest().with{
            query = "give min, max, average of all metrics"
            return it
        }

        SensorQueryService service = new SensorQueryService(mockedWeatherData, mockedSensors)
        List<Sensor> testSensors = new ArrayList<Sensor>(Arrays.asList(testPressureSensor,testWindSensor,testTempSensor))
        testSensors
        when:
        def result = service.processQuery(request)
        then:
        1 * mockedSensors.findAll() >> testSensors
        1 * mockedWeatherData.findFirstBySensorAndNameOrderByDateTimeDesc(_ as Sensor, "temperature") >> tempMetrics[0]
        1 * mockedWeatherData.findFirstBySensorAndNameOrderByDateTimeDesc(_ as Sensor, "humidity") >> humidityMetrics[0]
        1 * mockedWeatherData.findFirstBySensorOrderByDateTimeDesc(testPressureSensor as Sensor) >> pressureMetrics[0]
        1 * mockedWeatherData.findFirstBySensorOrderByDateTimeDesc(testWindSensor as Sensor) >> windSpeedMetrics[0]

        assert result.message.contains("test-temp1")
        assert result.message.contains("test-pressure1")
        assert result.message.contains("test-wind1")
        assert result.message.count("latest") == 4
    }


}
