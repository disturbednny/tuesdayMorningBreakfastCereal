package org.disturbednny.cereal.cerealSpringBootRest.service

import org.disturbednny.cereal.cerealSpringBootRest.db.WeatherData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SensorQueryService {
    private static final Logger log = LoggerFactory.getLogger(SensorQueryService.class)

    private final WeatherData weatherData
    @Autowired
    SensorQueryService(WeatherData data) {
    this.weatherData = data
    }


}
