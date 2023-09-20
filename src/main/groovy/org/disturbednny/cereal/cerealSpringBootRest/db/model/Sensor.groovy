package org.disturbednny.cereal.cerealSpringBootRest.db.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.disturbednny.cereal.cerealSpringBootRest.db.WeatherData

@Entity
@Table(schema = "default", name = "sensors" )
class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id
    public String name
    public String type
    public String location

    @OneToMany(mappedBy = "sensor")
    private List<WeatherData> metrics
}
