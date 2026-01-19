package com.example.nagoyameshi.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Favorite;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.RestaurantRepository;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.FavoriteService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class FavoriteController {

	private final FavoriteService favoriteService;
	private final RestaurantRepository restaurantRepository;

	public FavoriteController(FavoriteService favoriteService, RestaurantRepository restaurantRepository) {
		this.favoriteService = favoriteService;
		this.restaurantRepository = restaurantRepository;
	}

	// --- メソッドマッピングと処理 ---

	/**
	 * GETメソッドの"/favorites"
	 * お気に入り一覧ページ（favorites/index.htmlファイル）を表示する。
	 */
	@GetMapping("/favorites")
	public String index(
			@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			@PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable,
			Model model, RedirectAttributes redirectAttributes) {

		User user = userDetailsImpl.getUser();
		if (user.getRole().getName().equals("ROLE_FREE_MEMBER")) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへの登録が必要です。");
			return "redirect:/subscription/register";
		}
		// FavoriteServiceのfindFavoritesByUserOrderByCreatedAtDesc()を利用
		Page<Favorite> favoritePage = favoriteService.findFavoritesByUserOrderByCreatedAtDesc(user, pageable);
		model.addAttribute("favoritePage", favoritePage);

		return "favorites/index";
	}

	/**
	 * POSTメソッドの"/restaurants/{restaurantId}/favorites/create"
	 * お気に入りをデータベースに登録する。
	 */
	@PostMapping("/restaurants/{restaurantId}/favorites/create")
	public String create(
			@PathVariable(name = "restaurantId") int restaurantId,
			@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			RedirectAttributes redirectAttributes) {

		User user = userDetailsImpl.getUser();
		if (user.getRole().getName().equals("ROLE_FREE_MEMBER")) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへの登録が必要です。");
			return "redirect:/subscription/register";
		}

		Optional<Restaurant> restaurantOptional = restaurantRepository.findById(restaurantId);
		if (restaurantOptional.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");
			return "redirect:/restaurants";
		}

		Restaurant restaurant = restaurantOptional.get();
		favoriteService.createFavorite(restaurant, user);
		redirectAttributes.addFlashAttribute("successMessage", "お気に入りに追加しました。");
		return "redirect:/restaurants/" + restaurantId;
	}

	/**
	 * POSTメソッドの"/favorites/{favoriteId}/delete"
	 * お気に入りをデータベースから削除する。
	 */
	@PostMapping("/favorites/{favoriteId}/delete")
	public String delete(
			@PathVariable(name = "favoriteId") int favoriteId,
			@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			RedirectAttributes redirectAttributes,
			HttpServletRequest httpServletRequest) {

		User currentUser = userDetailsImpl.getUser();
		if (currentUser.getRole().getName().equals("ROLE_FREE_MEMBER")) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへの登録が必要です。");
			return "redirect:/subscription/register";
		}

		// FavoriteServiceのfindFavoriteById()を利用
		Optional<Favorite> favoriteOptional = favoriteService.findFavoriteById(favoriteId);
		String referer = httpServletRequest.getHeader("Referer");
		if (favoriteOptional.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "お気に入りが存在しません。");
			return "redirect:" + (referer != null ? referer : "/favorites");
		}

		Favorite favorite = favoriteOptional.get();
		// ユーザー認証チェック
		if (!favorite.getUser().getId().equals(currentUser.getId())) {
			redirectAttributes.addFlashAttribute("errorMessage", "不正なアクセスです。");
			return "redirect:/favorites";
		}

		// FavoriteServiceのdeleteFavorite()を利用して削除
		favoriteService.deleteFavorite(favorite);
		redirectAttributes.addFlashAttribute("successMessage", "お気に入りを削除しました。");
		return "redirect:" + (referer != null ? referer : "/favorites");
	}
}