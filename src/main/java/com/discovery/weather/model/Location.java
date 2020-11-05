package com.discovery.weather.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Location", uniqueConstraints = { @UniqueConstraint(columnNames = { "id" }) })
@Getter
@Setter
@Builder
public class Location {
	@Column(name = "city_name", nullable = true)
	private String cityName;

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "latitude", nullable = true)
	private Float latitude;

	@Column(name = "longitude", nullable = true)
	private Float longitude;

	@Column(name = "stateName", nullable = true)
	private String stateName;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "location")
	private List<Weather> weather;

	public Location() {

	}

	public Location(final String cityName, final Long id, final Float latitude, final Float longitude,
			final String stateName, final List<Weather> weather) {
		super();
		this.cityName = cityName;
		this.id = id;
		this.latitude = latitude;
		this.longitude = longitude;
		this.stateName = stateName;
		this.weather = weather;
	}
}
