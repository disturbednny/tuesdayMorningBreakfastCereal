package org.disturbednny.cereal.cerealSpringBootRest.pojo

import spock.lang.Specification

class TemperatureSensorSpec extends Specification{

    def 'test the regex for setting temperature. ensure unit of measure is always c because of normalizing to celcius'() {
        given:
        String value = "95F"
        Double expectedValue = 35.0
        TemperatureSensor sensor = new TemperatureSensor("testTemp", "Home")
        when:
        sensor.temperature = value

        then:
        expectedValue == sensor.temperature

    }
}
