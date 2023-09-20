package org.disturbednny.cereal.cerealSpringBootRest.pojo

class PressureSensor extends Sensor {
    private double pressure
    private String UoM

    // TODO: normalize on UoM to be sent to the database. for now we are only going to use bar
}
