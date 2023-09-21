package org.disturbednny.cereal.cerealSpringBootRest.service

import org.disturbednny.cereal.cerealSpringBootRest.db.Sensors
import org.disturbednny.cereal.cerealSpringBootRest.db.WeatherData
import org.disturbednny.cereal.cerealSpringBootRest.db.model.Sensor
import org.disturbednny.cereal.cerealSpringBootRest.db.model.WeatherMetric
import org.disturbednny.cereal.cerealSpringBootRest.domain.SensorInputRequest
import spock.lang.Specification

class SensorCollectionServiceSpec extends Specification {

    def setupSpec() {

    }

    def 'send metric data to be persisted, sensor not in database yet'() {
        given:
        SensorInputRequest request = new SensorInputRequest().with {
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
}
