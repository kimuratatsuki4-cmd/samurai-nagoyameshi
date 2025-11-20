package com.example.nagoyameshi.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.StripeService;
import com.example.nagoyameshi.service.UserService;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;
import com.stripe.param.SubscriptionCancelParams;

@Controller
@RequestMapping("/subscription")
public class SubscriptionController {

	private final StripeService stripeService;
	private final UserService userService;
	@Value("${stripe.premium-plan-price-id}")
	private String premiumPlanPriceId;

	// コンストラクタインジェクション
	public SubscriptionController(StripeService stripeService, UserService userService) {
		this.stripeService = stripeService;
		this.userService = userService;
	}

	// 1. 有料プラン登録ページを表示
	@GetMapping("/register")
	public String register() {
		// フロントエンドのStripe.jsで使用する公開鍵などをModelに渡す処理があればここに記述
		return "subscription/register";
	}

	// 2. 顧客作成・サブスクリプション作成・ロール更新
	@PostMapping("/create")
	public String create(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			@RequestParam("paymentMethodId") String paymentMethodId,
			RedirectAttributes redirectAttributes) {
		User user = userDetailsImpl.getUser();
		if (user.getStripeCustomerId() != null) {

			try {
				Customer customer = stripeService.createCustomer(user);
				// stripeCustomerIdフィールドに顧客IDを保存する
				userService.saveStripeCustomerId(user, customer.getId());
			} catch (StripeException e) {
				// TODO: handle exception
				redirectAttributes.addFlashAttribute("errorMessage", "有料プランへの登録に失敗しました。再度お試しください。");
			}
		}
		// Stripe上で顧客を作成し、支払い方法を紐付け、サブスクリプションを開始する
		String stripeCustomerId = user.getStripeCustomerId();
		try {
			stripeService.attachPaymentMethodToCustomer(paymentMethodId, stripeCustomerId);
			stripeService.setDefaultPaymentMethod(paymentMethodId, stripeCustomerId);
			stripeService.createSubscription(premiumPlanPriceId, stripeCustomerId);
		} catch (StripeException e) {
			// TODO 自動生成された catch ブロック
			redirectAttributes.addFlashAttribute("errorMessage", "有料プランへの登録に失敗しました。再度お試しください。");
		}

		// ユーザーのロールを更新する (例: ROLE_FREE -> ROLE_PAID)
		userService.updateRole(user, "ROLE_PAID_MEMBER");
		userService.refreshAuthenticationByRole("ROLE_PAID_MEMBER");

		// セッション情報の更新が必要な場合はここで行う（再ログインなしで権限反映など）
		redirectAttributes.addFlashAttribute("successMessage", "有料プランへの登録が完了しました。");
		return "redirect:/"; // マイページやトップページへリダイレクト
	}

	// 3. お支払い方法編集ページを表示
	@GetMapping("/edit")
	public String edit(Model model, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			RedirectAttributes redirectAttributes) {
		User user = userDetailsImpl.getUser();
		try {
			PaymentMethod paymentMethod = stripeService.getDefaultPaymentMethodId(user.getStripeCustomerId());
			model.addAttribute("card", paymentMethod.getCard());
			model.addAttribute("cardHolderName", paymentMethod.getBillingDetails().getName());
		} catch (StripeException e) {
			// TODO: handle exception
			redirectAttributes.addFlashAttribute("errorMessage", "お支払い方法を取得できませんでした。再度お試しください。");
		}

		return "subscription/edit";
	}

	// 4. デフォルトの支払い方法を更新
	@PostMapping("/update")
	public String update(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			@RequestParam("paymentMethodId") String paymentMethodId,
			RedirectAttributes redirectAttributes,
			Model model) {
		User user = userDetailsImpl.getUser();
		try {
			PaymentMethod defaultPaymentMethodId = stripeService.getDefaultPaymentMethodId(user.getStripeCustomerId());
			stripeService.attachPaymentMethodToCustomer(paymentMethodId, user.getStripeCustomerId());
			stripeService.setDefaultPaymentMethod(paymentMethodId, user.getStripeCustomerId());

			stripeService.detachPaymentMethodFromCustomer(defaultPaymentMethodId.getId());

			redirectAttributes.addFlashAttribute("successMessage", "お支払い方法を変更しました。");
			return "redirect:/subscription/edit";

		} catch (StripeException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "お支払い方法の変更に失敗しました。再度お試しください。");
			return "redirect:/subscription/edit";
		}
	}

	// 5. 有料プラン解約ページを表示
	@GetMapping("/cancel")
	public String cancel() {
		return "subscription/cancel";
	}

	// 6. サブスクリプション解約・支払い方法解除・ロール更新
	@PostMapping("/delete")
	public String delete(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			RedirectAttributes redirectAttributes) {
		User user = userDetailsImpl.getUser();
		try {
			Subscription subscription = Subscription.retrieve(user.getStripeCustomerId());
			SubscriptionCancelParams params = SubscriptionCancelParams.builder().build();
			subscription.cancel(params);

			PaymentMethod paymentMethod = stripeService.getDefaultPaymentMethodId(user.getStripeCustomerId());
			stripeService.detachPaymentMethodFromCustomer(paymentMethod.getId());

		} catch (StripeException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "有料プランの解約に失敗しました。再度お試しください。");
			return "redirect:/subscription/cancel";
		}
		userService.updateRole(user, "ROLE_FREE_MEMBER");
		userService.refreshAuthenticationByRole("ROLE_FREE_MEMBER");

		redirectAttributes.addFlashAttribute("successMessage", "有料プランを解約しました。");
		return "redirect:/";
	}
}