package com.deardream.deardream_be.domain.payment;


import com.deardream.deardream_be.domain.family.entity.Family;
import com.deardream.deardream_be.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    Payment findByTid(String tid);

    List<Payment> findAllByIsActiveTrue();
    List<Payment> findAllByUser(User user);

    @Query("""
    SELECT p FROM Payment p
    WHERE p.user = :user
        AND p.isActive = true
    ORDER BY p.approvedAt DESC
    LIMIT 1
""")
    Optional<Payment> findLastestByUser(@Param("user")User user);

    // 만료된 결제 조회
    @Query("""
    SELECT p FROM Payment p
    WHERE p.isActive = true
      AND p.sid IS NOT NULL
      AND p.approvedAt <= :cutoffDate
""")
    List<Payment> findExpiredActivePayments(@Param("cutoffDate") LocalDate cutoffDate);

    void deleteAllByUser(User user);


}
