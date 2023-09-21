package org.disturbednny.cereal.cerealSpringBootRest.db.model;

import jakarta.persistence.*;

import java.time.ZonedDateTime;

@Entity
@Table(schema = "station", name = "metrics")
public class WeatherMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @ManyToOne
    @JoinColumn(name = "sensorid")
    private Sensor sensor;
    @Column(name = "name")
    private String name;
    @Column(name = "metricvalue")
    private Double value;
    @Column(name = "unitofmeasure")
    private String unitOfMeasure;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "datetime")
    private ZonedDateTime dateTime;

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public String getName() {
        return name;
    }

    public void setName(String metricName) {
        this.name = metricName;
    }


    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public ZonedDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(ZonedDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getPrettyValue() {
        return String.format("%.2f %s", value, unitOfMeasure);
    }

}
