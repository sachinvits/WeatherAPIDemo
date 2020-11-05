package com.discovery.weather.response.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonPropertyOrder(value = { "lat", "lon", "city", "state" })
public class LocationResponseDto {
	private String city;
	private Float lat;
	private Float lon;
	private String state;
}
