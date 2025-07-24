package com.deardream.deardream_be.domain.payment;

import com.deardream.deardream_be.domain.family.entity.Family;
import com.deardream.deardream_be.domain.payment.entity.Subscription;
import com.deardream.deardream_be.domain.payment.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findAllByStatus(SubscriptionStatus status);
    Subscription findByFamily(Family family);
}
