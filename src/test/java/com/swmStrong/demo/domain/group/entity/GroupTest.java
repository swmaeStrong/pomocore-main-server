package com.swmStrong.demo.domain.group.entity;

import com.swmStrong.demo.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Group 엔티티 테스트")
class GroupTest {

    @Test
    @DisplayName("Builder로 Group 생성")
    void shouldCreateGroupWithBuilder() {
        // given
        User owner = new User("owner123", "그룹소유자");
        String name = "테스트그룹";
        List<String> tags = Arrays.asList("개발", "스터디");
        String description = "테스트용 그룹입니다";
        String groundRule = "규칙을 지켜주세요";
        boolean isPublic = true;
        String password = null;

        // when
        Group group = Group.builder()
                .owner(owner)
                .name(name)
                .tags(tags)
                .description(description)
                .groundRule(groundRule)
                .isPublic(isPublic)
                .password(password)
                .build();

        // then
        assertThat(group.getOwner()).isEqualTo(owner);
        assertThat(group.getName()).isEqualTo(name);
        assertThat(group.getTags()).isEqualTo(tags);
        assertThat(group.getDescription()).isEqualTo(description);
        assertThat(group.getGroundRule()).isEqualTo(groundRule);
        assertThat(group.isPublic()).isEqualTo(isPublic);
        assertThat(group.getPassword()).isEqualTo(password);
        assertThat(group.getMemberCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("설명 업데이트")
    void shouldUpdateDescription() {
        // given
        User owner = new User("owner123", "그룹소유자");
        Group group = Group.builder()
                .owner(owner)
                .name("테스트그룹")
                .tags(Arrays.asList("태그"))
                .description("기존설명")
                .groundRule("규칙")
                .isPublic(true)
                .password(null)
                .build();
        String newDescription = "새로운설명";

        // when
        group.updateDescription(newDescription);

        // then
        assertThat(group.getDescription()).isEqualTo(newDescription);
    }

    @Test
    @DisplayName("그라운드룰 업데이트")
    void shouldUpdateGroundRule() {
        // given
        User owner = new User("owner123", "그룹소유자");
        Group group = Group.builder()
                .owner(owner)
                .name("테스트그룹")
                .tags(Arrays.asList("태그"))
                .description("설명")
                .groundRule("기존규칙")
                .isPublic(true)
                .password(null)
                .build();
        String newGroundRule = "새로운규칙";

        // when
        group.updateGroundRule(newGroundRule);

        // then
        assertThat(group.getGroundRule()).isEqualTo(newGroundRule);
    }

    @Test
    @DisplayName("공개 여부 업데이트")
    void shouldUpdateIsPublic() {
        // given
        User owner = new User("owner123", "그룹소유자");
        Group group = Group.builder()
                .owner(owner)
                .name("테스트그룹")
                .tags(Arrays.asList("태그"))
                .description("설명")
                .groundRule("규칙")
                .isPublic(true)
                .password(null)
                .build();

        // when
        group.updateIsPublic(false);

        // then
        assertThat(group.isPublic()).isFalse();
    }

    @Test
    @DisplayName("그룹명 업데이트")
    void shouldUpdateName() {
        // given
        User owner = new User("owner123", "그룹소유자");
        Group group = Group.builder()
                .owner(owner)
                .name("기존그룹명")
                .tags(Arrays.asList("태그"))
                .description("설명")
                .groundRule("규칙")
                .isPublic(true)
                .password(null)
                .build();
        String newName = "새로운그룹명";

        // when
        group.updateName(newName);

        // then
        assertThat(group.getName()).isEqualTo(newName);
    }

    @Test
    @DisplayName("태그 업데이트")
    void shouldUpdateTags() {
        // given
        User owner = new User("owner123", "그룹소유자");
        Group group = Group.builder()
                .owner(owner)
                .name("테스트그룹")
                .tags(Arrays.asList("기존태그"))
                .description("설명")
                .groundRule("규칙")
                .isPublic(true)
                .password(null)
                .build();
        List<String> newTags = Arrays.asList("새태그1", "새태그2");

        // when
        group.updateTags(newTags);

        // then
        assertThat(group.getTags()).isEqualTo(newTags);
        assertThat(group.getTags()).isNotSameAs(newTags); // 새로운 ArrayList로 복사됨
    }

    @Test
    @DisplayName("소유자 업데이트")
    void shouldUpdateOwner() {
        // given
        User originalOwner = new User("owner123", "기존소유자");
        User newOwner = new User("newOwner123", "새소유자");
        Group group = Group.builder()
                .owner(originalOwner)
                .name("테스트그룹")
                .tags(Arrays.asList("태그"))
                .description("설명")
                .groundRule("규칙")
                .isPublic(true)
                .password(null)
                .build();

        // when
        group.updateOwner(newOwner);

        // then
        assertThat(group.getOwner()).isEqualTo(newOwner);
        assertThat(group.getOwner().getNickname()).isEqualTo("새소유자");
    }

    @Test
    @DisplayName("멤버 수 증가")
    void shouldIncreaseMemberCount() {
        // given
        User owner = new User("owner123", "그룹소유자");
        Group group = Group.builder()
                .owner(owner)
                .name("테스트그룹")
                .tags(Arrays.asList("태그"))
                .description("설명")
                .groundRule("규칙")
                .isPublic(true)
                .password(null)
                .build();

        // when
        group.increaseMemberCount();
        group.increaseMemberCount();

        // then
        assertThat(group.getMemberCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("멤버 수 감소")
    void shouldDecreaseMemberCount() {
        // given
        User owner = new User("owner123", "그룹소유자");
        Group group = Group.builder()
                .owner(owner)
                .name("테스트그룹")
                .tags(Arrays.asList("태그"))
                .description("설명")
                .groundRule("규칙")
                .isPublic(true)
                .password(null)
                .build();
        group.increaseMemberCount();
        group.increaseMemberCount();
        group.increaseMemberCount();

        // when
        group.decreaseMemberCount();

        // then
        assertThat(group.getMemberCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("비밀번호 업데이트")
    void shouldUpdatePassword() {
        // given
        User owner = new User("owner123", "그룹소유자");
        Group group = Group.builder()
                .owner(owner)
                .name("테스트그룹")
                .tags(Arrays.asList("태그"))
                .description("설명")
                .groundRule("규칙")
                .isPublic(false)
                .password("기존비밀번호")
                .build();
        String newPassword = "새비밀번호";

        // when
        group.updatePassword(newPassword);

        // then
        assertThat(group.getPassword()).isEqualTo(newPassword);
    }

    @Test
    @DisplayName("복합 업데이트 시나리오")
    void shouldHandleComplexUpdateScenario() {
        // given
        User owner = new User("owner123", "그룹소유자");
        Group group = Group.builder()
                .owner(owner)
                .name("기존그룹")
                .tags(Arrays.asList("기존태그"))
                .description("기존설명")
                .groundRule("기존규칙")
                .isPublic(true)
                .password(null)
                .build();

        // when
        group.updateName("새그룹명");
        group.updateDescription("새설명");
        group.updateGroundRule("새규칙");
        group.updateIsPublic(false);
        group.updatePassword("새비밀번호");
        group.updateTags(Arrays.asList("새태그1", "새태그2"));
        group.increaseMemberCount();

        // then
        assertThat(group.getName()).isEqualTo("새그룹명");
        assertThat(group.getDescription()).isEqualTo("새설명");
        assertThat(group.getGroundRule()).isEqualTo("새규칙");
        assertThat(group.isPublic()).isFalse();
        assertThat(group.getPassword()).isEqualTo("새비밀번호");
        assertThat(group.getTags()).containsExactly("새태그1", "새태그2");
        assertThat(group.getMemberCount()).isEqualTo(1);
    }
}