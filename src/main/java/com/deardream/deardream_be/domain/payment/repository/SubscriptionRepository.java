package com.deardream.deardream_be.domain.payment.repository;

import com.deardream.deardream_be.domain.payment.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
}
