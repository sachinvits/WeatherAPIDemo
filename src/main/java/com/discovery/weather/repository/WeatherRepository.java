package com.discovery.weather.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.discovery.weather.model.Weather;

@Repository
public interface WeatherRepository extends JpaRepository<Weather, Long> {

	@Query("select w from Weather w where w.dateRecorded >= :startDate and w.dateRecorded <= :endDate")
	List<Weather> getTemperatureFilterByDate(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
