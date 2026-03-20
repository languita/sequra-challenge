package com.sequra.challenge.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class OrderDTO {
    private String id;
    private String merchantReference;
    private BigDecimal amount;
    private LocalDate createdAt;

    // Constructor
    public OrderDTO(String id, String merchantReference, BigDecimal amount, LocalDate createdAt) {
        this.id = id;
        this.merchantReference = merchantReference;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMerchantReference() { return merchantReference; }
    public void setMerchantReference(String merchantReference) { this.merchantReference = merchantReference; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
}