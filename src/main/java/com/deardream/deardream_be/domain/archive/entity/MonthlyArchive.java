package com.deardream.deardream_be.domain.archive.entity;


import com.deardream.deardream_be.domain.archive.entity.DeliveryStatus;
import com.deardream.deardream_be.domain.family.entity.Family;
import com.deardream.deardream_be.domain.recipient.entity.Recipient;
import com.deardream.deardream_be.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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

    private Integer archiveYear;

    private Integer archiveMonth;

    private String pdfUrl;

    private String s3Key;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private Recipient recipient;

    @OneToMany(mappedBy = "archive", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ArchiveBookmark> bookmarks = new ArrayList<>();

    public void updateDeliverStatus(DeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public void updatePdfUrl(String pdfUrl, String s3Key) {
        this.pdfUrl = pdfUrl;
        this.s3Key = s3Key;
    }
}
