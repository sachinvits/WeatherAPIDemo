package com.discovery.weather.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.discovery.weather.model.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
}
