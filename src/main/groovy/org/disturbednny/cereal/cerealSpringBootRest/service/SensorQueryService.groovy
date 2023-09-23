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

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
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
    private static final Pattern betweenPattern = Pattern.compile("(?i)((?<length>between|from|)( )?(?<range>(?<begin>(\\d{4}-\\d{2}-\\d{2}){1})) and (?<end>(\\d{4}-\\d{2}-\\d{2}){1}))")
    private static final Pattern forSpecificDatePattern = Pattern.compile("(?i)(?<date>(\\d{4}-\\d{2}-\\d{2}){1})")



    private final WeatherData weatherData
    private final Sensors sensors
    @Autowired
    SensorQueryService(WeatherData data, Sensors sensors) {
        this.weatherData = data
        this.sensors = sensors
    }

    /**
     * This method takes in a SensorQueryRequest, parses the query out for statistic types, metrics to gather statistics on,
     * the sensor(s) to get the metrics for, and the time range. If no time range is given, it will provide the latest metrics
     * for each sensor.
     *
     * There is validation in place for time range. if the query is empty, the service will assume you want the latest metrics for all sensors
     *
     * @param SensorQueryRequest request
     * @return SensorQuery response with success / failure along with the message of either all of the statistics and metrics per sensor, or error message
     */
    SensorQueryResponse processQuery(SensorQueryRequest request) {
        log.debug("starting processing of query")
        SensorQueryResponse response = new SensorQueryResponse()
        ZonedDateTime begin = null
        ZonedDateTime end = null
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
                response.success = false
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
                begin = ZonedDateTime.of(LocalDate.parse(betweenMatcher.group("begin")), LocalTime.MIDNIGHT, ZoneId.systemDefault())
                end = ZonedDateTime.of(LocalDate.parse(betweenMatcher.group("end")), LocalTime.MIDNIGHT, ZoneId.systemDefault())
            }
            catch (Exception ex) {
                response.message = "Unable to get begin and end dates. check your formatting and try again"
                response.success = false
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
                        response.success = false
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
                        response.success = false
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
                        response.success = false
                        log.warn(response.message)
                        return response
                    }
                    begin = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).minusMonths(numberOfTemporal) //multiply by 7 for number of days in week.
                    end = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).plusDays(1)
                    break
                }
                default:
                    // this switch case should never get hit, unless the matcher is changed to include year, hours, minutes, etc
                    // and the switch case hasn't been updated
                    response.message = "temporal unit not supported at this time. file a feature request"
                    response.success = false
                    log.warn(response.message)
                    break
            }
        }
        else if(specificDateMatcher.find()) {
            try {
                begin = ZonedDateTime.of(LocalDate.parse(specificDateMatcher.group("date")), LocalTime.MIDNIGHT, ZoneId.systemDefault())
                end = ZonedDateTime.of(LocalDate.parse(specificDateMatcher.group("date")), LocalTime.MIDNIGHT, ZoneId.systemDefault())
            }
            catch (Exception ex) {
                response.message = "Unable to get begin and end dates. check your formatting and try again"
                response.success = false
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
        response.success = true
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

        resultsBuilder.appendln("Results are: ")
        //currently only the temperature sensor is the only one that has multiple metrics on it, so it needs special handling
        sensorList.forEach { Sensor it ->
            resultsBuilder.appendln("Metrics from sensor ${it.name}")
            if (it.type.containsIgnoreCase("temperature")) {
                if (needTemperature) {
                    temperatures.addAll(getTemperatureMetrics(it, begin, end).collect())
                    resultsBuilder.append(processMetricsIntoResults(temperatures, begin, end, minRequested, maxRequested, avgRequested))
                }
                if (needHumidity) {
                    humidityVals.addAll(getHumidityMetrics(it, begin, end).collect())
                    resultsBuilder.append(processMetricsIntoResults(humidityVals, begin, end, minRequested, maxRequested, avgRequested))
                }
            } else if (it.type.containsIgnoreCase("wind")) {
                if (needWindSpeed) {
                    windSpeeds.addAll(getWindSpeedMetrics(it, begin, end).collect())
                    resultsBuilder.append(processMetricsIntoResults(windSpeeds, begin, end, minRequested, maxRequested, avgRequested))
                }
            } else if (it.type.containsIgnoreCase("pressure")) {
                if (needPressure) {
                    pressures.addAll(getPressureMetrics(it, begin, end).collect())
                    resultsBuilder.append(processMetricsIntoResults(pressures, begin, end, minRequested, maxRequested, avgRequested))
                }
            }
            temperatures.clear()
            humidityVals.clear()
            windSpeeds.clear()
            pressures.clear()
        }
        return resultsBuilder.build()
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

    String processMetricsIntoResults(List<WeatherMetric> metrics, ZonedDateTime begin, ZonedDateTime end, boolean min, boolean max, boolean average) {
        TextStringBuilder builder = new TextStringBuilder()
        if(metrics.size() == 0){
            return "no metrics for this time range\n"
        }
        if(begin != null && end != null) {
            if (min) {
                builder.appendln("min ${metrics[0].name}: ${metrics.stream().min(Comparator.comparing(WeatherMetric::getValue)).map(it -> it.prettyValue).orElse("N/A")}")
            }
            if (max) {
                builder.appendln("max ${metrics[0].name}: ${metrics.stream().max(Comparator.comparing(WeatherMetric::getValue)).map(it -> it.prettyValue).orElse("N/A")}")
            }
            if (average) {
                builder.appendln("avg ${metrics[0].name}: ${metrics.stream().mapToDouble(v -> v.getValue()).average().orElse(0.0)} ${metrics[0].unitOfMeasure}")
            }
        }
        else {
            // we need to return the latest metric, check to see which ones we have available
            if(metrics.size() == 1)
                builder.appendln("latest ${metrics[0].name} value is ${metrics[0].value} ${metrics[0].unitOfMeasure}")
        }
        return builder.toString()
    }
}
