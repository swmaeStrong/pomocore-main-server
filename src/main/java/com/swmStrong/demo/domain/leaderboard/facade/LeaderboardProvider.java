package com.swmStrong.demo.domain.leaderboard.facade;

import com.swmStrong.demo.domain.common.enums.PeriodType;
import com.swmStrong.demo.domain.group.dto.GroupLeaderboardMember;
import com.swmStrong.demo.domain.leaderboard.dto.LeaderboardResult;
import com.swmStrong.demo.domain.leaderboard.repository.LeaderboardCache;
import com.swmStrong.demo.domain.leaderboard.service.LeaderboardService;
import com.swmStrong.demo.domain.user.dto.OnlineRequestDto;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.service.UserService;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class LeaderboardProvider {

    private final LeaderboardCache leaderboardCache;
    private final LeaderboardService leaderboardService;
    private final UserService userService;

    public LeaderboardProvider(LeaderboardCache leaderboardCache, LeaderboardService leaderboardService, UserService userService) {
        this.leaderboardCache = leaderboardCache;
        this.leaderboardService = leaderboardService;
        this.userService = userService;
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

        Map<String, User> userMap = groupUsers.stream()
                .collect(java.util.stream.Collectors.toMap(User::getId, user -> user));

        List<String> groupUserIds = groupUsers.stream().map(User::getId).toList();
        Map<String, OnlineRequestDto> onlineDetails = userService.getUserOnlineDetails(groupUserIds);
        
        List<GroupLeaderboardMember> members = new ArrayList<>();
        int groupRank = 1;

        for (ZSetOperations.TypedTuple<String> tuple : allMembers) {
            String userId = tuple.getValue();
            if (userMap.containsKey(userId)) {
                User user = userMap.get(userId);
                Double score = tuple.getScore();
                OnlineRequestDto onlineRequestDto = onlineDetails.get(userId);

                long currentTime = System.currentTimeMillis() / 1000;
                boolean isOnline = false;
                double lastActivityTime = 0.0;
                
                if (onlineRequestDto != null) {
                    lastActivityTime = onlineRequestDto.timestamp() + onlineRequestDto.sessionMinutes() * 60.0;
                    if (onlineRequestDto.sessionMinutes() == 0) {
                        // 즉시 종료 (dropOut) - 오프라인 처리 (이미 false로 초기화됨)
                    } else {
                        // 일반적인 경우 - 5분 여유 시간
                        isOnline = (currentTime - lastActivityTime) < 300;
                    }
                }
                
                members.add(GroupLeaderboardMember.builder()
                        .userId(userId)
                        .nickname(user.getNickname())
                        .profileImageUrl(user.getProfileImageUrl())
                        .score(score != null ? score : 0.0)
                        .rank(groupRank++)
                        .isOnline(isOnline)
                        .lastActivityTime(lastActivityTime)
                        .details(new ArrayList<>())
                        .build());
            }
        }
        
        return members;
    }
}
