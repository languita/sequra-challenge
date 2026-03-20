package com.sequra.challenge.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class MerchantDTO {
    private UUID id;
    private String reference;
    private String email;
    private LocalDate liveOn;
    private String paymentFrequency;
    private BigDecimal minimumMonthlyFee;

    // Constructor
    public MerchantDTO(UUID id, String reference, String email, LocalDate liveOn, String paymentFrequency, BigDecimal minimumMonthlyFee) {
        this.id = id;
        this.reference = reference;
        this.email = email;
        this.liveOn = liveOn;
        this.paymentFrequency = paymentFrequency;
        this.minimumMonthlyFee = minimumMonthlyFee;
    }

    // Getters y setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getLiveOn() { return liveOn; }
    public void setLiveOn(LocalDate liveOn) { this.liveOn = liveOn; }

    public String getPaymentFrequency() { return paymentFrequency; }
    public void setPaymentFrequency(String paymentFrequency) { this.paymentFrequency = paymentFrequency; }

    public BigDecimal getMinimumMonthlyFee() { return minimumMonthlyFee; }
    public void setMinimumMonthlyFee(BigDecimal minimumMonthlyFee) { this.minimumMonthlyFee = minimumMonthlyFee; }
}