package com.team4.backend.repository;

import com.team4.backend.entities.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByCustomerId(Integer customerId);
    List<Payment> findByBookingId(Integer bookingId);

    List<Payment> findByAmount(BigDecimal paymentId);
    List<Payment> findByPaymentStatus(String paymentStatus);

    @Query("""
    SELECT p FROM Payment p
    JOIN p.booking b
    WHERE (:paymentStatus IS NULL OR p.paymentStatus = :paymentStatus)
      AND (:bookingStatus IS NULL OR b.status = :bookingStatus)
    """)
    Page<Payment> findByFilters(
            @Param("paymentStatus") String paymentStatus,
            @Param("bookingStatus") String bookingStatus,
            Pageable pageable);

}
