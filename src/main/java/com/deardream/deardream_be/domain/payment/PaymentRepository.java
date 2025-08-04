package com.deardream.deardream_be.domain.payment;


import com.deardream.deardream_be.domain.family.entity.Family;
import com.deardream.deardream_be.domain.user.entity.User;
import org.apache.batik.ext.awt.image.PadMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByTid(String tid);
    Optional<Payment> findBySid(String sid);

    List<Payment> findAllByFamilyId(Long familyId);


    // 만료된 결제 조회

    //void deleteAllByUser(User user);

    // 최근 정기 결제 정보 조회
    Optional<Payment> findTopByFamilyIdAndStatusOrderByApprovedAtDesc(Long familyId, PaymentStatus status);

    List<Payment> findAllByStatusAndSidIsNotNull(PaymentStatus status);

}
