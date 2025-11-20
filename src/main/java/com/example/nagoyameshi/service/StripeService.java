package com.example.nagoyameshi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.nagoyameshi.entity.User;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionCollection;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.SubscriptionCancelParams;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.SubscriptionListParams;

import jakarta.annotation.PostConstruct;

@Service
public class StripeService {

	@Value("${stripe.api-key}")
	private String stripeApiKey;

	@PostConstruct
	private void init() {
		// Stripeのシークレットキーを設定する
		Stripe.apiKey = stripeApiKey;
	}

	public Customer createCustomer(User user) throws StripeException {
		CustomerCreateParams params = CustomerCreateParams.builder()
				.setName(user.getName())
				.setEmail(user.getEmail())
				.build();
		Customer customer = Customer.create(params);
		return customer;

	}

	public void attachPaymentMethodToCustomer(String paymentMethodId, String customerId) throws StripeException {
		//		支払方法を指定
		PaymentMethod resource = PaymentMethod.retrieve(paymentMethodId);
		//		顧客IDからパラメータを抽出し、紐づけ
		PaymentMethodAttachParams params = PaymentMethodAttachParams.builder().setCustomer(customerId).build();
		resource.attach(params);
	}

	public void setDefaultPaymentMethod(String paymentMethodId, String customerId) throws StripeException {
		CustomerUpdateParams params = CustomerUpdateParams.builder()
				.setInvoiceSettings(
						CustomerUpdateParams.InvoiceSettings.builder().setDefaultPaymentMethod(paymentMethodId).build())
				.build();

		//		顧客IDからCustomerオブジェクトを取得
		Customer customer = Customer.retrieve(customerId);
		customer.update(params);
	}

	public Subscription createSubscription(String priceId, String customerId) throws StripeException {
		SubscriptionCreateParams params = SubscriptionCreateParams.builder()
				.setCustomer(customerId)
				.addItem(
						SubscriptionCreateParams.Item.builder()
								.setPrice(priceId)
								.build())
				.build();
		Subscription subscription = Subscription.create(params);
		return subscription;

	}

	public PaymentMethod getDefaultPaymentMethodId(String customerId) throws StripeException {
		Customer customer = Customer.retrieve(customerId);
		//		customerオブジェクトから請求設定 (Invoice Settings) を取得する　→　method実行
		String defaultPaymentMethodId = customer.getInvoiceSettings().getDefaultPaymentMethod();
		return PaymentMethod.retrieve(defaultPaymentMethodId);

	}

	public void detachPaymentMethodFromCustomer(String paymentMethodId) throws StripeException {
		// ポイント2: 支払い方法のIDをもとに PaymentMethodオブジェクトを取得する
		PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);

		// ポイント3: PaymentMethodオブジェクトの紐づけを解除する
		// これにより、関連付けられていたCustomerからこの支払い方法が削除されます
		paymentMethod.detach();

	}

	public List<Subscription> getSubscriptions(String customerId) throws StripeException {
		SubscriptionListParams params = SubscriptionListParams.builder()
				.setCustomer(customerId) // ここで顧客IDをセット
				.build();
		SubscriptionCollection subscriptions = Subscription.list(params);
		return subscriptions.getData();

	}

	public void cancelSubscriptions(List<Subscription> subscriptions) throws StripeException {
		SubscriptionCancelParams params = SubscriptionCancelParams.builder().build();
		for (Subscription subscription : subscriptions) {
			subscription.cancel(params);
		}
	}

}
