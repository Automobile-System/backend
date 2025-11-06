package com.TenX.Automobile.controller;

import com.TenX.Automobile.entity.Payment;
import com.TenX.Automobile.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Payment>> getAllPayments() {
        log.info("Fetching all payments");
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long paymentId) {
        log.info("Fetching payment with ID: {}", paymentId);
        return ResponseEntity.ok(paymentService.getPaymentById(paymentId));
    }

    @GetMapping("/job/{jobId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Payment> getPaymentByJobId(@PathVariable Long jobId) {
        log.info("Fetching payment for job ID: {}", jobId);
        return ResponseEntity.ok(paymentService.getPaymentByJobId(jobId));
    }

    @PostMapping("/job/{jobId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CUSTOMER')")
    public ResponseEntity<Payment> createPayment(@PathVariable Long jobId, @RequestBody Payment payment) {
        log.info("Creating payment for job ID: {}", jobId);
        return ResponseEntity.ok(paymentService.createPayment(payment, jobId));
    }

    @PutMapping("/{paymentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Payment> updatePayment(@PathVariable Long paymentId, @RequestBody Payment paymentDetails) {
        log.info("Updating payment with ID: {}", paymentId);
        return ResponseEntity.ok(paymentService.updatePayment(paymentId, paymentDetails));
    }

    @DeleteMapping("/{paymentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deletePayment(@PathVariable Long paymentId) {
        log.info("Deleting payment with ID: {}", paymentId);
        paymentService.deletePayment(paymentId);
        return ResponseEntity.noContent().build();
    }
}
