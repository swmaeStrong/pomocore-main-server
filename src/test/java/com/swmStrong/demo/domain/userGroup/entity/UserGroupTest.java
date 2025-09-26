package com.swmStrong.demo.domain.userGroup.entity;

import com.swmStrong.demo.domain.group.entity.Group;
import com.swmStrong.demo.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserGroup 엔티티 테스트")
class UserGroupTest {

    @Test
    @DisplayName("User와 Group으로 UserGroup 생성")
    void shouldCreateUserGroupWithUserAndGroup() {
        // given
        User user = new User("user123", "테스트사용자");
        User owner = new User("owner123", "그룹소유자");
        Group group = Group.builder()
                .owner(owner)
                .name("테스트그룹")
                .tags(Arrays.asList("개발", "스터디"))
                .description("테스트용 그룹입니다")
                .groundRule("규칙을 지켜주세요")
                .isPublic(true)
                .password(null)
                .build();

        // when
        UserGroup userGroup = new UserGroup(user, group);

        // then
        assertThat(userGroup.getUser()).isEqualTo(user);
        assertThat(userGroup.getGroup()).isEqualTo(group);
        assertThat(userGroup.getId()).isNull(); // 아직 영속화되지 않음
    }

    @Test
    @DisplayName("UserGroup의 User와 Group 관계 확인")
    void shouldVerifyUserAndGroupRelationship() {
        // given
        User user = new User("user123", "테스트사용자");
        User owner = new User("owner123", "그룹소유자");
        Group group = Group.builder()
                .owner(owner)
                .name("스터디그룹")
                .tags(Arrays.asList("Java", "Spring"))
                .description("Java Spring 스터디 그룹")
                .groundRule("매일 참여해주세요")
                .isPublic(false)
                .password("1234")
                .build();

        // when
        UserGroup userGroup = new UserGroup(user, group);

        // then
        assertThat(userGroup.getUser().getId()).isEqualTo("user123");
        assertThat(userGroup.getUser().getNickname()).isEqualTo("테스트사용자");
        assertThat(userGroup.getGroup().getName()).isEqualTo("스터디그룹");
        assertThat(userGroup.getGroup().getOwner()).isEqualTo(owner);
        assertThat(userGroup.getGroup().isPublic()).isFalse();
    }

    @Test
    @DisplayName("같은 User와 Group으로 생성한 UserGroup들의 동등성 확인")
    void shouldVerifyEqualityOfUserGroupsWithSameUserAndGroup() {
        // given
        User user = new User("user123", "테스트사용자");
        User owner = new User("owner123", "그룹소유자");
        Group group = Group.builder()
                .owner(owner)
                .name("테스트그룹")
                .tags(Arrays.asList("태그1"))
                .description("설명")
                .groundRule("규칙")
                .isPublic(true)
                .password(null)
                .build();

        // when
        UserGroup userGroup1 = new UserGroup(user, group);
        UserGroup userGroup2 = new UserGroup(user, group);

        // then
        assertThat(userGroup1.getUser()).isEqualTo(userGroup2.getUser());
        assertThat(userGroup1.getGroup()).isEqualTo(userGroup2.getGroup());
        assertThat(userGroup1).isNotEqualTo(userGroup2); // 다른 인스턴스
    }
}