package com.example.nagoyameshi.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.CategoryService;
import com.example.nagoyameshi.service.RestaurantService;

@Controller
public class HomeController {
	private final RestaurantService restaurantService;
	private final CategoryService categoryService;

	public HomeController(RestaurantService restaurantService, CategoryService categoryService) {
		super();
		this.restaurantService = restaurantService;
		this.categoryService = categoryService;
	}

	@GetMapping("/")
	public String index(Model model,
			@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {

		if (userDetailsImpl != null && userDetailsImpl.getUser().getRole().getName().equals("ROLE_ADMIN_MEMBER")) {
			return "redirect:/admin";
		}

		// 名古屋駅の座標
		final Double NAGOYA_LAT = 35.170915;
		final Double NAGOYA_LNG = 136.881537;

		Page<Restaurant> highlyRatedRestaurants = restaurantService
				.findAllRestaurantsByOrderByAverageScoreDesc(PageRequest.of(0, 6));
		Page<Restaurant> newRestaurants = restaurantService
				.findAllRestaurantsByOrderByCreatedAtDesc(PageRequest.of(0, 6));
		Page<Restaurant> nearbyRestaurants = restaurantService
				.findAllRestaurantsOrderByDistanceAsc(PageRequest.of(0, 6));

		// 距離を計算して設定
		for (Restaurant restaurant : nearbyRestaurants.getContent()) {
			if (restaurant.getLatitude() != null && restaurant.getLongitude() != null) {
				double distance = calculateDistance(NAGOYA_LAT, NAGOYA_LNG,
						restaurant.getLatitude(), restaurant.getLongitude());
				restaurant.setDistance(distance);
			}
		}

		// カテゴリ名を持つCategoryエンティティを取得
		Category washoku = categoryService.findFirstCategoryByName("和食");
		Category udon = categoryService.findFirstCategoryByName("うどん");
		Category don = categoryService.findFirstCategoryByName("丼物");
		Category ramen = categoryService.findFirstCategoryByName("ラーメン");
		Category oden = categoryService.findFirstCategoryByName("おでん");
		Category fried = categoryService.findFirstCategoryByName("揚げ物");

		List<Category> categories = categoryService.findAllCategories();

		// Modelにデータを追加
		model.addAttribute("highlyRatedRestaurants", highlyRatedRestaurants);
		model.addAttribute("newRestaurants", newRestaurants);
		model.addAttribute("nearbyRestaurants", nearbyRestaurants);

		model.addAttribute("washoku", washoku);
		model.addAttribute("udon", udon);
		model.addAttribute("don", don);
		model.addAttribute("ramen", ramen);
		model.addAttribute("oden", oden);
		model.addAttribute("fried", fried);
		model.addAttribute("categories", categories);

		return "index";
	}

	// Haversineの公式を使用して2点間の距離を計算（km）
	private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
		final int EARTH_RADIUS = 6371; // 地球の半径（km）

		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
				Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
						Math.sin(dLng / 2) * Math.sin(dLng / 2);

		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = EARTH_RADIUS * c;

		return Math.round(distance * 100.0) / 100.0; // 小数第2位まで丸める
	}

}
