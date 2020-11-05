package com.discovery.weather.request.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationRequestDto {
	private String city;
	private Float lat;
	private Float lon;
	private String state;
}
