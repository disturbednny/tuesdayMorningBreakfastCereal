package org.disturbednny.cereal.cerealSpringBootRest.db.model

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(schema = "station", name = "sensors" )
class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id
    public String name
    public String type
    public String location

    @OneToMany(mappedBy = "sensor")
    private List<WeatherMetric> metrics
}
