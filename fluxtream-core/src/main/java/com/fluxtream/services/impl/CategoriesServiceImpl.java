package com.fluxtream.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.fluxtream.dao.CategoriesDao;
import com.fluxtream.domain.Guest;
import com.fluxtream.dto.CategoryDto;
import com.fluxtream.services.CategoriesService;

@Service
public class CategoriesServiceImpl implements CategoriesService {

	@Autowired
	CategoriesDao categoriesDao;

	@Override
	public List<CategoryDto> getCategories(Guest guest) {
		return categoriesDao.getCategories(guest);
	}

	@Override
	public Integer saveCategory(Guest guest, CategoryDto categoryDto) throws DataAccessException {
		if (categoryDto.id == null) {
			return categoriesDao.saveCategory(guest, categoryDto);
		}
		return categoriesDao.updateCategory(guest, categoryDto);
	}

	@Override
	public CategoryDto getCategory(Guest guest, Integer categoryId) {
		return categoriesDao.findCategory(guest, categoryId);
	}

}
