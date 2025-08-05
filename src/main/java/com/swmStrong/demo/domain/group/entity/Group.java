package com.swmStrong.demo.domain.group.entity;

import com.swmStrong.demo.domain.common.entity.BaseEntity;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.userGroup.entity.UserGroup;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "\"group\"")
@Entity
public class Group extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    private List<UserGroup> users = new ArrayList<>();

    @Column(name = "name", nullable = false, unique = true, length = 32)
    private String name;

    @Column(columnDefinition = "text[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> tags;

    @Column(name = "description", length = 1024)
    private String description;

    @Column(name = "ground_rule", length = 1024)
    private String groundRule;

    @Column(name = "memberCount")
    private int memberCount = 0;

    @Column(name = "is_public")
    private boolean isPublic = true;

    @Column(name = "password")
    private String password;

    @Builder
    public Group(User owner, String name, List<String> tags, String description, String groundRule, boolean isPublic, String password) {
        this.owner = owner;
        this.name = name;
        this.tags = tags;
        this.description = description;
        this.groundRule = groundRule;
        this.isPublic = isPublic;
        this.password = password;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updateGroundRule(String groundRule) {
        this.groundRule = groundRule;
    }

    public void updateIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateTags(List<String> tags) {
        this.tags = new ArrayList<>(tags);
    }

    public void updateOwner(User owner) {
        this.owner = owner;
    }

    public void increaseMemberCount() {
        this.memberCount++;
    }

    public void decreaseMemberCount() {
        this.memberCount--;
    }

    public void updatePassword(String password) {
        this.password = password;
    }
}
