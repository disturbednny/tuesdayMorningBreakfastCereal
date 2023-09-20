package org.disturbednny.cereal.cerealSpringBootRest.db.model

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType

import java.time.ZonedDateTime

@Entity
@Table(schema="default", name = "weatherMetrics")
class WeatherMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id

    @ManyToOne
    @JoinColumn(name="sensorId")
    private Sensor sensor

    private String metricName
    private String metricType
    private float value
    private String unitOfMeasure
    @Temporal(TemporalType.TIMESTAMP)
    private ZonedDateTime dateTime

}
