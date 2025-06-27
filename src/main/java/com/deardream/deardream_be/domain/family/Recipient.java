package com.deardream.deardream_be.domain.family;

import com.deardream.deardream_be.domain.institution.CalendarType;
import com.deardream.deardream_be.domain.institution.DeliveryType;
import com.deardream.deardream_be.domain.institution.Institution;
import com.deardream.deardream_be.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "recipient")
public class Recipient extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    @ManyToOne
    @JoinColumn(name = "istitution")
    private Institution institution;

    private String name;
    private LocalDate birth;

    @Enumerated(EnumType.STRING)
    private CalendarType calendarType;

    private String phone;

    @Enumerated(EnumType.STRING)
    private DeliveryType deliveryType;

    private String address;
    private String addressDetail;
    private Integer postalCode;


}
