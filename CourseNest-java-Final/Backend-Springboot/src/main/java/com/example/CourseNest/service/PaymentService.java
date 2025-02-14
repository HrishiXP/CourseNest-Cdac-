package com.example.CourseNest.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.CourseNest.exception.PaymentNotFoundException;
import com.example.CourseNest.model.Consultation;
import com.example.CourseNest.model.Course;
import com.example.CourseNest.model.Payment;
import com.example.CourseNest.model.PaymentResponse;
import com.example.CourseNest.model.Purchase;
import com.example.CourseNest.model.User;
import com.example.CourseNest.model.Workshop;
import com.example.CourseNest.repository.ConsultationRepository;
import com.example.CourseNest.repository.CourseRepository;
import com.example.CourseNest.repository.PaymentRepository;
import com.example.CourseNest.repository.PurchaseRepository;
import com.example.CourseNest.repository.UserRepository;
import com.example.CourseNest.repository.WorkshopRepository;
import com.paypal.orders.Order;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentService {

	@Autowired
	private PaymentRepository paymentRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CourseRepository courseRepository;

	@Autowired
	private PurchaseRepository purchaseRepository;

	@Autowired
	private ConsultationRepository consultationRepository;

	@Autowired
	private WorkshopRepository workshopRepository;

	@Autowired
	private PaypalService paypalService;

	public List<Payment> getAllPaymentsByUserId(Long userId) {
		return paymentRepository.findAll();
	}

	public Order getOrder(String orderId) throws Exception {
		return paypalService.getOrder(orderId);
	}

	public Payment getPaymentById(Long paymentId) {
		return paymentRepository.findById(paymentId)
				.orElseThrow(() -> new PaymentNotFoundException("Payment with ID " + paymentId + " not found"));
	}

	public ResponseEntity<PaymentResponse> createPayment(Long userId, Long courseId) throws IOException {
		User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
		Course course = courseRepository.findById(courseId)
				.orElseThrow(() -> new IllegalArgumentException("Course not found"));
		BigDecimal amount = course.getPrice();

		// Create PayPal payment with currency set to USD
		Order paypalOrder = paypalService.createPayment(amount, course.getDescription(),
				"http://localhost:8080/api/payments/cancel", "http://localhost:8080/api/payments/success");

		String approvalUrl = paypalService.getApprovalLink(paypalOrder);
		String orderId = paypalOrder.id(); // Retrieve the orderId from the PayPal order

		// Create and save the Payment entity
		Payment payment = new Payment();
		payment.setUser(user);
		payment.setCourse(course);
		payment.setAmount(amount);
		payment.setStatus("CREATED");
		payment.setOrderId(orderId); // Set the orderId in the Payment entity
		payment.setCreatedAt(LocalDateTime.now());
		payment.setUpdatedAt(LocalDateTime.now());

		Payment savedPayment = paymentRepository.save(payment);

		// Return payment with approval link
		PaymentResponse paymentResponse = new PaymentResponse(savedPayment, approvalUrl);
		return ResponseEntity.ok(paymentResponse);
	}

	public ResponseEntity<PaymentResponse> createPaymentForConsultation(Long userId, Long consultationId)
			throws IOException {
		User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
		Consultation consultation = consultationRepository.findById(consultationId)
				.orElseThrow(() -> new IllegalArgumentException("Consultation not found"));
		BigDecimal amount = BigDecimal.valueOf(consultation.getPrice());

		// Create PayPal payment
		Order paypalOrder = paypalService.createPayment(amount, consultation.getDescription(),
				"http://localhost:8080/api/payments/cancel", "http://localhost:8080/api/payments/success");

		String approvalUrl = paypalService.getApprovalLink(paypalOrder);
		String orderId = paypalOrder.id();

		// Create and save the Payment entity
		Payment payment = new Payment();
		payment.setUser(user);
		payment.setConsultation(consultation);
		payment.setAmount(amount);
		payment.setStatus("CREATED");
		payment.setOrderId(orderId);
		payment.setCreatedAt(LocalDateTime.now());
		payment.setUpdatedAt(LocalDateTime.now());

		Payment savedPayment = paymentRepository.save(payment);

		return ResponseEntity.ok(new PaymentResponse(savedPayment, approvalUrl));
	}

	public ResponseEntity<PaymentResponse> createPaymentForWorkshop(Long userId, Long workshopId) throws IOException {
		User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
		Workshop workshop = workshopRepository.findById(workshopId)
				.orElseThrow(() -> new IllegalArgumentException("Workshop not found"));
		BigDecimal amount = BigDecimal.valueOf(workshop.getPrice());

		// Create PayPal payment
		Order paypalOrder = paypalService.createPayment(amount, workshop.getDescription(),
				"http://localhost:8080/api/payments/cancel", "http://localhost:8080/api/payments/success");

		String approvalUrl = paypalService.getApprovalLink(paypalOrder);
		String orderId = paypalOrder.id();

		// Create and save the Payment entity
		Payment payment = new Payment();
		payment.setUser(user);
		payment.setWorkshop(workshop);
		payment.setAmount(amount);
		payment.setStatus("CREATED");
		payment.setOrderId(orderId);
		payment.setCreatedAt(LocalDateTime.now());
		payment.setUpdatedAt(LocalDateTime.now());

		Payment savedPayment = paymentRepository.save(payment);

		return ResponseEntity.ok(new PaymentResponse(savedPayment, approvalUrl));
	}

	public void captureOrder(Payment payment, String orderId) throws IOException {
		log.info("Capturing order with ID: {}", orderId);
		Order executedOrder = paypalService.capturePayment(orderId);

		// Update the payment status
		payment.setStatus(executedOrder.status());
		payment.setUpdatedAt(LocalDateTime.now()); // Update the updatedAt timestamp
		paymentRepository.save(payment);

		// Check if the payment was successful before creating the Purchase record
		if ("COMPLETED".equalsIgnoreCase(executedOrder.status())) {
			log.info("Payment completed, recording purchase...");
			// Create and save the Purchase record
			Purchase purchase = new Purchase();
			purchase.setUser(payment.getUser());

			// Assign the correct product to the purchase
			if (payment.getCourse() != null) {
				purchase.setCourse(payment.getCourse());
			} else if (payment.getConsultation() != null) {
				purchase.setConsultation(payment.getConsultation());
			} else if (payment.getWorkshop() != null) {
				purchase.setWorkshop(payment.getWorkshop());
			}

			purchase.setAmount(payment.getAmount());
			purchase.setPaymentStatus("COMPLETED");
			purchase.setCreatedAt(LocalDateTime.now());
			purchase.setUpdatedAt(LocalDateTime.now());

			purchaseRepository.save(purchase);
		} else {
			log.warn("Payment not completed for order ID: {}", orderId);
		}
	}

	public void refundPayment(Long paymentId, Long userId) {
		Payment payment = paymentRepository.findById(paymentId)
				.orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

		payment.setStatus("REFUNDED");
		payment.setUpdatedAt(LocalDateTime.now()); // Update the updatedAt timestamp
		payment.getUser().setCredits(payment.getUser().getCredits().add(payment.getAmount()));
		paymentRepository.save(payment);
	}

	public Long getProductId(Payment payment) {
		if (payment.getCourse() != null) {
			return payment.getCourse().getCourseId(); // Assuming Course has getId()
		} else if (payment.getConsultation() != null) {
			return payment.getConsultation().getConsultationId(); // Assuming Consultation has getId()
		} else if (payment.getWorkshop() != null) {
			return payment.getWorkshop().getWorkshopId(); // Assuming Workshop has getId()
		}
		return null;
	}

	// Method to derive productType
	public String getProductType(Payment payment) {
		if (payment.getCourse() != null) {
			return "course";
		} else if (payment.getConsultation() != null) {
			return "consultation";
		} else if (payment.getWorkshop() != null) {
			return "workshop";
		}
		return null;
	}
}
