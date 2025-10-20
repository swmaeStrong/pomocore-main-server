package com.swmStrong.demo.domain.user.service;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.user.dto.OnBoardAnswerCountDto;
import com.swmStrong.demo.domain.user.dto.OnBoardRequestDto;
import com.swmStrong.demo.domain.user.dto.OnBoardResponseDto;
import com.swmStrong.demo.domain.user.dto.OnBoardStatisticsResponseDto;
import com.swmStrong.demo.domain.user.entity.OnBoard;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.repository.OnBoardRepository;
import com.swmStrong.demo.domain.user.repository.UserRepository;
import com.swmStrong.demo.infra.redis.repository.RedisRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.swmStrong.demo.domain.user.service.UserServiceImpl.USER_INFO_FORMAT;

@Service
public class OnBoardServiceImpl implements OnBoardService {
    private final UserRepository userRepository;
    private final OnBoardRepository onBoardRepository;
    private final RedisRepository redisRepository;

    public OnBoardServiceImpl(
            UserRepository userRepository,
            OnBoardRepository onBoardRepository,
            RedisRepository redisRepository
    ) {
        this.userRepository = userRepository;
        this.onBoardRepository = onBoardRepository;
        this.redisRepository = redisRepository;
    }

    @Override
    @Transactional
    public void save(String userId, OnBoardRequestDto onBoardRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        //TODO: 유저가 온보딩 페이지가 다시 들어올 수 있는가? 다시 들어왔을 가능성에 대해 생각해보기

        OnBoard onBoard = OnBoard.builder()
                .userId(userId)
                .questions(onBoardRequestDto.questions().stream()
                        .map(q -> OnBoard.Question.builder()
                                .question(q.getQuestion())
                                .answer(q.getAnswer())
                                .build())
                        .toList()
                ).build();

        user.completeOnBoard();

        userRepository.save(user);
        onBoardRepository.save(onBoard);
        redisRepository.deleteData(String.format(USER_INFO_FORMAT, userId));
    }


    @Override
    public List<OnBoardStatisticsResponseDto> getCount() {
        List<OnBoard> onBoards = onBoardRepository.findAll();

        Map<String, Map<String, Integer>> questionAnswerCountMap = new HashMap<>();

        for (OnBoard onBoard : onBoards) {
            for (OnBoard.Question question : onBoard.getQuestions()) {
                String questionText = question.getQuestion();
                String answerText = question.getAnswer();

                questionAnswerCountMap
                    .computeIfAbsent(questionText, k -> new HashMap<>())
                    .merge(answerText, 1, Integer::sum);
            }
        }

        return questionAnswerCountMap.entrySet().stream()
            .map(entry -> {
                String questionText = entry.getKey();
                List<OnBoardAnswerCountDto> answerCounts = entry.getValue().entrySet().stream()
                    .map(answerEntry -> new OnBoardAnswerCountDto(answerEntry.getKey(), answerEntry.getValue()))
                    .collect(Collectors.toList());

                return new OnBoardStatisticsResponseDto(questionText, answerCounts);
            })
            .collect(Collectors.toList());
    }

    public OnBoardResponseDto getUserOnBoard(String userId) {
        return OnBoardResponseDto.from(onBoardRepository.findByUserId(userId));
    }
}
