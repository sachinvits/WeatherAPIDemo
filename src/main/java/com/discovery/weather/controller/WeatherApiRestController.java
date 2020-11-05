package com.discovery.weather.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.discovery.weather.request.dto.WeatherRequestDto;
import com.discovery.weather.response.dto.WeatherResponseDto;
import com.discovery.weather.service.WeatherService;

@RestController
public class WeatherApiRestController {
	private static final Logger LOG = LoggerFactory.getLogger(WeatherApiRestController.class);

	@Autowired
	private WeatherService weatherService;

	@RequestMapping(value = "/weather", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> addWeatherDeatails(@RequestBody final WeatherRequestDto weatherRequestDto)
			throws Exception {
		final Long weatherId = weatherService.addWeather(weatherRequestDto);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(value = "/weather", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<WeatherResponseDto>> getAllWeatherData() throws Exception {

		final List<WeatherResponseDto> weatherList = weatherService.getWeather();
		return new ResponseEntity<>(weatherList, HttpStatus.OK);
	}

	@RequestMapping(value = "/weather/temperature", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<WeatherResponseDto>> getTemperatureFilterByDate(
			@RequestParam(name = "start", required = true) final String startDate,
			@RequestParam(name = "end", required = true) final String endDate) throws Exception {

		final List<WeatherResponseDto> weatherList = weatherService.getTemperatureFilterByDate(startDate, endDate);
		return new ResponseEntity<>(weatherList, HttpStatus.OK);
	}
}
