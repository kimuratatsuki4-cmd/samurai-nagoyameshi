package com.example.nagoyameshi.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReservationRegisterForm;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.ReservationService;
import com.example.nagoyameshi.service.RestaurantService;

@Controller
public class ReservationController {

	private final ReservationService reservationService;
	private final RestaurantService restaurantService;

	public ReservationController(ReservationService reservationService, RestaurantService restaurantService) {
		this.reservationService = reservationService;
		this.restaurantService = restaurantService;
	}

	// ------------------------------------------------
	// 1. 予約一覧ページ表示
	// ------------------------------------------------
	@GetMapping("/reservations")
	public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			@PageableDefault(page = 0, size = 15, sort = "id", direction = Direction.ASC) Pageable pageable,
			Model model,
			RedirectAttributes redirectAttributes) {

		// 🚨 認証チェック: 未ログインまたはユーザー情報がない場合はログインページへリダイレクト
		if (userDetailsImpl == null || userDetailsImpl.getUser() == null) {
			return "redirect:/login";
		}

		User user = userDetailsImpl.getUser();

		// 🚨 無料会員チェック(無料会員であれば、有料プラン登録ページにリダイレクト)
		if (user.getRole().getName().equals("ROLE_FREE_MEMBER")) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへの登録が必要です。");
			return "redirect:/subscription/register";
		}

		// ユーザーに紐づく予約を予約日時順で取得
		Page<Reservation> reservationPage = reservationService.findReservationsByUserOrderByReservedDatetimeDesc(user,
				pageable);

		model.addAttribute("reservationPage", reservationPage);
		model.addAttribute("currentDateTime", LocalDateTime.now());
		return "reservations/index";
	}

	// ------------------------------------------------
	// 2. 予約ページ表示
	// ------------------------------------------------
	@GetMapping("/restaurants/{restaurantId}/reservations/register")
	public String register(@PathVariable("restaurantId") Integer restaurantId,
			Model model,
			RedirectAttributes redirectAttributes,
			@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {

		// 🚨 認証チェック
		if (userDetailsImpl == null || userDetailsImpl.getUser() == null) {
			return "redirect:/login";
		}

		User user = userDetailsImpl.getUser();

		// 🚨 無料会員チェック（無料会員であれば、有料プラン登録ページにリダイレクト）
		if (user.getRole().getName().equals("ROLE_FREE_MEMBER")) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへの登録が必要です。");
			return "redirect:/subscription/register";
		}

		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(restaurantId);
		//店舗が存在しないため、会員用の店舗一覧ページにリダイレクト
		if (optionalRestaurant.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");
			return "redirect:/restaurants";
		}

		Restaurant restaurant = optionalRestaurant.get();
		ReservationRegisterForm reservationRegisterForm = new ReservationRegisterForm();
		List<Integer> restaurantRegularHolidays = restaurantService.findDayIndexesByRestaurantId(restaurantId);

		model.addAttribute("restaurant", restaurant);
		model.addAttribute("restaurantRegularHolidays", restaurantRegularHolidays);
		model.addAttribute("reservationRegisterForm", reservationRegisterForm);

		return "reservations/register";
	}

	// ------------------------------------------------
	// 3. 予約登録処理
	// ------------------------------------------------
	@PostMapping("/restaurants/{restaurantId}/reservations/create")
	public String create(@PathVariable("restaurantId") Integer restaurantId,
			@ModelAttribute @Validated ReservationRegisterForm reservationRegisterForm,
			BindingResult bindingResult,
			RedirectAttributes redirectAttributes,
			@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			Model model) {

		// 🚨 認証チェック
		if (userDetailsImpl == null || userDetailsImpl.getUser() == null) {
			return "redirect:/login";
		}
		User user = userDetailsImpl.getUser();

		// 🚨 無料会員チェック(有料プラン登録ページへリダイレクト)
		if (user.getRole().getName().equals("ROLE_FREE_MEMBER")) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへの登録が必要です。");
			return "redirect:/subscription/register";
		}

		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(restaurantId);
		if (optionalRestaurant.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");
			return "redirect:/restaurants";
		}
		Restaurant restaurant = optionalRestaurant.get();

		// 🚨 ビジネスロジックチェック (予約時刻が2時間後以降であるか)
		if (!reservationService.isAtLeastTwoHoursInFuture(LocalDateTime.of(
				reservationRegisterForm.getReservationDate(),
				reservationRegisterForm.getReservationTime()))) {
			// エラーを手動で追加
			FieldError fieldError = new FieldError(bindingResult.getObjectName(), "reservationTime",
					"当日の予約は2時間前までにお願いいたします。");
			bindingResult.addError(fieldError);
		}

		// 🚨 人数が店舗の収容人数を超えていないかチェック
		if (reservationRegisterForm.getNumberOfPeople() > restaurant.getSeatingCapacity()) {
			FieldError fieldError = new FieldError(bindingResult.getObjectName(), "reservationTime",
					"予約人数が収容可能人数を超過しています。");
			bindingResult.addError(fieldError);
		}

		// 🚨 バリデーションエラーチェック
		if (bindingResult.hasErrors()) {
			return "reservations/register";
		}

		// 予約情報を登録
		reservationService.createReservation(restaurant, user, reservationRegisterForm);
		redirectAttributes.addFlashAttribute("successMessage", "予約が完了しました。");
		return "redirect:/reservations";
	}

	// ------------------------------------------------
	// 4. 予約削除処理
	// ------------------------------------------------
	@PostMapping("/reservations/{reservationId}/delete")
	public String delete(@PathVariable("reservationId") Integer reservationId,
			RedirectAttributes redirectAttributes,
			@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {

		// 🚨 認証チェック
		if (userDetailsImpl == null || userDetailsImpl.getUser() == null) {
			return "redirect:/login";
		}
		User user = userDetailsImpl.getUser();

		// 🚨 無料会員チェック
		if (user.getRole().getName().equals("ROLE_FREE_MEMBER")) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへの登録が必要です。");
			return "redirect:/subscription/register";
		}
		
		//予約有無チェック
		Optional<Reservation> optionalReservation = reservationService.findReservationById(reservationId);
		if (optionalReservation.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "予約が見つかりませんでした。");
			return "redirect:/reservations";
		}
		Reservation reservation = optionalReservation.get();

		// 🚨 予約の所有者チェック
		if (!reservation.getUser().getId().equals(user.getId())) {
			redirectAttributes.addFlashAttribute("errorMessage", "不正なアクセスです。");
			return "redirect:/reservations";
		}

		// 🚨 削除の可否チェック (例: 予約時刻の2時間前を過ぎていないかなど、必要に応じて)
		LocalDateTime reservedDateTime = reservation.getReservedDatetime();
		if (reservedDateTime.isBefore(LocalDateTime.now().plusHours(2))) {
			redirectAttributes.addFlashAttribute("errorMessage", "予約時刻の2時間前を過ぎたため、削除できません。");
			return "redirect:/reservations";
		}

		reservationService.deleteReservation(reservation);
		redirectAttributes.addFlashAttribute("successMessage", "予約をキャンセルしました。");
		return "redirect:/reservations";
	}
}