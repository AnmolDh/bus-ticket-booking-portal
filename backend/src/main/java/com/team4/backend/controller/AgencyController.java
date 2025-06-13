package com.team4.backend.controller;

import com.team4.backend.dto.request.AgencyOfficeRequestDto;
import com.team4.backend.dto.request.AgencyRequestDto;
import com.team4.backend.dto.response.AgencyOfficeResponseDto;
import com.team4.backend.dto.response.AgencyResponseDto;
import com.team4.backend.dto.response.ApiResponse;
import com.team4.backend.dto.response.PagedResponse;
import com.team4.backend.entities.Agency;
import com.team4.backend.entities.AgencyOffice;
import com.team4.backend.mapper.AgencyMapper;
import com.team4.backend.mapper.AgencyOfficeMapper;
import com.team4.backend.repository.AgencyOfficeRepository;
import com.team4.backend.repository.AgencyRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/agencies")
public class AgencyController {

    private final AgencyRepository agencyRepository;
    private final AgencyOfficeRepository agencyOfficeRepository;
    private final AgencyMapper agencyMapper;
    private final AgencyOfficeMapper agencyOfficeMapper;

    public AgencyController(AgencyRepository agencyRepository, AgencyOfficeRepository agencyOfficeRepository, AgencyMapper agencyMapper, AgencyOfficeMapper agencyOfficeMapper) {
        this.agencyRepository = agencyRepository;
        this.agencyOfficeRepository = agencyOfficeRepository;
        this.agencyMapper = agencyMapper;
        this.agencyOfficeMapper = agencyOfficeMapper;
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<PagedResponse<AgencyResponseDto>>> getAllAgencies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Agency> agencyPage = agencyRepository.findAll(pageable);

        List<AgencyResponseDto> dtoList = agencyPage.getContent()
                .stream()
                .map(agencyMapper::toResponseDto)
                .toList();

        if (dtoList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "No agencies found"));
        }

        PagedResponse<AgencyResponseDto> pagedResponse = new PagedResponse<>(
                dtoList,
                agencyPage.getNumber(),
                agencyPage.getSize(),
                agencyPage.getTotalElements(),
                agencyPage.getTotalPages(),
                agencyPage.isLast()
        );

        return ResponseEntity.ok(ApiResponse.success(pagedResponse));
    }



    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AgencyResponseDto>> getAgencyById(@PathVariable("id") Integer agencyId) {
        Optional<Agency> agency = agencyRepository.findById(agencyId);
        if (agency.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "Agency not found with id " + agencyId));
        }
        return ResponseEntity.ok(ApiResponse.success(agencyMapper.toResponseDto(agency.get())));
    }

    @GetMapping("/offices")
    public ResponseEntity<ApiResponse<PagedResponse<AgencyOfficeResponseDto>>> getOffices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AgencyOffice> officePage = agencyOfficeRepository.findAll(pageable);

        List<AgencyOfficeResponseDto> dtoList = officePage.getContent()
                .stream()
                .map(agencyOfficeMapper::toResponseDto)
                .toList();

        if (dtoList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "No agency offices found"));
        }

        PagedResponse<AgencyOfficeResponseDto> pagedResponse = new PagedResponse<>(
                dtoList,
                officePage.getNumber(),
                officePage.getSize(),
                officePage.getTotalElements(),
                officePage.getTotalPages(),
                officePage.isLast()
        );

        return ResponseEntity.ok(ApiResponse.success(pagedResponse));
    }


    @GetMapping("/offices/agency_id/{agency}")
    public ResponseEntity<ApiResponse<List<AgencyOfficeResponseDto>>> getOfficesByAgencyId(@PathVariable("agency") Integer agencyId) {
        List<AgencyOffice> offices = agencyOfficeRepository.findByAgencyId(agencyId);
        if (offices.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "No offices found for agency ID " + agencyId));
        }
        List<AgencyOfficeResponseDto> dtoList = offices.stream().map(agencyOfficeMapper::toResponseDto).toList();
        return ResponseEntity.ok(ApiResponse.success(dtoList));
    }

    @GetMapping("/offices/{id}")
    public ResponseEntity<ApiResponse<AgencyOfficeResponseDto>> getOfficeById(@PathVariable("id") Integer officeId) {
        Optional<AgencyOffice> officeOpt = agencyOfficeRepository.findById(officeId);
        if (officeOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "Office not found with id " + officeId));
        }
        return ResponseEntity.ok(ApiResponse.success(agencyOfficeMapper.toResponseDto(officeOpt.get())));
    }

    @GetMapping("/offices/email/{email}")
    public ResponseEntity<ApiResponse<List<AgencyOfficeResponseDto>>> getOfficeEmails(@PathVariable("email") String emailId) {
        List<AgencyOffice> offices = agencyOfficeRepository.findByOfficeMail(emailId);
        if (offices.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "No offices found with email " + emailId));
        }
        List<AgencyOfficeResponseDto> dtoList = offices.stream().map(agencyOfficeMapper::toResponseDto).toList();
        return ResponseEntity.ok(ApiResponse.success(dtoList));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<List<AgencyResponseDto>>> getEmails(@PathVariable("email") String email) {
        List<Agency> agencies = agencyRepository.findByEmail(email);
        if (agencies.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "No agencies found with email " + email));
        }
        List<AgencyResponseDto> dtoList = agencies.stream().map(agencyMapper::toResponseDto).toList();
        return ResponseEntity.ok(ApiResponse.success(dtoList));
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<AgencyResponseDto>> createAgency(@RequestBody @Valid AgencyRequestDto agencyRequestDto) {
        if (agencyRequestDto.id() != null && agencyRepository.existsById(agencyRequestDto.id())) {
            return new ResponseEntity<>(
                    ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Agency with ID " + agencyRequestDto.id() + " already exists"),
                    HttpStatus.BAD_REQUEST
            );
        }

        Agency agency = agencyMapper.toEntity(agencyRequestDto);
        agency = agencyRepository.save(agency);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Agency Created", agencyMapper.toResponseDto(agency)));
    }

    @PutMapping("/{agency_id}")
    public ResponseEntity<ApiResponse<AgencyResponseDto>> updateAgency(@PathVariable("agency_id") Integer agencyId,
                                                                       @RequestBody @Valid AgencyRequestDto agencyRequestDto) {
        return agencyRepository.findById(agencyId)
                .map(existingAgency -> {
                    Agency updatedAgency = agencyMapper.toEntity(agencyRequestDto);
                    updatedAgency.setId(agencyId);
                    Agency savedAgency = agencyRepository.save(updatedAgency);
                    return ResponseEntity.ok(
                            ApiResponse.success(HttpStatus.OK.value(), "Agency updated successfully", agencyMapper.toResponseDto(savedAgency))
                    );
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.error(HttpStatus.NOT_FOUND.value(), "Agency not found")
                ));
    }

    @PostMapping("/offices")
    public ResponseEntity<ApiResponse<AgencyOfficeResponseDto>> addAgencyOffice(@Valid @RequestBody AgencyOfficeRequestDto requestDto) {
        if (requestDto.id() != null && agencyOfficeRepository.existsById(requestDto.id())) {
            return new ResponseEntity<>(
                    ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Agency Office with ID " + requestDto.id() + " already exists"),
                    HttpStatus.BAD_REQUEST
            );
        }

        AgencyOffice agencyOffice = agencyOfficeMapper.toEntity(requestDto);
        agencyOffice = agencyOfficeRepository.save(agencyOffice);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Agency Office Created", agencyOfficeMapper.toResponseDto(agencyOffice)));
    }

    @PutMapping("/offices/{office_id}")
    public ResponseEntity<ApiResponse<AgencyOfficeResponseDto>> updateAgencyOffice(
            @PathVariable("office_id") Integer officeId,
            @RequestBody @Valid AgencyOfficeRequestDto officeDto) {

        return agencyOfficeRepository.findById(officeId)
                .map(existingOffice -> {
                    AgencyOffice updatedOffice = agencyOfficeMapper.toEntity(officeDto);
                    updatedOffice.setId(officeId);
                    AgencyOffice savedOffice = agencyOfficeRepository.save(updatedOffice);
                    return ResponseEntity.ok(
                            ApiResponse.success(HttpStatus.OK.value(), "Office updated successfully", agencyOfficeMapper.toResponseDto(savedOffice))
                    );
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.error(HttpStatus.NOT_FOUND.value(), "Office not found")
                ));
    }
}
