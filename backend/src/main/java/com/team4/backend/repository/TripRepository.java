package com.team4.backend.repository;

import com.team4.backend.entities.Trip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Integer> {
    List<Trip> findByRouteId(int routeId);

    List<Trip> findByBusId(int busId);

    List<Trip> findByTripDate(Instant tripDate);

    List<Trip> findByRoute_FromCity(String fromCity);

    List<Trip> findByRoute_ToCity(String toCity);

    List<Trip> findByBus_Type(String bus_Type);

    List<Trip> findByRoute_FromCityAndRoute_ToCity(String fromCity, String toCity);

    List<Trip> findByRoute_FromCityAndRoute_ToCityAndTripDate(String fromCity, String toCity, Instant tripDate);

    List<Trip> findByRoute_FromCityAndRoute_ToCityAndTripDateAndBus_Type(String fromCity, String toCity, Instant tripDate, String busType);

    @Query("""
    SELECT t FROM Trip t
    WHERE (:fromCity IS NULL OR t.route.fromCity = :fromCity)
      AND (:toCity IS NULL OR t.route.toCity = :toCity)
""")
    Page<Trip> findByFilters(
            @Param("fromCity") String fromCity,
            @Param("toCity") String toCity,
            Pageable pageable
    );

}
