package com.sequra.challenge.repository;

import com.sequra.challenge.model.Merchant;
import com.sequra.challenge.model.PaymentFrequency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {

    // Find a merchant by reference (used during orders import)
    Optional<Merchant> findByReference(String reference);

    // Find merchants by payment frequency (daily / weekly)
    List<Merchant> findByPaymentFrequency(PaymentFrequency paymentFrequency);
}