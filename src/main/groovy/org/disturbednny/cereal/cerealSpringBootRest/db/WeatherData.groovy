package org.disturbednny.cereal.cerealSpringBootRest.db

import org.disturbednny.cereal.cerealSpringBootRest.db.model.WeatherMetric
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface WeatherData extends CrudRepository<WeatherMetric,Long> {

}