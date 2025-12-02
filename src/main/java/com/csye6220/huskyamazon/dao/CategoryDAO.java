package com.csye6220.huskyamazon.dao;

import com.csye6220.huskyamazon.entity.Category;
import java.util.List;

public interface CategoryDAO {
    void save(Category category);
    Category findById(Long id);
    List<Category> findAll();
}