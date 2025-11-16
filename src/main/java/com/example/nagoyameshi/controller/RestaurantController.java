package com.example.nagoyameshi.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.service.CategoryService;
import com.example.nagoyameshi.service.RestaurantService;

@Controller
public class RestaurantController {
	private final RestaurantService restaurantService;
	private final CategoryService categoryService;
	
	public RestaurantController(RestaurantService restaurantService, CategoryService categoryService) {
		super();
		this.restaurantService = restaurantService;
		this.categoryService = categoryService;
	}

	@GetMapping("/restaurants")
	public String index(
			@RequestParam(name = "keyword", required =  false) String keyword,
			@RequestParam(name = "categoryId", required =  false) Integer categoryId,
			@RequestParam(name = "price", required =  false) Integer price,
			@RequestParam(name = "order", required =  false) String order,
			@PageableDefault(page = 0,size = 15, sort = "id", direction = Direction.ASC)Pageable pageable,
			Model model) {
		
		Page<Restaurant> restaurantPage;

	       if (keyword != null && !keyword.isEmpty()) {
	           if (order != null && order.equals("lowestPriceAsc")) {
	               restaurantPage = restaurantService.findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByLowestPriceAsc(keyword, keyword, keyword, pageable);
	           } else {
	               restaurantPage = restaurantService.findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByCreatedAtDesc(keyword, keyword, keyword, pageable);
	           }
	       } else if (categoryId != null) {
	           if (order != null && order.equals("lowestPriceAsc")) {
	               restaurantPage = restaurantService.findRestaurantsByCategoryIdOrderByLowestPriceAsc(categoryId, pageable);
	           } else {
	               restaurantPage = restaurantService.findRestaurantsByCategoryIdOrderByCreatedAtDesc(categoryId, pageable);
	           }
	       } else if (price != null) {
	           if (order != null && order.equals("lowestPriceAsc")) {
	               restaurantPage = restaurantService.findRestaurantsByLowestPriceLessThanEqualOrderByLowestPriceAsc(price, pageable);
	           } else {
	               restaurantPage = restaurantService.findRestaurantsByLowestPriceLessThanEqualOrderByCreatedAtDesc(price, pageable);
	           }
	       } else {
	           if (order != null && order.equals("lowestPriceAsc")) {
	               restaurantPage = restaurantService.findAllRestaurantsByOrderByLowestPriceAsc(pageable);
	           } else {
	               restaurantPage = restaurantService.findAllRestaurantsByOrderByCreatedAtDesc(pageable);
	           }
	       }

	       List<Category> categories = categoryService.findAllCategories();
	       model.addAttribute("restaurantPage", restaurantPage);
	       model.addAttribute("categories", categories);
	       model.addAttribute("keyword", keyword);
	       model.addAttribute("categoryId", categoryId);
	       model.addAttribute("price", price);
	       model.addAttribute("order", order);

		return "restaurants/index";
	}
}
