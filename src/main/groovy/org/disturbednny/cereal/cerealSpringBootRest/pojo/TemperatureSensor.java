package org.disturbednny.cereal.cerealSpringBootRest.pojo;

import com.fasterxml.jackson.annotation.JsonSetter;
import org.codehaus.groovy.runtime.StringGroovyMethods;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemperatureSensor extends Sensor {
    private static final Pattern temperaturePattern = Pattern.compile("(?<temp>([+-]?\\d+(\\.\\d+)*))\\s?(?<unit>[CcFf])");
    private static final Pattern humidityPattern = Pattern.compile("(?<humidity>([+-]?\\d+(\\.\\d+)*))\\s?(?<unit>[%])");

    private Double temperature;
    private String temperatureUoM;
    private Double relativeHumidity;
    public TemperatureSensor(String name, String location) {

        super(name, "temperature", location);
    }

    @JsonSetter("temperature")
    public void setTemperature(String value) {
        // normalize to all celcius for ease of computation later down the road
        Matcher tempMatcher = temperaturePattern.matcher(value);
        if(tempMatcher.matches()) {
            Double tmp = Double.parseDouble(tempMatcher.group("temp"));
            temperature = StringGroovyMethods.containsIgnoreCase(tempMatcher.group("unit").toLowerCase(), "c") ? tmp : convertFahrenheitToCelcius(tmp);
            temperatureUoM = "c";
        }
        else {
            throw new IllegalArgumentException("Passed in value was not able to be parsed as a temperature");
        }
    }

    public Double getTemperature() {
        return temperature;
    }

    public Double convertFahrenheitToCelcius(Double value) {
        return (value - 32) * (5 / 9.0);
    }

    public Double convertCelciusToFahrenheit(Double value) {
        return (value * (9.0 / 5)) + 32;
    }

    public String getTemperatureUoM() {
        return temperatureUoM;
    }

    public void setTemperatureUoM(String temperatureUoM) {
        this.temperatureUoM = temperatureUoM;
    }

    public Double getRelativeHumidity() {
        return relativeHumidity;
    }

    public void setRelativeHumidity(String relativeHumidity) {

        Matcher humidMatcher = humidityPattern.matcher(relativeHumidity);
        if(humidMatcher.matches()) {
            this.relativeHumidity = Double.parseDouble(humidMatcher.group("humidity"));
        }
    }
    public void setRelativeHumidity(Double relativeHumidity) {

            this.relativeHumidity = relativeHumidity;
    }
}
