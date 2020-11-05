package com.discovery.weather.response.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LocationResponseDto {
	private String city;
	private Float lat;
	private Float lon;
	private String state;
}
