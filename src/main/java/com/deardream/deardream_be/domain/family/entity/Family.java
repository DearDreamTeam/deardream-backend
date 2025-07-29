package com.deardream.deardream_be.domain.family.entity;

import com.deardream.deardream_be.domain.archive.entity.MonthlyArchive;
import com.deardream.deardream_be.domain.post.Post;
import com.deardream.deardream_be.domain.recipient.entity.Recipient;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;

import java.util.List;

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
    @JoinColumn(name = "leader_id")
    private User leader;

    private String familyLink;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "has_subscribed")
    private Boolean hasSubscribed;

    @OneToMany(mappedBy = "family", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Post> posts;

    @OneToMany(mappedBy = "family", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Recipient> recipients;

    @OneToMany(mappedBy = "family", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<MonthlyArchive> monthlyArchives;

    // 가족 등록
    public void startFamilyRegistration(User leader, String familyLink) {
        this.leader = leader;
        this.familyLink = familyLink;
    }


    // familyLink 업데이트
    public void updateFamilyInviteLink(String inviteLinkToken) {
        this.familyLink = inviteLinkToken;
    }

    // family 활성화 상태 업데이트
    public void setFamilyActive() {
        this.isActive = true;
        this.hasSubscribed = true;
    }

    public void setFamilyDeActive() {
        this.isActive = false;
    }
}