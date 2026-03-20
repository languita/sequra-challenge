package com.sequra.challenge.repository;

import com.sequra.challenge.model.Disbursement;
import com.sequra.challenge.model.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DisbursementRepository extends JpaRepository<Disbursement, Long> {

    // Buscar disbursements de un merchant entre fechas (daily, weekly o monthly top-up)
    List<Disbursement> findByMerchantAndDateBetween(Merchant merchant, LocalDate start, LocalDate end);
}