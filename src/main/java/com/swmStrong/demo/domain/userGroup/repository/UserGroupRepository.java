package com.swmStrong.demo.domain.userGroup.repository;

import com.swmStrong.demo.domain.group.entity.Group;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.userGroup.entity.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {
    
    boolean existsByUserAndGroup(User user, Group group);
    
    Optional<UserGroup> findByUserAndGroup(User user, Group group);
    
    void deleteByUserAndGroup(User user, Group group);
}