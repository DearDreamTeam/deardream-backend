package com.deardream.deardream_be.domain.payment;


import com.deardream.deardream_be.domain.payment.entity.Payment;
import com.deardream.deardream_be.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    Payment findByTid(String tid);

    Payment findByUser(User user);

    List<Payment> findAllByUser(User user);

    void deleteAllByUser(User user);


}
