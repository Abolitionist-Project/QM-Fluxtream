package com.fluxtream.dao;

import java.util.List;

import org.springframework.dao.DataAccessException;

import com.fluxtream.domain.Guest;
import com.fluxtream.dto.CategoryDto;

public interface CategoriesDao {

	Integer saveCategory(Guest guest, CategoryDto categoryDto) throws DataAccessException;

	Integer updateCategory(Guest guest, CategoryDto categoryDto) throws DataAccessException;

	List<CategoryDto> getCategories(Guest guest);

	CategoryDto findCategory(Guest guest, Integer categoryId);
}
