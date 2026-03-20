package com.sequra.challenge.repository;

import com.sequra.challenge.model.Merchant;
import com.sequra.challenge.model.PaymentFrequency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {

    // Buscar merchants por referencia (para orders import)
    Optional<Merchant> findByReference(String reference);

    // Buscar merchants por frecuencia de pago (daily / weekly)
    List<Merchant> findByPaymentFrequency(PaymentFrequency paymentFrequency);
}