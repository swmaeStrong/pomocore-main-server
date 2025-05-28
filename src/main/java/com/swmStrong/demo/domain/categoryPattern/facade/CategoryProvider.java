package com.swmStrong.demo.domain.categoryPattern.facade;

import org.bson.types.ObjectId;

import java.util.List;

public interface CategoryProvider {
    List<String> getCategories();
    String getCategoryById(ObjectId categoryId);
    ObjectId getCategoryIdByCategory(String category);
}
