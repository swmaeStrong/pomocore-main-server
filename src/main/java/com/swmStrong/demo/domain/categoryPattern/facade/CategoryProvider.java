package com.swmStrong.demo.domain.categoryPattern.facade;

import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

public interface CategoryProvider {
    List<String> getCategories();
    String getCategoryById(ObjectId categoryId);
    ObjectId getCategoryIdByCategory(String category);
    Map<ObjectId, String> getCategoryMapById();
    Map<String, ObjectId> getCategoryMapByCategory();
}
