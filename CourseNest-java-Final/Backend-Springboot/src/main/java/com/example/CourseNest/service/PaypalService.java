package com.example.CourseNest.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.AmountWithBreakdown;
import com.paypal.orders.ApplicationContext;
import com.paypal.orders.LinkDescription;
import com.paypal.orders.Order;
import com.paypal.orders.OrderRequest;
import com.paypal.orders.OrdersCaptureRequest;
import com.paypal.orders.OrdersCreateRequest;
import com.paypal.orders.OrdersGetRequest;
import com.paypal.orders.PurchaseUnitRequest;

@Service
public class PaypalService {

	@Autowired
	private PayPalHttpClient payPalClient;

	// Method to create a payment with PayPal
	public Order createPayment(BigDecimal total, String description, String cancelUrl, String successUrl)
			throws IOException {
		OrderRequest orderRequest = new OrderRequest();
		orderRequest.checkoutPaymentIntent("CAPTURE");

		ApplicationContext applicationContext = new ApplicationContext().brandName("CourseNest").landingPage("BILLING")
				.cancelUrl(cancelUrl).returnUrl(successUrl).userAction("CONTINUE");

		orderRequest.applicationContext(applicationContext);

		// Ensure currency is set to USD
		AmountWithBreakdown amount = new AmountWithBreakdown().currencyCode("USD").value(total.toString());

		List<PurchaseUnitRequest> purchaseUnits = new ArrayList<>();
		purchaseUnits.add(new PurchaseUnitRequest().amountWithBreakdown(amount).description(description));

		orderRequest.purchaseUnits(purchaseUnits);

		OrdersCreateRequest request = new OrdersCreateRequest().requestBody(orderRequest);

		HttpResponse<Order> response = payPalClient.execute(request);
		return response.result();
	}

	// Method to get the approval link from PayPal
	public String getApprovalLink(Order order) {
		for (LinkDescription link : order.links()) {
			if (link.rel().equals("approve")) {
				return link.href();
			}
		}
		return null;
	}

	// Method to capture payment after PayPal approval
	public Order capturePayment(String orderId) throws IOException {
		OrdersCaptureRequest request = new OrdersCaptureRequest(orderId);
		request.requestBody(new OrderRequest());

		HttpResponse<Order> response = payPalClient.execute(request);
		return response.result();
	}

	// Method to get the order details from PayPal
	public Order getOrder(String orderId) throws IOException {
		OrdersGetRequest request = new OrdersGetRequest(orderId);
		HttpResponse<Order> response = payPalClient.execute(request);
		return response.result();
	}
}
