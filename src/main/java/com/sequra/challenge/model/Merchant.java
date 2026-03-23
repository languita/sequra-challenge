package com.sequra.challenge.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

@Entity
public class Merchant {

    @Id
    private UUID id;
    private String reference;
    private String email;
    private LocalDate liveOn; // only relevant for weekly merchants (start date)
    
    @Enumerated(EnumType.STRING)
    private PaymentFrequency paymentFrequency; // DAILY / WEEKLY
    
    private BigDecimal minimumMonthlyFee = BigDecimal.ZERO; // from CSV

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public LocalDate getLiveOn() {
		return liveOn;
	}

	public void setLiveOn(LocalDate liveOn) {
		this.liveOn = liveOn;
	}

	public PaymentFrequency getPaymentFrequency() {
		return paymentFrequency;
	}

	public void setPaymentFrequency(PaymentFrequency paymentFrequency) {
		this.paymentFrequency = paymentFrequency;
	}

	public BigDecimal getMinimumMonthlyFee() {
		return minimumMonthlyFee;
	}

	public void setMinimumMonthlyFee(BigDecimal minimumMonthlyFee) {
		this.minimumMonthlyFee = minimumMonthlyFee;
	}
    
    
}