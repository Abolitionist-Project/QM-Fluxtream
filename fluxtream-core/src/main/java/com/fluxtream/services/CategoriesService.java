package com.fluxtream.services;

import java.util.List;

import org.springframework.dao.DataAccessException;

import com.fluxtream.domain.Guest;
import com.fluxtream.dto.CategoryDto;

public interface CategoriesService {

	List<CategoryDto> getCategories(Guest guest);

	/**
	 * Saves category and return id for for it
	 * 
	 * @param guest
	 * @param name
	 * @return id - saved category id
	 */
	Integer saveCategory(Guest guest, CategoryDto categoryDto) throws DataAccessException;

	CategoryDto getCategory(Guest guest, Integer categoryId);
}
