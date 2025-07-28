package com.swmStrong.demo.domain.group.entity;

import com.swmStrong.demo.domain.common.entity.BaseEntity;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.userGroup.entity.UserGroup;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "group")
@Entity
public class Group extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    private List<UserGroup> users = new ArrayList<>();

    @Column(name = "name", nullable = false, unique = true, length = 32)
    private String name;

    @Column(name = "tag", nullable = false, length = 16)
    private String tag;

    @Column(name = "description", length = 1024)
    private String description;

    @Column(name = "ground_rule", length = 1024)
    private String groundRule;

    @Column(name = "is_public")
    private boolean isPublic = true;

    @Builder
    public Group(User user, String name, String tag, String description, String groundRule, boolean isPublic) {
        this.user = user;
        this.name = name;
        this.tag = tag;
        this.description = description;
        this.groundRule = groundRule;
        this.isPublic = isPublic;
    }
}
