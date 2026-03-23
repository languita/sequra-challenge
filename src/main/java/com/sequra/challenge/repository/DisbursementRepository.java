package com.sequra.challenge.repository;

import com.sequra.challenge.model.Disbursement;
import com.sequra.challenge.model.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DisbursementRepository extends JpaRepository<Disbursement, Long> {

    List<Disbursement> findByMerchant(Merchant merchant);

    // Find disbursements for a merchant between two dates (daily, weekly, or monthly top-up)
    List<Disbursement> findByMerchantAndDateBetween(Merchant merchant, LocalDate start, LocalDate end);
}