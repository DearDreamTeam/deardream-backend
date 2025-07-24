package com.deardream.deardream_be.domain.post.entity;

import com.deardream.deardream_be.domain.family.entity.Family;
import com.deardream.deardream_be.domain.user.entity.User;
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
@Table(name = "post")
public class Post extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = true)
    private User author;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    @Column(length = 1000)
    private String content;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> images = new ArrayList<>();


    public void updateContent(String content) {
        this.content = content;
    }



}
