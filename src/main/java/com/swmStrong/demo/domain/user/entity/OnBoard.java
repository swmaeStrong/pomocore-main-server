package com.swmStrong.demo.domain.user.entity;

import com.swmStrong.demo.domain.common.entity.BaseEntity;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Getter
@Document(collection = "on_board")
public class OnBoard extends BaseEntity {
    @Id
    private ObjectId id;

    private String userId;

    private List<Question> questions = new ArrayList<>();

    @Getter
    public static class Question {
        private String question;
        private String answer;

        @Builder
        public Question(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }
    }

    @Builder
    public OnBoard(String userId, List<Question> questions) {
        this.userId = userId;
        this.questions = questions;
    }
}
