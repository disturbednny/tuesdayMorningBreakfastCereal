package org.disturbednny.cereal.cerealSpringBootRest.pojo;

public class PressureSensor extends Sensor {
    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public String getUoM() {
        return UoM;
    }

    public void setUoM(String uoM) {
        UoM = uoM;
    }

    private double pressure;
    private String UoM;


    // TODO: normalize on UoM to be sent to the database. for now we are only going to use bar

    public PressureSensor(String name, String location) {
        super(name, "pressure", location);
    }
}
