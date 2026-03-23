package com.sequra.challenge.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sequra.challenge.model.Merchant;
import com.sequra.challenge.model.Order;

public interface OrderRepository extends JpaRepository<Order, String> {

    // Completed orders not yet disbursed
	List<Order> findByMerchantAndDisbursedFalseAndCreatedAtBetween(
	        Merchant merchant,
	        LocalDate start,
	        LocalDate end
	);

	List<Order> findByMerchantAndDisbursedFalseOrderByCreatedAtAsc(Merchant merchant);
}