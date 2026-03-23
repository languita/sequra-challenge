
package com.sequra.challenge.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "merchant_id")
    private Merchant merchant;

	@ManyToOne
	@JoinColumn(name = "disbursement_id")
	private Disbursement disbursement;

    private BigDecimal amount;
    private LocalDate createdAt;
    private Boolean disbursed = false;
    
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Merchant getMerchant() {
		return merchant;
	}
	public void setMerchant(Merchant merchant) {
		this.merchant = merchant;
	}

	public Disbursement getDisbursement() {
		return disbursement;
	}

	public void setDisbursement(Disbursement disbursement) {
		this.disbursement = disbursement;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public LocalDate getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDate createdAt) {
		this.createdAt = createdAt;
	}
	public Boolean getDisbursed() {
		return disbursed;
	}
	public void setDisbursed(Boolean disbursed) {
		this.disbursed = disbursed;
	}
    
    
}