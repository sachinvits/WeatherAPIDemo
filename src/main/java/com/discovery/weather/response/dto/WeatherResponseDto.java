package com.discovery.weather.response.dto;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonPropertyOrder(value = { "id", "date", "location", "temperature" })
public class WeatherResponseDto {
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date date;
	private Long id;
	private LocationResponseDto location;
	private List<Float> temperature;
}
