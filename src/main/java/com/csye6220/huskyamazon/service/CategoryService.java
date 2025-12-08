package com.csye6220.huskyamazon.service;

import com.csye6220.huskyamazon.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();
    Category getCategoryById(Long id);

    // --- ⭐ 新增：添加分类 ---
    void addCategory(Category category);

    void deleteCategory(Long id);
}