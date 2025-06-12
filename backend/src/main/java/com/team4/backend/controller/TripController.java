package com.team4.backend.controller;

import com.team4.backend.dto.request.TripRequestDto;
import com.team4.backend.dto.response.ApiResponse;
import com.team4.backend.dto.response.PagedResponse;
import com.team4.backend.dto.response.TripResponseDto;
import com.team4.backend.entities.Trip;
import com.team4.backend.mapper.TripMapper;
import com.team4.backend.repository.TripRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/trips")
public class TripController {
    private final TripRepository tripRepository;
    private final TripMapper tripMapper;

    public TripController(TripRepository tripRepository,
                          TripMapper tripMapper) {
        this.tripRepository = tripRepository;
        this.tripMapper = tripMapper;
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<PagedResponse<TripResponseDto>>> getAllTrips(
            @RequestParam(required = false) String fromCity,
            @RequestParam(required = false) String toCity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Trip> tripPage = tripRepository.findByFilters(fromCity, toCity, pageable);

        List<TripResponseDto> tripResponseDtos = tripPage.getContent()
                .stream()
                .map(tripMapper::toResponseDto)
                .toList();

        PagedResponse<TripResponseDto> paged = new PagedResponse<>(
                tripResponseDtos, page, size, tripPage.getTotalElements(),
                tripPage.getTotalPages(), tripPage.isLast()
        );

        return ResponseEntity.ok(ApiResponse.success(paged));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TripResponseDto>> getTripById(@PathVariable int id) {
        Optional<Trip> trip = tripRepository.findById(id);
        if (trip.isPresent()) {
            TripResponseDto tripResponseDto = tripMapper.toResponseDto(trip.get());
            return new ResponseEntity<>(ApiResponse.success(tripResponseDto), HttpStatus.OK);
        }
        return new ResponseEntity<>(
                ApiResponse.error(HttpStatus.NOT_FOUND.value(), "Trip with Id " + id + " not found"),
                HttpStatus.NOT_FOUND);
    }

    @GetMapping("/routes/{route_id}")
    public ResponseEntity<ApiResponse<List<TripResponseDto>>> getTripsByRouteId(@PathVariable("route_id") int routeId) {
        List<Trip> trip = tripRepository.findByRouteId(routeId);

        if (trip.isEmpty()) {
            return new ResponseEntity<>(ApiResponse.error(HttpStatus.NOT_FOUND.value(),
                    "Trips with RouteId " + routeId + " not found"), HttpStatus.NOT_FOUND);
        }

        List<TripResponseDto> tripRequestDtos = trip.stream().map(tripMapper::toResponseDto).toList();
        return new ResponseEntity<>(ApiResponse.success(tripRequestDtos), HttpStatus.OK);
    }

    @GetMapping("/buses/{bus_id}")
    public ResponseEntity<ApiResponse<List<TripResponseDto>>> getTripsByBusId(@PathVariable("bus_id") int busId) {
        List<Trip> trip = tripRepository.findByBusId(busId);
        if (trip.isEmpty()) {
            return new ResponseEntity<>(
                    ApiResponse.error(HttpStatus.NOT_FOUND.value(), "Trips with BusId " + busId + " not found"),
                    HttpStatus.NOT_FOUND);
        }
        List<TripResponseDto> tripResponseDtos = trip.stream().map(tripMapper::toResponseDto).toList();
        return new ResponseEntity<>(ApiResponse.success(tripResponseDtos), HttpStatus.OK);
    }

    @GetMapping("/trip_date/{trip_date}")
    public ResponseEntity<ApiResponse<List<TripResponseDto>>> getTripsByTripDate(@PathVariable("trip_date") Instant tripDate) {
        List<Trip> trip = tripRepository.findByTripDate(tripDate);
        if (trip.isEmpty()) {
            return new ResponseEntity<>(
                    ApiResponse.error(HttpStatus.NOT_FOUND.value(), "Trips on date " + tripDate + " not found"),
                    HttpStatus.NOT_FOUND);
        }
        List<TripResponseDto> tripResponseDtos = trip.stream().map(tripMapper::toResponseDto).toList();
        return new ResponseEntity<>(ApiResponse.success(tripResponseDtos), HttpStatus.OK);
    }

    @GetMapping("/from_city/{from_city}")
    public ResponseEntity<ApiResponse<List<TripResponseDto>>> getTripsByFromCity(@PathVariable("from_city") String fromCity) {
        List<Trip> trips = tripRepository.findByRoute_FromCity(fromCity);
        if (trips.isEmpty()) {
            return new ResponseEntity<>(
                    ApiResponse.error(HttpStatus.NOT_FOUND.value(), "Trips from city " + fromCity + " not found"),
                    HttpStatus.NOT_FOUND);
        }
        List<TripResponseDto> tripResponseDtos = trips.stream().map(tripMapper::toResponseDto).toList();
        return new ResponseEntity<>(ApiResponse.success(tripResponseDtos), HttpStatus.OK);
    }

    @GetMapping("/to_city/{to_city}")
    public ResponseEntity<ApiResponse<List<TripResponseDto>>> getTripsByToCity(@PathVariable("to_city") String toCity) {
        List<Trip> trips = tripRepository.findByRoute_ToCity(toCity);
        if (trips.isEmpty()) {
            return new ResponseEntity<>(
                    ApiResponse.error(HttpStatus.NOT_FOUND.value(), "Trips to city " + toCity + " not found"),
                    HttpStatus.NOT_FOUND);
        }
        List<TripResponseDto> tripResponseDtos = trips.stream().map(tripMapper::toResponseDto).toList();
        return new ResponseEntity<>(ApiResponse.success(tripResponseDtos), HttpStatus.OK);
    }

    @GetMapping("/bus_type/{bus_type}")
    public ResponseEntity<ApiResponse<List<TripResponseDto>>> getTripsByBusType(@PathVariable("bus_type") String busType) {
        List<Trip> trips = tripRepository.findByBus_Type(busType);
        if (trips.isEmpty()) {
            return new ResponseEntity<>(
                    ApiResponse.error(HttpStatus.NOT_FOUND.value(), "Trips with bus type " + busType + " not found"),
                    HttpStatus.NOT_FOUND);
        }
        List<TripResponseDto> tripResponseDtos = trips.stream().map(tripMapper::toResponseDto).toList();
        return new ResponseEntity<>(ApiResponse.success(tripResponseDtos), HttpStatus.OK);
    }

    @GetMapping("/{from_city}/{to_city}/{trip_date}")
    public ResponseEntity<ApiResponse<List<TripResponseDto>>> getTripsByFromToCityAndDate(@PathVariable("from_city") String fromCity,
                                                                                         @PathVariable("to_city") String toCity,
                                                                                         @PathVariable("trip_date") Instant tripDate) {
        List<Trip> trips = tripRepository.findByRoute_FromCityAndRoute_ToCityAndTripDate(fromCity, toCity, tripDate);
        if (trips.isEmpty()) {
            return new ResponseEntity<>(
                    ApiResponse.error(HttpStatus.NOT_FOUND.value(),
                            "Trips from city " + fromCity + " to " + toCity + " on date " + tripDate + " not found"),
                    HttpStatus.NOT_FOUND);
        }
        List<TripResponseDto> tripResponseDtos = trips.stream().map(tripMapper::toResponseDto).toList();
        return new ResponseEntity<>(ApiResponse.success(tripResponseDtos), HttpStatus.OK);
    }

    @GetMapping("/{from_city}/{to_city}")
    public ResponseEntity<ApiResponse<List<TripResponseDto>>> getTripsByFromToCityAndDate(@PathVariable("from_city") String fromCity,
                                                                                         @PathVariable("to_city") String toCity) {
        List<Trip> trips = tripRepository.findByRoute_FromCityAndRoute_ToCity(fromCity, toCity);
        if (trips.isEmpty()) {
            return new ResponseEntity<>(
                    ApiResponse.error(HttpStatus.NOT_FOUND.value(),
                            "Trips from city " + fromCity + " to " + toCity + " not found"),
                    HttpStatus.NOT_FOUND);
        }
        List<TripResponseDto> tripResponseDtos = trips.stream().map(tripMapper::toResponseDto).toList();
        return new ResponseEntity<>(ApiResponse.success(tripResponseDtos), HttpStatus.OK);
    }

    @GetMapping("/{from_city}/{to_city}/{trip_date}/{bus_type}")
    public ResponseEntity<ApiResponse<List<TripResponseDto>>> getTripsByFromToCityAndDate(@PathVariable("from_city") String fromCity,
                                                                                         @PathVariable("to_city") String toCity,
                                                                                         @PathVariable("trip_date") Instant tripDate,
                                                                                         @PathVariable("bus_type") String busType ){
        List<Trip> trips = tripRepository.findByRoute_FromCityAndRoute_ToCityAndTripDateAndBus_Type(fromCity, toCity, tripDate, busType);
        if (trips.isEmpty()) {
            return new ResponseEntity<>(
                    ApiResponse.error(HttpStatus.NOT_FOUND.value(),
                            "Trips from city " + fromCity + " to " + toCity + " on date " + tripDate + " with bus type " + busType + " not found"),
                    HttpStatus.NOT_FOUND);
        }
        List<TripResponseDto> tripResponseDtos = trips.stream().map(tripMapper::toResponseDto).toList();
        return new ResponseEntity<>(ApiResponse.success(tripResponseDtos), HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<TripResponseDto>> addTrip(@Valid @RequestBody TripRequestDto tripRequestDto) {
        if (tripRequestDto.id() != null && tripRepository.existsById(tripRequestDto.id())) {
            return new ResponseEntity<>(
                    ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Trip with ID " + tripRequestDto.id() + " already exists"),
                    HttpStatus.BAD_REQUEST
            );
        }

        Trip trip = tripRepository.save(tripMapper.toEntity(tripRequestDto));
        return new ResponseEntity<>(
                ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        "Trip Created",
                        tripMapper.toResponseDto(trip)
                ),
                HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TripResponseDto>> updateTrip(@PathVariable Integer id,
                                                                  @Valid @RequestBody TripRequestDto tripRequestDto) {
        if (!tripRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Trip not found"));
        }

        Trip updatedTrip = tripMapper.toEntity(tripRequestDto);
        updatedTrip.setId(id);

        Trip savedTrip = tripRepository.save(updatedTrip);

        return ResponseEntity.ok(ApiResponse.success(tripMapper.toResponseDto(savedTrip)));
    }

}
