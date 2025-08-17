package com.swmStrong.demo.domain.leaderboard.facade;

import com.swmStrong.demo.domain.common.enums.PeriodType;
import com.swmStrong.demo.domain.group.dto.GroupLeaderboardMember;
import com.swmStrong.demo.domain.leaderboard.dto.LeaderboardResult;
import com.swmStrong.demo.domain.leaderboard.entity.Leaderboard;
import com.swmStrong.demo.domain.leaderboard.repository.LeaderboardCache;
import com.swmStrong.demo.domain.leaderboard.repository.LeaderboardRepository;
import com.swmStrong.demo.domain.leaderboard.service.LeaderboardService;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.service.UserService;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class LeaderboardProvider {

    private final LeaderboardCache leaderboardCache;
    private final LeaderboardService leaderboardService;
    private final LeaderboardRepository leaderboardRepository;

    public LeaderboardProvider(LeaderboardCache leaderboardCache, LeaderboardService leaderboardService, 
                              UserService userService, LeaderboardRepository leaderboardRepository) {
        this.leaderboardCache = leaderboardCache;
        this.leaderboardService = leaderboardService;
        this.leaderboardRepository = leaderboardRepository;
    }

    public double getUserScore(String userId, String category, LocalDate date, PeriodType periodType) {
        String key = leaderboardService.generateKey(category, date, periodType);
        return leaderboardCache.findScoreByUserId(key, userId);
    }
    
    public Map<String, Double> getUserScores(List<String> userIds, String category, LocalDate date, PeriodType periodType) {
        String key = leaderboardService.generateKey(category, date, periodType);
        return leaderboardCache.findScoresByUserIds(key, userIds);
    }
    
    public Map<String, LeaderboardResult> getUserResults(List<String> userIds, String category, LocalDate date, PeriodType periodType) {
        String key = leaderboardService.generateKey(category, date, periodType);
        return leaderboardCache.findResultsByUserIds(key, userIds);
    }

    public List<GroupLeaderboardMember> getGroupLeaderboardMembers(List<User> groupUsers, String category, LocalDate date, PeriodType periodType) {
        String key = leaderboardService.generateKey(category, date, periodType);
        Set<ZSetOperations.TypedTuple<String>> allMembers = leaderboardCache.findAll(key);
        if (!allMembers.isEmpty()) {
            return getGroupLeaderboardMembersByRedis(allMembers, groupUsers);
        }
        return getGroupLeaderboardMembersByDB(groupUsers, category, date, periodType);
    }

    private List<GroupLeaderboardMember> getGroupLeaderboardMembersByDB(List<User> groupUsers, String category, 
                                                                        LocalDate date, PeriodType periodType) {
        String periodKey = generatePeriodKey(date, periodType);
        List<String> userIds = groupUsers.stream()
                .map(User::getId)
                .toList();
        
        List<Leaderboard> leaderboards = leaderboardRepository
                .findByCategoryIdAndPeriodTypeAndPeriodKeyAndUserIdInOrderByScoreDesc(
                        category, periodType, periodKey, userIds);
        
        if (leaderboards.isEmpty()) {
            return new ArrayList<>();
        }
        
        Map<String, User> userMap = groupUsers.stream()
                .collect(java.util.stream.Collectors.toMap(User::getId, user -> user));
        
        List<GroupLeaderboardMember> members = new ArrayList<>();
        int groupRank = 1;
        
        for (Leaderboard leaderboard : leaderboards) {
            User user = userMap.get(leaderboard.getUserId());
            if (user != null) {
                members.add(GroupLeaderboardMember.builder()
                        .userId(leaderboard.getUserId())
                        .nickname(user.getNickname())
                        .profileImageUrl(user.getProfileImageUrl())
                        .score(leaderboard.getScore())
                        .rank(groupRank++)
                        .details(new ArrayList<>())
                        .build());
            }
        }
        
        return members;
    }
    
    private String generatePeriodKey(LocalDate date, PeriodType periodType) {
        return switch (periodType) {
            case DAILY -> date.toString();
            case WEEKLY -> {
                WeekFields weekFields = WeekFields.ISO;
                int year = date.get(weekFields.weekBasedYear());
                int weekNumber = date.get(weekFields.weekOfWeekBasedYear());
                yield String.format("%d-W%d", year, weekNumber);
            }
            case MONTHLY -> {
                int year = date.getYear();
                int month = date.getMonthValue();
                yield String.format("%d-M%d", year, month);
            }
        };
    }

    private List<GroupLeaderboardMember> getGroupLeaderboardMembersByRedis(Set<ZSetOperations.TypedTuple<String>> allMembers, List<User> groupUsers) {

        Map<String, User> userMap = groupUsers.stream()
                .collect(java.util.stream.Collectors.toMap(User::getId, user -> user));
        
        List<GroupLeaderboardMember> members = new ArrayList<>();
        int groupRank = 1;

        for (ZSetOperations.TypedTuple<String> tuple : allMembers) {
            String userId = tuple.getValue();
            if (userMap.containsKey(userId)) {
                User user = userMap.get(userId);
                Double score = tuple.getScore();
                
                members.add(GroupLeaderboardMember.builder()
                        .userId(userId)
                        .nickname(user.getNickname())
                        .profileImageUrl(user.getProfileImageUrl())
                        .score(score != null ? score : 0.0)
                        .rank(groupRank++)
                        .details(new ArrayList<>())
                        .build());
            }
        }
        return members;
    }

    public void increaseSessionCount(String userId, LocalDate date) {
        leaderboardService.increaseSessionCount(userId, date);
    }

    public void increaseSessionScore(String userId, LocalDate date, int score) {
        leaderboardService.increaseSessionScore(userId, date, score);
    }
}
