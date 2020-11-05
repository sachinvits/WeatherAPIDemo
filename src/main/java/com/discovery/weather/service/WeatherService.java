package com.discovery.weather.service;

import java.util.List;

import com.discovery.weather.request.dto.WeatherRequestDto;
import com.discovery.weather.response.dto.WeatherResponseDto;

public interface WeatherService {
	Long addWeather(WeatherRequestDto weatherRequestDto);

	List<WeatherResponseDto> getTemperatureFilterByDate(String startDate, String endDate);

	List<WeatherResponseDto> getWeather();
}
