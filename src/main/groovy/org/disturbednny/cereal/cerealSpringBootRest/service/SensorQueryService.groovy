package org.disturbednny.cereal.cerealSpringBootRest.service

import org.disturbednny.cereal.cerealSpringBootRest.db.Sensors
import org.disturbednny.cereal.cerealSpringBootRest.db.WeatherData
import org.disturbednny.cereal.cerealSpringBootRest.db.model.WeatherMetric
import org.disturbednny.cereal.cerealSpringBootRest.domain.SensorQueryRequest
import org.disturbednny.cereal.cerealSpringBootRest.domain.SensorQueryResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SensorQueryService {
    private static final Logger log = LoggerFactory.getLogger(SensorQueryService.class)

    private final WeatherData weatherData
    private final Sensors sensors
    @Autowired
    SensorQueryService(WeatherData data, Sensors sensors) {
        this.weatherData = data
        this.sensors = sensors
    }

    SensorQueryResponse processQuery(SensorQueryRequest request) {


    }

    def calculateAverageOfMetrics (List<WeatherMetric> metrics) {

    }

    def getMinOfMetrics(List<WeatherMetric> metrics) {

    }

    def getMaxOfMetrics(List<WeatherMetric> metrics) {

    }

    def getSumOfMetrics(List<WeatherMetric> metrics) {

    }
}
