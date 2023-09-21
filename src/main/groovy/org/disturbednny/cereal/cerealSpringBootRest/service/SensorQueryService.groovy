package org.disturbednny.cereal.cerealSpringBootRest.service

import org.apache.commons.text.TextStringBuilder
import org.disturbednny.cereal.cerealSpringBootRest.db.Sensors
import org.disturbednny.cereal.cerealSpringBootRest.db.WeatherData
import org.disturbednny.cereal.cerealSpringBootRest.db.model.Sensor
import org.disturbednny.cereal.cerealSpringBootRest.db.model.WeatherMetric
import org.disturbednny.cereal.cerealSpringBootRest.domain.SensorQueryRequest
import org.disturbednny.cereal.cerealSpringBootRest.domain.SensorQueryResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.regex.Matcher
import java.util.regex.Pattern

@Service
class SensorQueryService {
    private static final Logger log = LoggerFactory.getLogger(SensorQueryService.class)
    private static final Pattern averagePattern = Pattern.compile("([Aa]verage|[aA]vg)")
    private static final Pattern minPattern = Pattern.compile("([mM]inimum|[Mm]in)")
    private static final Pattern sensorPattern = Pattern.compile("(?i)(sensor (?<sensorName>\\S+))")
    private static final Pattern maxPattern = Pattern.compile("([mM]aximum|[Mm]ax)")
    private static final Pattern temperaturePattern = Pattern.compile("(?i)(temperature|temp)")
    private static final Pattern allMetricPattern = Pattern.compile("(?i)(all metrics)")
    private static final Pattern humidityPattern = Pattern.compile("(?i)(humidity)")
    private static final Pattern windSpeedPattern = Pattern.compile("(?i)(wind speed)")
    private static final Pattern pressurePattern = Pattern.compile("(?i)((atmospheric)?( )?pressure)")
    private static final Pattern temporalPattern = Pattern.compile("(?i)((?<temporal>last|past)( )(?<number>\\d+)?( )?((?<time>(week|month|day))(s)?))")
    private static final Pattern betweenPattern = Pattern.compile("(?i)((?i)(?<length>between|from|)( )?(?<range>(?<begin>(\\d?\\d/\\d?\\d/\\d\\d(\\d\\d)?)) and (?<end>(\\d?\\d/\\d?\\d/\\d\\d(\\d\\d)?))))")
    private static final Pattern forSpecificDatePattern = Pattern.compile("(?i)(?<date>(\\d?\\d/\\d\\d?/\\d\\d(\\d\\d)?))")



    private final WeatherData weatherData
    private final Sensors sensors
    @Autowired
    SensorQueryService(WeatherData data, Sensors sensors) {
        this.weatherData = data
        this.sensors = sensors
    }

