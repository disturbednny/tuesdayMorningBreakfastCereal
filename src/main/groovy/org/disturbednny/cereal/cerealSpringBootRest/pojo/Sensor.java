package org.disturbednny.cereal.cerealSpringBootRest.pojo;

public class Sensor {
    public Sensor(String name, String type, String location) {
        this.name = name;
        this.type = type;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getLocation() {
        return location;
    }

    private final String name;
    private final String type;
    private final String location;
}
