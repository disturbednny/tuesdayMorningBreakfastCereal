package org.disturbednny.cereal.cerealSpringBootRest.db

import org.disturbednny.cereal.cerealSpringBootRest.db.model.Sensor
import org.disturbednny.cereal.cerealSpringBootRest.db.model.WeatherMetric
import org.springframework.data.jpa.repository.JpaRepository

import org.springframework.stereotype.Repository

import java.time.ZonedDateTime

@Repository
interface WeatherData extends JpaRepository<WeatherMetric,Long> {



    List<WeatherMetric> findAllBySensor(Sensor sensor)

    WeatherMetric findFirstBySensorOrderByDateTimeDesc(Sensor sensor)

    List<WeatherMetric> findAllBySensorAndDateTimeBetween(Sensor sensor, ZonedDateTime start, ZonedDateTime end)
}