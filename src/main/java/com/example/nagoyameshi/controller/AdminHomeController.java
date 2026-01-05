package com.example.nagoyameshi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.nagoyameshi.service.ReservationService;
import com.example.nagoyameshi.service.RestaurantService;
import com.example.nagoyameshi.service.UserService;

@Controller
public class AdminHomeController {
	private final UserService userService;
	private final RestaurantService restaurantService;
	private final ReservationService reservationService;

	public AdminHomeController(UserService userService, RestaurantService restaurantService,
			ReservationService reservationService) {
		super();
		this.userService = userService;
		this.restaurantService = restaurantService;
		this.reservationService = reservationService;
	}

	@GetMapping("/admin")
	public String index(Model model) {
		Integer totalFreeMembers = userService.countUsersByRole_Name("ROLE_FREE_MEMBER");
		Integer totalPaidMembers = userService.countUsersByRole_Name("ROLE_PAID_MEMBER");
		Integer totalMembers = totalFreeMembers + totalPaidMembers;

		// 2. 店舗数の集計
		Long totalRestaurants = restaurantService.countRestaurants(); // サービス層で全件数を取得
		// 3. 予約数の集計
		Long totalReservations = reservationService.countReservations(); // サービス層で総予約数を取得
		// 4. 月間売上の計算
		// 売上 = 300 * 有料会員数 (totalPaidMembers)
		Integer salesForThisMonth = 300 * totalPaidMembers;

		// Modelインターフェースでビュー側へ変数を渡す
		model.addAttribute("totalFreeMembers", totalFreeMembers);
		model.addAttribute("totalPaidMembers", totalPaidMembers);
		model.addAttribute("totalMembers", totalMembers);
		model.addAttribute("totalRestaurants", totalRestaurants);
		model.addAttribute("totalReservations", totalReservations);
		model.addAttribute("salesForThisMonth", salesForThisMonth);

		return "admin/index";
	}

}
