package com.discovery.weather.response.dto;

import java.util.Date;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WeatherResponseDto {
	private Date dateRecorded;
	private Long id;
	private LocationResponseDto location;
	private List<Float> temperature;
}
