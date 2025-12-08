package com.csye6220.huskyamazon.service.impl;

import com.csye6220.huskyamazon.dao.CategoryDAO;
import com.csye6220.huskyamazon.entity.Category;
import com.csye6220.huskyamazon.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryDAO categoryDAO;

    @Autowired
    public CategoryServiceImpl(CategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        return categoryDAO.findById(id);
    }

    @Override
    @Transactional
    public void addCategory(Category category) {
        categoryDAO.save(category);
    }

    /**
     * ⭐ 删除分类
     * 注意：删除前应检查该分类下是否有商品
     */
    @Override
    @Transactional
    public void deleteCategory(Long id) {
        // 方式2: 删除前检查是否存在（更安全）
        Category category = categoryDAO.findById(id);
        if (category == null) {
             throw new RuntimeException("Category not found with id: " + id);
         }
         categoryDAO.delete(id);
    }
}