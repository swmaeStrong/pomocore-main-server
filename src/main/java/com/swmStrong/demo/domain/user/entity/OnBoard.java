package com.swmStrong.demo.domain.user.entity;

import com.swmStrong.demo.domain.common.entity.BaseEntity;
import jakarta.persistence.Id;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;


@Document(collection = "on_board")
public class OnBoard extends BaseEntity {
    @Id
    private ObjectId id;

    private String userId;

    private List<Question> questions = new ArrayList<>();

    public static class Question {
        private String question;

        private String answer;
    }
}
