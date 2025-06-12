package com.team4.backend.controller;

import com.team4.backend.dto.request.PaymentRequestDto;
import com.team4.backend.dto.response.ApiResponse;
import com.team4.backend.dto.response.PagedResponse;
import com.team4.backend.dto.response.PaymentResponseDto;
import com.team4.backend.entities.Payment;
import com.team4.backend.mapper.PaymentMapper;
import com.team4.backend.repository.PaymentRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/payments")
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    public PaymentController(PaymentRepository paymentRepository, PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> addPayment(@Valid @RequestBody PaymentRequestDto paymentRequestDto) {
        if (paymentRequestDto.id() != null && paymentRepository.existsById(paymentRequestDto.id())) {
            return new ResponseEntity<>(
                    ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Payment with ID " + paymentRequestDto.id() + " already exists"),
                    HttpStatus.BAD_REQUEST
            );
        }

        Payment payment = paymentRepository.save(paymentMapper.toEntity(paymentRequestDto));
        return new ResponseEntity<>(
                ApiResponse.success(HttpStatus.CREATED.value(), "Payment Created", paymentMapper.toResponseDto(payment)),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> updatePayment(
            @PathVariable Integer id,
            @Valid @RequestBody PaymentRequestDto paymentRequestDto) {

        if (!paymentRepository.existsById(id)) {
            return new ResponseEntity<>(
                    ApiResponse.error(HttpStatus.NOT_FOUND.value(), "Payment with ID " + id + " not found"),
                    HttpStatus.NOT_FOUND
            );
        }

        Payment payment = paymentMapper.toEntity(paymentRequestDto);
        payment.setId(id);
        Payment saved = paymentRepository.save(payment);

        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Payment updated successfully", paymentMapper.toResponseDto(saved))
        );
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<PagedResponse<PaymentResponseDto>>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String bookingStatus,
            @RequestParam(required = false) String paymentStatus) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Payment> paymentPage = paymentRepository.findByFilters(paymentStatus, bookingStatus, pageable);

        List<PaymentResponseDto> responseDtos = paymentPage.getContent()
                .stream()
                .map(paymentMapper::toResponseDto)
                .toList();

        PagedResponse<PaymentResponseDto> paged = new PagedResponse<>(
                responseDtos, page, size, paymentPage.getTotalElements(),
                paymentPage.getTotalPages(), paymentPage.isLast()
        );

        return ResponseEntity.ok(ApiResponse.success(paged));
    }


    @GetMapping("/{payment_id}")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> getPaymentById(@PathVariable("payment_id") Integer paymentId) {
        Optional<Payment> payment = paymentRepository.findById(paymentId);
        return payment.map(value -> ResponseEntity.ok(ApiResponse.success(paymentMapper.toResponseDto(value))))
                .orElseGet(() -> new ResponseEntity<>(
                        ApiResponse.error(HttpStatus.NOT_FOUND.value(), "Payment with Id " + paymentId + " not found"),
                        HttpStatus.NOT_FOUND));
    }

    @GetMapping("/customer/{customer_id}")
    public ResponseEntity<ApiResponse<List<PaymentResponseDto>>> getPaymentsByCustomerId(@PathVariable("customer_id") Integer customerId) {
        List<Payment> payments = paymentRepository.findByCustomerId(customerId);
        if (payments.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Payments for customer " + customerId + " not found"));
        }
        List<PaymentResponseDto> dtos = payments.stream().map(paymentMapper::toResponseDto).toList();
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/booking/{booking_id}")
    public ResponseEntity<ApiResponse<List<PaymentResponseDto>>> getPaymentsByBookingId(@PathVariable("booking_id") Integer bookingId) {
        List<Payment> payments = paymentRepository.findByBookingId(bookingId);
        if (payments.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Payments for booking " + bookingId + " not found"));
        }
        List<PaymentResponseDto> dtos = payments.stream().map(paymentMapper::toResponseDto).toList();
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/amount/{amount}")
    public ResponseEntity<ApiResponse<List<PaymentResponseDto>>> getPaymentsByAmount(@PathVariable("amount") BigDecimal amount) {
        List<Payment> payments = paymentRepository.findByAmount(amount);
        if (payments.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Payments with amount " + amount + " not found"));
        }
        List<PaymentResponseDto> dtos = payments.stream().map(paymentMapper::toResponseDto).toList();
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/status/{payment_status}")
    public ResponseEntity<ApiResponse<List<PaymentResponseDto>>> getPaymentsByStatus(@PathVariable("payment_status") String paymentStatus) {
        List<Payment> payments = paymentRepository.findByPaymentStatus(paymentStatus);
        if (payments.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Payments with status " + paymentStatus + " not found"));
        }
        List<PaymentResponseDto> dtos = payments.stream().map(paymentMapper::toResponseDto).toList();
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }
}
