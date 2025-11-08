package com.TenX.Automobile.service;

import com.TenX.Automobile.model.entity.Payment;
import com.TenX.Automobile.model.entity.Job;
import com.TenX.Automobile.repository.PaymentRepository;
import com.TenX.Automobile.repository.JobRepository;
import com.TenX.Automobile.exception.ResourceNotFoundException;
import com.TenX.Automobile.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final JobRepository jobRepository;

    /**
     * Create a payment for a job
     */
    public Payment createPayment(Payment payment, Long jobId) {
        log.info("Creating payment for job ID: {}", jobId);

        // Validate payment data
        validatePaymentData(payment);

        // Check if payment already exists for this job (one-to-one relationship)
        if (paymentRepository.findByJob_JobId(jobId).isPresent()) {
            throw new DuplicateResourceException("Payment already exists for job ID: " + jobId);
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        payment.setJob(job);
        Payment savedPayment = paymentRepository.save(payment);

        log.info("Payment created successfully with ID: {} for job: {}", savedPayment.getP_Id(), jobId);
        return savedPayment;
    }

    /**
     * Get payment by ID
     */
    @Transactional(readOnly = true)
    public Payment getPaymentById(Long paymentId) {
        log.info("Fetching payment with ID: {}", paymentId);
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
    }

    /**
     * Get payment by job ID
     */
    @Transactional(readOnly = true)
    public Payment getPaymentByJobId(Long jobId) {
        log.info("Fetching payment for job ID: {}", jobId);
        return paymentRepository.findByJob_JobId(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for job id: " + jobId));
    }

    /**
     * Get all payments
     */
    @Transactional(readOnly = true)
    public List<Payment> getAllPayments() {
        log.info("Fetching all payments");
        return paymentRepository.findAll();
    }

    /**
     * Update payment
     */
    public Payment updatePayment(Long paymentId, Payment paymentDetails) {
        log.info("Updating payment with ID: {}", paymentId);

        Payment existingPayment = getPaymentById(paymentId);

        // Validate new payment data
        validatePaymentData(paymentDetails);

        existingPayment.setP_Amount(paymentDetails.getP_Amount());
        existingPayment.setP_Type(paymentDetails.getP_Type());

        Payment updatedPayment = paymentRepository.save(existingPayment);
        log.info("Payment updated successfully with ID: {}", paymentId);

        return updatedPayment;
    }

    /**
     * Delete payment
     */
    public void deletePayment(Long paymentId) {
        log.info("Deleting payment with ID: {}", paymentId);
        Payment payment = getPaymentById(paymentId);
        paymentRepository.delete(payment);
        log.info("Payment deleted successfully with ID: {}", paymentId);
    }

    /**
     * Check if payment exists for a job
     */
    @Transactional(readOnly = true)
    public boolean existsByJobId(Long jobId) {
        return paymentRepository.findByJob_JobId(jobId).isPresent();
    }

    /**
     * Validate payment data
     */
    private void validatePaymentData(Payment payment) {
        if (payment.getP_Amount() <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than 0");
        }

        if (payment.getP_Type() == null || payment.getP_Type().trim().isEmpty()) {
            throw new IllegalArgumentException("Payment type cannot be empty");
        }
    }
}
