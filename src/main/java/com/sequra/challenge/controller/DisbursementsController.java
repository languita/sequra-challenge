package com.sequra.challenge.controller;

import com.sequra.challenge.model.Disbursement;
import com.sequra.challenge.model.Merchant;
import com.sequra.challenge.model.Order;
import com.sequra.challenge.repository.DisbursementRepository;
import com.sequra.challenge.repository.MerchantRepository;
import com.sequra.challenge.repository.OrderRepository;
import com.sequra.challenge.service.DisbursementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/disbursements")
public class DisbursementsController {

    @Autowired
    private DisbursementRepository disbursementRepository;

    @Autowired
    private DisbursementService disbursementService;


    // GET all disbursements
    @GetMapping
    public List<Disbursement> getAllDisbursements() {
        return disbursementRepository.findAll();
    }

    // POST to manually process disbursements
    @PostMapping("/process")
    public String processDisbursements() {
        disbursementService.processDailyDisbursements();
        disbursementService.processWeeklyDisbursements();
        return "Disbursements processed successfully";
    }

    // POST to backfill historical disbursements for all pending orders
    @PostMapping("/process/historical")
    public String processHistoricalDisbursements() {
        int created = disbursementService.processHistoricalDisbursements();
        return "Historical disbursements processed successfully. Created: " + created;
    }

    // POST to backfill historical monthly minimum fee disbursements
    @PostMapping("/process/historical/monthly-fees")
    public String processHistoricalMonthlyFees() {
        int created = disbursementService.processHistoricalMonthlyTopUps();
        return "Historical monthly fee disbursements processed successfully. Created: " + created;
    }
}