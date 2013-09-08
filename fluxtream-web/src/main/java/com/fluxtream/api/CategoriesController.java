package com.fluxtream.api;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fluxtream.auth.AuthHelper;
import com.fluxtream.domain.Guest;
import com.fluxtream.dto.CategoryDto;
import com.fluxtream.mvc.models.StatusModel;
import com.fluxtream.services.CategoriesService;
import com.fluxtream.services.GuestService;
import com.fluxtream.services.VariablesService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

@Controller
@RequestMapping(value = "/categories")
public class CategoriesController {

	private static final Logger LOGGER = LoggerFactory.getLogger(CategoriesController.class);

	@Autowired
	VariablesService variablesService;

	@Autowired
	CategoriesService categoriesService;

	@Autowired
	GuestService guestService;

	@GET
	@RequestMapping(value = "")
	@Produces({ MediaType.APPLICATION_JSON })
	public @ResponseBody
	String getAllCategories() {
		/*
		 * XXX guest is not longer required, see https://github.com/mikepsinn/Quantimodo/issues/197
		 */
		Guest guest = guestService.getGuestById(AuthHelper.getGuestId());
		List<CategoryDto> categories = categoriesService.getCategories(guest);
		return new Gson().toJson(categories);
	}

	@POST
	@RequestMapping(value = "/category", headers = { "Accept=application/json" })
	@Produces({ MediaType.APPLICATION_JSON })
	public @ResponseBody
	String addCategory(@RequestBody CategoryDto categoryDto) {
		Guest guest = guestService.getGuestById(AuthHelper.getGuestId());
		Gson gson = new Gson();
		JsonObject jsonObject = new JsonObject();
		try {
			Integer id = categoriesService.saveCategory(guest, categoryDto);
			StatusModel result = new StatusModel(true, "Successfully added new category!");

			jsonObject.add("status_model", gson.toJsonTree(result));
			jsonObject.add("id", new JsonPrimitive(id));
			return gson.toJson(jsonObject);
		} catch (DataAccessException e) {
			LOGGER.warn("Error occured while saving new category", e);
			StatusModel result = new StatusModel(false, "This name is used already, please, enter another");
			jsonObject.add("status_model", gson.toJsonTree(result));
			return gson.toJson(jsonObject);
		}
	}

	@POST
	@RequestMapping(value = "/category/{id}")
	@Produces({ MediaType.APPLICATION_JSON })
	public @ResponseBody
	String getCategory(@PathVariable(value = "id") Integer categoryId) {
		Guest guest = guestService.getGuestById(AuthHelper.getGuestId());
		Gson gson = new Gson();
		JsonObject jsonObject = new JsonObject();
		try {
			CategoryDto categoryDto = categoriesService.getCategory(guest, categoryId);
			return gson.toJson(categoryDto);
		} catch (DataAccessException e) {
			LOGGER.warn("Error occured while getting category " + categoryId, e);
			StatusModel result = new StatusModel(false, "An error occured while getting category.");
			jsonObject.add("status_model", gson.toJsonTree(result));
			return gson.toJson(jsonObject);
		}
	}

}
