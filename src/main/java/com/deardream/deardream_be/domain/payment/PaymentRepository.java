package com.deardream.deardream_be.domain.payment;


import com.deardream.deardream_be.domain.family.entity.Family;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    Payment findByTid(String tid);

    Payment findByFamily(Family family);

    Payment findLastByFamily(Family family);

    Optional<Payment> findFirstByFamilyAndIsActiveTrueAndSidNotNullOrderByCreatedAtDesc(Family family);

    List<Payment> findAllByIsActiveTrue();
    List<Payment> findAllByFamily(Family family);
}
