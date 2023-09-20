package org.disturbednny.cereal.cerealSpringBootRest.pojo

class Sensor {

    private String name
    private String type
    private String location

    Sensor(String name, String type, String location) {
        this.name = name
        this.type = type
        this.location = location
    }

    String getName() {
        return name
    }

    String getType() {
        return type
    }

    String getLocation() {
        return location
    }
}
