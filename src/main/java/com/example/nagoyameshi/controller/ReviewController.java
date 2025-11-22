package com.example.nagoyameshi.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReviewEditForm;
import com.example.nagoyameshi.form.ReviewRegisterForm;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.RestaurantService;
import com.example.nagoyameshi.service.ReviewService;

@Controller
@RequestMapping("/restaurants/{restaurantId}/reviews")
public class ReviewController {
   private final ReviewService reviewService;
   private final RestaurantService restaurantService;

   public ReviewController(ReviewService reviewService, RestaurantService restaurantService) {
       this.reviewService = reviewService;
       this.restaurantService = restaurantService;
   }

   @GetMapping
   public String index(@PathVariable(name = "restaurantId") Integer restaurantId,
                       @PageableDefault(page = 0, size = 5, sort = "id", direction = Direction.ASC) Pageable pageable,
                       @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                       RedirectAttributes redirectAttributes,
                       Model model)
   {
	// 🚩 修正: userDetailsImpl または user が null ならログインページへリダイレクト
	    if (userDetailsImpl == null || userDetailsImpl.getUser() == null) {
	        return "redirect:/login"; 
	    }
	   
       Optional<Restaurant> optionalRestaurant  = restaurantService.findRestaurantById(restaurantId);

       if (optionalRestaurant.isEmpty()) {
           redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");

           return "redirect:/restaurants";
       }

       Restaurant restaurant = optionalRestaurant.get();
       User user = userDetailsImpl.getUser();
       String userRoleName = user.getRole().getName();
       Page<Review> reviewPage;

       if (userRoleName.equals("ROLE_PAID_MEMBER")) {
           reviewPage = reviewService.findReviewsByRestaurantOrderByCreatedAtDesc(restaurant, pageable);
       } else {
           reviewPage = reviewService.findReviewsByRestaurantOrderByCreatedAtDesc(restaurant, PageRequest.of(0, 3));
       }

       boolean hasUserAlreadyReviewed = reviewService.hasUserAlreadyReviewed(restaurant, user);

       model.addAttribute("restaurant", restaurant);
       model.addAttribute("userRoleName", userRoleName);
       model.addAttribute("reviewPage", reviewPage);
       model.addAttribute("hasUserAlreadyReviewed", hasUserAlreadyReviewed);

       return "reviews/index";
   }

   @GetMapping("/register")
   public String register(@PathVariable(name = "restaurantId") Integer restaurantId,
                          @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                          RedirectAttributes redirectAttributes,
                          Model model)
   {
	   
	// 🚩 修正: userDetailsImpl または user が null ならログインページへリダイレクト
	    if (userDetailsImpl == null || userDetailsImpl.getUser() == null) {
	        return "redirect:/login"; 
	    }
       User user = userDetailsImpl.getUser();

       if (user.getRole().getName().equals("ROLE_FREE_MEMBER")) {
           redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへの登録が必要です。");

           return "redirect:/subscription/register";
       }

       Optional<Restaurant> optionalRestaurant  = restaurantService.findRestaurantById(restaurantId);

       if (optionalRestaurant.isEmpty()) {
           redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");

           return "redirect:/restaurants";
       }

       Restaurant restaurant = optionalRestaurant.get();
       ReviewRegisterForm reviewRegisterForm = new ReviewRegisterForm();
       reviewRegisterForm.setScore(5);

       model.addAttribute("restaurant", restaurant);
       model.addAttribute("reviewRegisterForm", reviewRegisterForm);

       return "reviews/register";
   }

   @PostMapping("/create")
   public String create(@PathVariable(name = "restaurantId") Integer restaurantId,
                        @ModelAttribute @Validated ReviewRegisterForm reviewRegisterForm,
                        BindingResult bindingResult,
                        @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                        RedirectAttributes redirectAttributes,
                        Model model)
   {
	// 🚩 修正: userDetailsImpl または user が null ならログインページへリダイレクト
	    if (userDetailsImpl == null || userDetailsImpl.getUser() == null) {
	        return "redirect:/login"; 
	    }
       User user = userDetailsImpl.getUser();

       if (user.getRole().getName().equals("ROLE_FREE_MEMBER")) {
           redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへの登録が必要です。");

           return "redirect:/subscription/register";
       }

       Optional<Restaurant> optionalRestaurant  = restaurantService.findRestaurantById(restaurantId);

       if (optionalRestaurant.isEmpty()) {
           redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");

           return "redirect:/restaurants";
       }

       Restaurant restaurant = optionalRestaurant.get();

       if (bindingResult.hasErrors()) {
           model.addAttribute("restaurant", restaurant);
           model.addAttribute("reviewRegisterForm", reviewRegisterForm);

           return "reviews/register";
       }

       reviewService.createReview(reviewRegisterForm, restaurant, user);
       redirectAttributes.addFlashAttribute("successMessage", "レビューを投稿しました。");

       return "redirect:/restaurants/{restaurantId}";
   }

