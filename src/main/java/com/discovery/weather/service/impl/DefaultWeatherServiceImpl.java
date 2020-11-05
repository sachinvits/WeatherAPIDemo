package com.discovery.weather.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.discovery.weather.model.Location;
import com.discovery.weather.model.Weather;
import com.discovery.weather.repository.WeatherRepository;
import com.discovery.weather.request.dto.WeatherRequestDto;
import com.discovery.weather.response.dto.LocationResponseDto;
import com.discovery.weather.response.dto.WeatherResponseDto;
import com.discovery.weather.service.WeatherService;

@Service
public class DefaultWeatherServiceImpl implements WeatherService {
	private static final Logger LOG = LoggerFactory.getLogger(DefaultWeatherServiceImpl.class);

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	@Autowired
	private WeatherRepository weatherRepository;

	@Override
	public Long addWeather(final WeatherRequestDto weatherRequestDto) {
		final Location location = Location.builder()//
				.cityName(weatherRequestDto.getLocation().getCity())//
				.stateName(weatherRequestDto.getLocation().getState())//
				.latitude(weatherRequestDto.getLocation().getLat())//
				.longitude(weatherRequestDto.getLocation().getLon())//
				.build();

		final Weather weather = Weather.builder()//
				.id(weatherRequestDto.getId())//
				.dateRecorded(weatherRequestDto.getDateRecorded())//
				.temperature(weatherRequestDto.getTemperature())//
				.location(location)//
				.build();//

		final Weather savedWeather = weatherRepository.save(weather);
		LOG.info("Saved Weather id={}", savedWeather.getId());
		return savedWeather.getId();
	}

	@Override
	public List<WeatherResponseDto> getTemperatureFilterByDate(final String startDate, final String endDate) {
		final List<Weather> weatherList = weatherRepository.getTemperatureFilterByDate(parseDate(startDate),
				parseDate(endDate));

		final List<WeatherResponseDto> weatherResponses = new ArrayList<>();
		return weatherResponses;
	}

	@Override
	public List<WeatherResponseDto> getWeather() {
		final List<Weather> weatherList = weatherRepository.findAll();

		final List<WeatherResponseDto> weatherResponses = new ArrayList<>();
		weatherList.forEach(weather -> {
			final LocationResponseDto locationResponse = LocationResponseDto.builder()//
					.city(weather.getLocation().getCityName())//
					.state(weather.getLocation().getStateName())//
					.lat(weather.getLocation().getLatitude())//
					.lon(weather.getLocation().getLongitude())//
					.build();//

			final WeatherResponseDto weatherResponse = WeatherResponseDto.builder()//
					.dateRecorded(weather.getDateRecorded())//
					.id(weather.getId())//
					.temperature(weather.getTemperature())//
					.location(locationResponse).build();//

			weatherResponses.add(weatherResponse);
		});

		return weatherResponses;
	}

	private Date parseDate(final String date) {
		try {
			return dateFormat.parse(date);
		} catch (final ParseException e) {
			e.printStackTrace();
		}

		return null;

	}

}
