package org.disturbednny.cereal.cerealSpringBootRest.domain

class SensorInputRequest {
    String sensorName
    String sensorType
    String sensorLocation
    Map<String,String> metrics
}
