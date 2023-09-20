package org.disturbednny.cereal.cerealSpringBootRest.db

import org.disturbednny.cereal.cerealSpringBootRest.db.model.Sensor
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface Sensors extends JpaRepository<Sensor,Long> {


    Sensor findByName(String name)

    @Query("select s from Sensor s where s.type = ?1")
    List<Sensor> findByType(String sensorType)

    @Query("select s from Sensor s where s.location = ?1")
    List<Sensor> findByLocation(String location)

}