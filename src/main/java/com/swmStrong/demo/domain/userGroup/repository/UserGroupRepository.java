package com.swmStrong.demo.domain.userGroup.repository;

import com.swmStrong.demo.domain.group.dto.GroupMember;
import com.swmStrong.demo.domain.group.entity.Group;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.userGroup.entity.UserGroup;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {
    
    boolean existsByUserAndGroup(User user, Group group);

    List<UserGroup> findByGroup(Group group);

    List<UserGroup> findByUser(User user);

    @Query("SELECT new com.swmStrong.demo.domain.group.dto.GroupMember(u.id, u.nickname, u.profileImageUrl, null, null) FROM UserGroup ug JOIN ug.user u WHERE ug.group = :group")
    List<GroupMember> findByGroupWithUser(@Param("group") Group group);

    @Query("SELECT u FROM UserGroup ug JOIN ug.user u WHERE ug.group = :group")
    List<User> findUsersByGroup(@Param("group") Group group);

    Optional<UserGroup> findByUserAndGroup(User user, Group group);
    
    @Modifying
    void deleteByUserAndGroup(User user, Group group);

    boolean existsByGroup(Group group);

    long countByGroup(Group group);

    @Modifying
    void deleteByGroup(Group group);
}