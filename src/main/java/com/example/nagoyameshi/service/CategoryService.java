package com.example.nagoyameshi.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.form.CategoryEditForm;
import com.example.nagoyameshi.form.CategoryRegisterForm;
import com.example.nagoyameshi.repository.CategoryRepository;

@Service
public class CategoryService {
	private final CategoryRepository categoryRepository;

	public CategoryService(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	// すべてのカテゴリをページングされた状態で取得する
	public Page<Category> findAllCategories(Pageable pageable) {
		return categoryRepository.findAll(Objects.requireNonNull(pageable));
	}

	// 指定されたキーワードをカテゴリ名に含むカテゴリを、ページングされた状態で取得する
	public Page<Category> findCategoriesByNameLike(String keyword, Pageable pageable) {
		return categoryRepository.findByNameLike("%" + keyword + "%", Objects.requireNonNull(pageable));
	}

	// 指定したidを持つカテゴリを取得する
	public Optional<Category> findCategoryById(Integer id) {
		return categoryRepository.findById(Objects.requireNonNull(id));
	}

	// カテゴリのレコード数を取得する
	public long countCategories() {
		return categoryRepository.count();
	}

	// idが最も大きいカテゴリを取得する
	public Category findFirstCategoryByOrderByIdDesc() {
		return categoryRepository.findFirstByOrderByIdDesc();
	}

	@Transactional
	public void createCategory(CategoryRegisterForm categoryRegisterForm) {
		Category category = new Category();

		category.setName(categoryRegisterForm.getName());

		categoryRepository.save(Objects.requireNonNull(category));
	}

	@Transactional
	public void updateCategory(CategoryEditForm categoryEditForm, Category category) {
		category.setName(categoryEditForm.getName());

		categoryRepository.save(Objects.requireNonNull(category));
	}

	@Transactional
	public void deleteCategory(Category category) {
		if (category != null) {
			categoryRepository.delete(Objects.requireNonNull(category));
		}
	}

	public List<Category> findAllCategories() {
		return categoryRepository.findAll();

	}

	public Category findFirstCategoryByName(String name) {
		return categoryRepository.findFirstByName(Objects.requireNonNull(name));
	}

}