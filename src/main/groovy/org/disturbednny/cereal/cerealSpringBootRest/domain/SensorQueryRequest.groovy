package org.disturbednny.cereal.cerealSpringBootRest.domain

class SensorQueryRequest implements Serializable {
    String statisticType
    String metrics
    String sensors
    // for this, we are only looking for 1 and 30 days. future expansion to include potential begin / end date
    int daysBack
}
