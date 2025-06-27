package com.deardream.deardream_be.domain.family;

import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;


@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "family")
public class Family extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "leaer_id")
    private User leader;

    private String familyLink;


}
