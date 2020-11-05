package com.discovery.weather.request.dto;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeatherRequestDto {
	private Date dateRecorded;
	private Long id;
	private LocationRequestDto location;
	private List<Float> temperature;
}
