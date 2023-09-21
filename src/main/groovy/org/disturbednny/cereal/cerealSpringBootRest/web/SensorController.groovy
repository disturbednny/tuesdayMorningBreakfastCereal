package org.disturbednny.cereal.cerealSpringBootRest.web

import com.fasterxml.jackson.databind.ObjectMapper
import org.disturbednny.cereal.cerealSpringBootRest.domain.SensorInputRequest
import org.disturbednny.cereal.cerealSpringBootRest.domain.SensorInputResponse
import org.disturbednny.cereal.cerealSpringBootRest.domain.SensorQueryRequest
import org.disturbednny.cereal.cerealSpringBootRest.domain.SensorQueryResponse
import org.disturbednny.cereal.cerealSpringBootRest.service.SensorCollectionService
import org.disturbednny.cereal.cerealSpringBootRest.service.SensorQueryService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/weather")
class SensorController {

    private static final Logger log = LoggerFactory.getLogger(SensorController.class)
    private final SensorQueryService queryService
    private final SensorCollectionService collectionService

    @Autowired
    ObjectMapper objectMapper


    @Autowired
    public SensorController(SensorCollectionService collectionService, SensorQueryService queryService) {
        this.collectionService = collectionService
        this.queryService = queryService
    }

    @RequestMapping(method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<SensorInputResponse> processSensorData(@RequestBody String requestJson) {
        try {
            SensorInputRequest request  = objectMapper.readValue(requestJson, SensorInputRequest.class)
            SensorInputResponse result = collectionService.saveSensorData(request)
            ResponseEntity<SensorInputResponse>.ok(result)
        }
        catch(Exception exception) {
            SensorInputResponse result = new SensorInputResponse()
            result.message = "Caught exception ${exception.message} when processing request"
            log.error(result.message)
            ResponseEntity.internalServerError().body(result)
        }
    }

    @RequestMapping(method = RequestMethod.GET, consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<SensorQueryResponse> getWeatherData(@RequestBody String requestJson) {
        try {
            SensorQueryRequest request = objectMapper.readValue(requestJson, SensorQueryRequest.class)
            SensorQueryResponse response = queryService.processQuery(request)
            ResponseEntity<SensorQueryResponse>.ok(response)
        }
        catch (Exception exception) {
            log.error("Caught exception ${exception.message} when processing request")
            SensorQueryResponse response = new SensorQueryResponse()

            ResponseEntity<SensorQueryResponse>.internalServerError().body(response)
        }

    }
}
