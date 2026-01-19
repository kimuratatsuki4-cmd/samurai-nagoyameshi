package com.example.nagoyameshi.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.CategoryRestaurant;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.repository.CategoryRestaurantRepository;

@Service
public class CategoryRestaurantService {
	private final CategoryRestaurantRepository categoryRestaurantRepository;
	private final CategoryService categoryService;

	public CategoryRestaurantService(CategoryRestaurantRepository categoryRestaurantRepository,
			CategoryService categoryService) {
		this.categoryRestaurantRepository = categoryRestaurantRepository;
		this.categoryService = categoryService;

		// TODO 自動生成されたコンストラクター・スタブ
	}

	public List<Integer> findCategoryIdsByRestaurantOrderByIdAsc(Restaurant restaurant) {
		return categoryRestaurantRepository.findCategoryIdsByRestaurantOrderByIdAsc(restaurant);
	}

	@Transactional
	public void createCategoriesRestaurants(List<Integer> categoryIds, Restaurant restaurant) {
		if (categoryIds == null) {
			return;
		}
		for (Integer categoryId : categoryIds) {
			if (categoryId != null) {
				Optional<Category> optionalCategory = categoryService.findCategoryById(categoryId);
				if (optionalCategory.isPresent()) {
					categoryRestaurantRepository.findByCategoryAndRestaurant(optionalCategory.get(), restaurant);
				} else {
					CategoryRestaurant categoryRestaurant = new CategoryRestaurant();
					categoryRestaurant.setCategory(optionalCategory.get());
					categoryRestaurant.setRestaurant(restaurant);
					categoryRestaurantRepository.save(Objects.requireNonNull(categoryRestaurant));
				}
			}
		}
	}

	@Transactional
	public void syncCategoriesRestaurants(List<Integer> newCategoryIds, Restaurant restaurant) {
		List<CategoryRestaurant> currentCategoryRestaurants = categoryRestaurantRepository
				.findByRestaurantOrderByIdAsc(restaurant);
		// newCategoryIdsがnullの場合はすべてのエンティティを削除する
		if (newCategoryIds == null) {
			for (CategoryRestaurant categoryRestaurant : currentCategoryRestaurants) {
				if (categoryRestaurant != null) {
					categoryRestaurantRepository.delete(Objects.requireNonNull(categoryRestaurant));
				}
			}
			return;
		}
		// 既存のエンティティが新しいリストに存在しない場合は削除する
		for (CategoryRestaurant categoryRestaurant : currentCategoryRestaurants) {
			if (categoryRestaurant != null && !newCategoryIds.contains(categoryRestaurant.getCategory().getId())) {
				categoryRestaurantRepository.delete(Objects.requireNonNull(categoryRestaurant));
			}
		}
		// 新しいカテゴリとの結びつけがある場合、新しいエンティティを作成する。
		for (Integer categoryId : newCategoryIds) {
			if (categoryId != null) {
				Optional<Category> optionalCategoryOptional = categoryService.findCategoryById(categoryId);
				if (optionalCategoryOptional.isPresent()) {
					Category category = optionalCategoryOptional.get();
					Optional<CategoryRestaurant> optionalCategoryRestaurant = categoryRestaurantRepository
							.findByCategoryAndRestaurant(category, restaurant);
					if (optionalCategoryRestaurant.isEmpty()) {
						CategoryRestaurant categoryRestaurant = new CategoryRestaurant();
						categoryRestaurant.setRestaurant(restaurant);
						categoryRestaurant.setCategory(category);
						categoryRestaurantRepository.save(Objects.requireNonNull(categoryRestaurant));
					}

				}
			}

		}

	}

}
