package com.deardream.deardream_be.domain.archive;


import com.deardream.deardream_be.domain.family.Family;
import com.deardream.deardream_be.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "monthly_archive")
public class MonthlyArchive extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    private int archiveYear;

    private int archiveMonth;

    private String pdfUrl;

    private String s3Key;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;

}
