package org.disturbednny.cereal.cerealSpringBootRest.pojo;

public class WindSensor extends Sensor {
    private Double speed;
    private String UoM;

    // TODO: add normalizing to a specific Unit of measure for wind speed. for now just use miles per hour

    public WindSensor(String name, String location) {
        super(name, "wind", location);
    }

    public void setSpeed(double value){
        this.speed = value;
    }

    public void setUoM(String value) {
        this.UoM = value;
    }

    public Double getSpeed() {
        return speed;
    }

    public String getUoM() {
        return UoM;
    }
}
