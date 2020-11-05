package com.discovery.weather.model;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Weather", uniqueConstraints = { @UniqueConstraint(columnNames = { "id" }) })
@Getter
@Setter
@Builder
public class Weather {
	@Column(name = "date_recorded", nullable = true)
	private Date dateRecorded;

	@Id
	@Column(name = "id", nullable = false)
	private Long id;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Location location;

	@ElementCollection
	@OrderColumn
	@Column(name = "temperature", nullable = true)
	private List<Float> temperature;

	public Weather() {

	}

	public Weather(final Date dateRecorded, final Long id, final Location location, final List<Float> temperature) {
		super();
		this.dateRecorded = dateRecorded;
		this.id = id;
		this.location = location;
		this.temperature = temperature;
	}

}
