package org.disturbednny.cereal.cerealSpringBootRest.pojo

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.JsonValue

class TemperatureSensor extends Sensor
{

    private Double temperature
    private String temperatureUoM
    private Double relativeHumidity

    /// sets the temperature, normalizing to celcius on deserialization.
    @JsonSetter("temperature")
    void setTemperature(Double value) {
                // normalize to all celcius for ease of computation later down the road
        temperature = temperatureUoM.containsIgnoreCase("c") ? value : convertFahrenheitToCelcius(value)
    }

    /// converts from fahrenheit to celcius
    Double convertFahrenheitToCelcius(Double value) {
        return (value - 32) * (5/9.0)
    }

    /// converts from celcius to fahrenheit
    Double convertCelciusToFahrenheit(Double value) {
        return (value * (9.0/5)) + 32
    }
}