   @GetMapping("/{reviewId}/edit")
   public String edit(@PathVariable(name = "restaurantId") Integer restaurantId,
                      @PathVariable(name = "reviewId") Integer reviewId,
                      @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                      RedirectAttributes redirectAttributes,
                      Model model)
   {
	// 🚩 修正: userDetailsImpl または user が null ならログインページへリダイレクト
	    if (userDetailsImpl == null || userDetailsImpl.getUser() == null) {
	        return "redirect:/login"; 
	    }
	    
       User user = userDetailsImpl.getUser();

       if (user.getRole().getName().equals("ROLE_FREE_MEMBER")) {
           redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへの登録が必要です。");

           return "redirect:/subscription/register";
       }

       Optional<Restaurant> optionalRestaurant  = restaurantService.findRestaurantById(restaurantId);
       Optional<Review> optionalReview  = reviewService.findReviewById(reviewId);

       if (optionalRestaurant.isEmpty() || optionalReview.isEmpty()) {
           redirectAttributes.addFlashAttribute("errorMessage", "指定されたページが見つかりません。");

           return "redirect:/restaurants";
       }

       Review review = optionalReview.get();

       if (!review.getRestaurant().getId().equals(restaurantId) || !review.getUser().getId().equals(user.getId())) {
           redirectAttributes.addFlashAttribute("errorMessage", "不正なアクセスです。");

           return "redirect:/restaurants/{restaurantId}";
       }

       Restaurant restaurant = optionalRestaurant.get();
       ReviewEditForm reviewEditForm = new ReviewEditForm(review.getScore(), review.getContent());

       model.addAttribute("restaurant", restaurant);
       model.addAttribute("review", review);
       model.addAttribute("reviewEditForm", reviewEditForm);

       return "reviews/edit";
   }

   @PostMapping("/{reviewId}/update")
   public String update(@PathVariable(name = "restaurantId") Integer restaurantId,
                        @PathVariable(name = "reviewId") Integer reviewId,
                        @ModelAttribute @Validated ReviewEditForm reviewEditForm,
                        BindingResult bindingResult,
                        @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                        RedirectAttributes redirectAttributes,
                        Model model)
   {
	// 🚩 修正: userDetailsImpl または user が null ならログインページへリダイレクト
	    if (userDetailsImpl == null || userDetailsImpl.getUser() == null) {
	        return "redirect:/login"; 
	    }
	   
       User user = userDetailsImpl.getUser();

       if (user.getRole().getName().equals("ROLE_FREE_MEMBER")) {
           redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへの登録が必要です。");

           return "redirect:/subscription/register";
       }

       Optional<Restaurant> optionalRestaurant  = restaurantService.findRestaurantById(restaurantId);
       Optional<Review> optionalReview  = reviewService.findReviewById(reviewId);

       if (optionalRestaurant.isEmpty() || optionalReview.isEmpty()) {
           redirectAttributes.addFlashAttribute("errorMessage", "指定されたページが見つかりません。");

           return "redirect:/restaurants";
       }

       Review review = optionalReview.get();

       if (!review.getRestaurant().getId().equals(restaurantId) || !review.getUser().getId().equals(user.getId())) {
           redirectAttributes.addFlashAttribute("errorMessage", "不正なアクセスです。");

           return "redirect:/restaurants/{restaurantId}";
       }

       Restaurant restaurant = optionalRestaurant.get();

       if (bindingResult.hasErrors()) {
           model.addAttribute("restaurant", restaurant);
           model.addAttribute("review", review);
           model.addAttribute("reviewEditForm", reviewEditForm);

           return "reviews/edit";
       }

       reviewService.updateReview(reviewEditForm, review);
       redirectAttributes.addFlashAttribute("successMessage", "レビューを編集しました。");

       return "redirect:/restaurants/{restaurantId}";
   }

   @PostMapping("/{reviewId}/delete")
   public String delete(@PathVariable(name = "restaurantId") Integer restaurantId,
                        @PathVariable(name = "reviewId") Integer reviewId,
                        @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                        RedirectAttributes redirectAttributes)
   {
	// 🚩 修正: userDetailsImpl または user が null ならログインページへリダイレクト
	    if (userDetailsImpl == null || userDetailsImpl.getUser() == null) {
	        return "redirect:/login"; 
	    }
       User user = userDetailsImpl.getUser();

       if (user.getRole().getName().equals("ROLE_FREE_MEMBER")) {
           redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへの登録が必要です。");

           return "redirect:/subscription/register";
       }

       Optional<Restaurant> optionalRestaurant  = restaurantService.findRestaurantById(restaurantId);
       Optional<Review> optionalReview  = reviewService.findReviewById(reviewId);

       if (optionalRestaurant.isEmpty() || optionalReview.isEmpty()) {
           redirectAttributes.addFlashAttribute("errorMessage", "指定されたページが見つかりません。");

           return "redirect:/restaurants";
       }

       Review review = optionalReview.get();

       if (!review.getRestaurant().getId().equals(restaurantId) || !review.getUser().getId().equals(user.getId())) {
           redirectAttributes.addFlashAttribute("errorMessage", "不正なアクセスです。");

           return "redirect:/restaurants/{restaurantId}";
       }

       reviewService.deleteReview(review);
       redirectAttributes.addFlashAttribute("successMessage", "レビューを削除しました。");

       return "redirect:/restaurants/{restaurantId}";
   }
}