    SensorQueryResponse processQuery(SensorQueryRequest request) {
        log.debug("starting processing of query")
        SensorQueryResponse response = new SensorQueryResponse()
        ZonedDateTime begin
        ZonedDateTime end
        String query = request.query
        Matcher sensorMatcher = sensorPattern.matcher(query)
        Matcher temporalMatcher = temporalPattern.matcher(query)
        Matcher betweenMatcher = betweenPattern.matcher(query)
        Matcher specificDateMatcher = forSpecificDatePattern.matcher(query)

        List<Sensor> sensorList = new ArrayList<>()

        // grab the sensor name first, if we don't have the sensor, no sense in continuing

        while(sensorMatcher.find())
        {
            Sensor sensor
            String sensorName = sensorMatcher.group("sensorName")
            sensor = sensors.findByName(sensorName)
            if(sensor == null){
                response.message = "unable to find sensor by this name in the database. name passed in was ${sensorName}"
                log.error(response.message)
                return response
            }
            sensorList.add(sensor)
        }

        //if no sensors were defined, assume all
        if(sensorList.size() == 0) {
            sensorList = sensors.findAll()
        }

        //now, get the date or time range, we match for between first, then by words, then finally by a specific date. if none match, just retrieve the latest

        if(betweenMatcher.find()) {
            try {
                begin = ZonedDateTime.parse(betweenMatcher.group("begin"))
                end = ZonedDateTime.parse(betweenMatcher.group("end"))
            }
            catch (Exception ex) {
                response.message = "Unable to get begin and end dates. check your formatting and try again"
                log.error(response.message)
                return response
            }
            begin = begin.truncatedTo(ChronoUnit.DAYS)
            end = end.truncatedTo(ChronoUnit.DAYS).plusDays(1)

        }
        else if(temporalMatcher.find()) {
            int numberOfTemporal = 1
            String temporalUnit
            if(temporalMatcher.group("number") != null)
                numberOfTemporal = Integer.parseInt(temporalMatcher.group("number"))

            //if the number of temporal is less than 1, set to 1
            if(numberOfTemporal < 1)
                numberOfTemporal = 1

            temporalUnit = temporalMatcher.group("time").toLowerCase()
            switch (temporalUnit) {
                case "day": {
                    if(numberOfTemporal > 31) {
                        response.message = "you can only request up to one month of data. please reduce your date range or your request"
                        log.warn(response.message)
                        return response
                    }
                    begin = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).minusDays(numberOfTemporal)
                    end = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).plusDays(1)
                    break
                }
                case "week": {
                    if(numberOfTemporal > 4) {
                        response.message = "you can only request up to one month of data. please reduce your date range or your request"
                        log.warn(response.message)
                        return response
                    }
                    begin = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).minusDays(numberOfTemporal * 7) //multiply by 7 for number of days in week.
                    end = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).plusDays(1)
                    break

                }
                case "month": {
                    if(numberOfTemporal > 1) {
                        response.message = "you can only request up to one month of data. please reduce your date range or your request"
                        log.warn(response.message)
                        return response
                    }
                    begin = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).minusMonths(numberOfTemporal) //multiply by 7 for number of days in week.
                    end = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).plusDays(1)
                    break
                }
                default:
                    response.message = "temporal unit not supported at this time. file a feature request"
                    log.warn(response.message)
                    break
            }
        }
        else if(specificDateMatcher.find()) {
            try {
                begin = ZonedDateTime.parse(specificDateMatcher.group("date"))
                end = ZonedDateTime.parse(specificDateMatcher.group("date"))
            }
            catch (Exception ex) {
                response.message = "Unable to get begin and end dates. check your formatting and try again"
                log.error(response.message)
                return response
            }

            //now make sure we start at beginning of the day and end at the following midnight
            begin = begin.truncatedTo(ChronoUnit.DAYS)
            end = end.truncatedTo(ChronoUnit.DAYS).plusDays(1)
        }

        // now that we have our begin and end times, lets look at metrics.
        //now, pass the query along with the metrics so we can run the arithmetic
        response.message = getAndProcessMetrics(sensorList, query, begin, end)
        return response
    }

    String getAndProcessMetrics(List<Sensor> sensorList, String query, ZonedDateTime begin, ZonedDateTime end) {
        boolean needTemperature
        boolean needHumidity
        boolean needPressure
        boolean needWindSpeed
        boolean minRequested
        boolean maxRequested
        boolean avgRequested
        TextStringBuilder resultsBuilder = new TextStringBuilder()
        TextStringBuilder resultsBuilderTemp = new TextStringBuilder()
        TextStringBuilder resultsBuilderHumid = new TextStringBuilder()
        TextStringBuilder resultsBuilderPress = new TextStringBuilder()
        TextStringBuilder resultsBuilderSpeed = new TextStringBuilder()
        List<WeatherMetric> temperatures = new ArrayList<>()
        List<WeatherMetric> humidityVals = new ArrayList<>()
        List<WeatherMetric> pressures = new ArrayList<>()
        List<WeatherMetric> windSpeeds = new ArrayList<>()

        needTemperature = temperaturePattern.matcher(query).find()
        needHumidity = humidityPattern.matcher(query).find()
        needPressure = pressurePattern.matcher(query).find()
        needWindSpeed = windSpeedPattern.matcher(query).find()
        minRequested = minPattern.matcher(query).find()
        maxRequested = maxPattern.matcher(query).find()
        avgRequested = averagePattern.matcher(query).find()

        if(allMetricPattern.matcher(query).find()) {
            needWindSpeed = needPressure = needHumidity = needTemperature = true
        }

        //currently only the temperature sensor is the only one that has multiple metrics on it, so it needs special handling
        sensorList.forEach { Sensor it ->
            if (it.type.containsIgnoreCase("temperature")) {
                if (needTemperature) {
                    temperatures.addAll(getTemperatureMetrics(it, begin, end).collect())
                }
                if (needHumidity) {
                    humidityVals.addAll(getHumidityMetrics(it, begin, end).collect())
                }
            } else if (it.type.containsIgnoreCase("wind")) {
                if (needWindSpeed) {
                    windSpeeds.addAll(getWindSpeedMetrics(it, begin, end).collect())
                }
            } else if (it.type.containsIgnoreCase("pressure")) {
                if (needPressure) {
                    pressures.addAll(getPressureMetrics(it, begin, end).collect())
                }
            }
        }

        //once we have all metrics for each type, now we can see if we need min, max, avg
        //if begin and end are null, there's no point in running these as there will only be 0 or one record as it will be the latest value
        if(begin != null && end != null) {
            if (minRequested) {
                if (needTemperature)
                    resultsBuilderTemp.appendln("min Temperature: ${temperatures.stream().min(Comparator.comparing(WeatherMetric::getValue)).map {it.getPrettyValue()}.orElse("0.0")}")
                if (needHumidity)
                    resultsBuilderHumid.appendln("min humidty: ${humidityVals.stream().min(Comparator.comparing(WeatherMetric::getValue)).map {it.getPrettyValue()}}}")
                if (needPressure)
                    resultsBuilderPress.appendln("min pressure: ${pressures.stream().min(Comparator.comparing(WeatherMetric::getValue)).map {it.getPrettyValue()}}}")
                if (needWindSpeed)
                    resultsBuilderSpeed.appendln("min wind speed: ${windSpeeds.stream().min(Comparator.comparing(WeatherMetric::getValue)).map {it.getPrettyValue()}}}")
            }
            if (maxRequested) {
                if (needTemperature)
                    resultsBuilderTemp.appendln("max Temperature: ${temperatures.stream().max(Comparator.comparing(WeatherMetric::getValue)).map {it.getPrettyValue()}}}")
                if (needHumidity)
                    resultsBuilderHumid.appendln("max humidty: ${humidityVals.stream().max(Comparator.comparing(WeatherMetric::getValue)).map {it.getPrettyValue()}}}")
                if (needPressure)
                    resultsBuilderPress.appendln("max pressure: ${pressures.stream().max(Comparator.comparing(WeatherMetric::getValue)).map {it.getPrettyValue()}}}")
                if (needWindSpeed)
                    resultsBuilderSpeed.appendln("max wind speed: ${windSpeeds.stream().max(Comparator.comparing(WeatherMetric::getValue)).map {it.getPrettyValue()}}}")
            }
            if (avgRequested) {
                if (needTemperature)
                    resultsBuilderTemp.appendln("avg Temperature: ${temperatures.stream().mapToDouble(v -> v.getValue()).average().orElse(0.0)} C")
                if (needHumidity)
                    resultsBuilderHumid.appendln("avg humidty: ${humidityVals.stream().mapToDouble(v -> v.getValue()).average().orElse(0.0)} ${humidityVals.first().unitOfMeasure}")
                if (needPressure)
                    resultsBuilderPress.appendln("avg pressure: ${pressures.stream().mapToDouble(v -> v.getValue()).average().orElse(0.0)} ${pressures.first().unitOfMeasure}")
                if (needWindSpeed)
                    resultsBuilderSpeed.appendln("avg wind speed: ${windSpeeds.stream().mapToDouble(v -> v.getValue()).average().orElse(0.0)} ${windSpeeds.first().unitOfMeasure}")
            }
        }
        else {
            // we need to return the latest metric, check to see which ones we have available
            if(temperatures.size() == 1)
                resultsBuilderTemp.appendln("latest temperature value is ${temperatures.first().value} ${temperatures.first().unitOfMeasure}")
            if(humidityVals.size() == 1)
                resultsBuilderHumid.appendln("latest humidity value is ${humidityVals.first().value} ${humidityVals.first().unitOfMeasure}")
            if(windSpeeds.size() == 1)
                resultsBuilderSpeed.appendln("latest wind speed value is ${windSpeeds.first().value} ${windSpeeds.first().unitOfMeasure}")
            if(pressures.size() == 1)
                resultsBuilderPress.appendln("latest atmospheric pressure value is ${pressures.first().value} ${pressures.first().unitOfMeasure}")
        }

        resultsBuilder.appendln("Results are: ")
        if(resultsBuilderTemp.length() > 0)
            resultsBuilder.append(resultsBuilderTemp.toString())
        if(resultsBuilderHumid.length() > 0)
            resultsBuilder.append(resultsBuilderHumid.toString())
        if(resultsBuilderSpeed.length() > 0)
            resultsBuilder.append(resultsBuilderSpeed.toString())
        if(resultsBuilderPress.length() > 0)
            resultsBuilder.append(resultsBuilderPress.toString())
        return resultsBuilder.toString()

    }

    ArrayList<WeatherMetric> getTemperatureMetrics(Sensor sensor, ZonedDateTime begin, ZonedDateTime end) {
        if(begin != null && end != null){
            List<WeatherMetric> tmp = weatherData.findAllBySensorAndNameAndDateTimeBetween(sensor, "temperature", begin, end)
            return tmp
        }
        else {
            List<WeatherMetric> tmp = new ArrayList<>()
            tmp.add(weatherData.findFirstBySensorAndNameOrderByDateTimeDesc(sensor, "temperature"))
            return tmp
        }
    }

    ArrayList<WeatherMetric> getHumidityMetrics(Sensor sensor, ZonedDateTime begin, ZonedDateTime end) {
        if(begin != null && end != null){
            return weatherData.findAllBySensorAndNameAndDateTimeBetween(sensor, "humidity", begin, end)
        }
        else {
            List<WeatherMetric> tmp = new ArrayList<>()
            tmp.add(weatherData.findFirstBySensorAndNameOrderByDateTimeDesc(sensor, "humidity"))
            return tmp
        }
    }

    ArrayList<WeatherMetric> getWindSpeedMetrics(Sensor sensor, ZonedDateTime begin, ZonedDateTime end) {
        if(begin != null && end != null){
            return weatherData.findAllBySensorAndDateTimeBetween(sensor, begin, end)
        }
        else {
            List<WeatherMetric> tmp = new ArrayList<>()
            tmp.add(weatherData.findFirstBySensorOrderByDateTimeDesc(sensor))
            return tmp
        }
    }

    ArrayList<WeatherMetric> getPressureMetrics(Sensor sensor, ZonedDateTime begin, ZonedDateTime end) {
        if(begin != null && end != null){
            return weatherData.findAllBySensorAndDateTimeBetween(sensor, begin, end)
        }
        else {
            List<WeatherMetric> tmp = new ArrayList<>()
            tmp.add(weatherData.findFirstBySensorOrderByDateTimeDesc(sensor))
            return tmp
        }
    }
}
