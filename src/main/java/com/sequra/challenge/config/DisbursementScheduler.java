package com.sequra.challenge.config;

import com.sequra.challenge.service.DisbursementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DisbursementScheduler {

    @Autowired
    private DisbursementService disbursementService;

    // Daily job: 08:00 UTC
    @Scheduled(cron = "0 0 8 * * *", zone = "UTC")
    public void dailyJob() {
        disbursementService.processDailyDisbursements();
    }

    // Weekly job: 08:00 UTC, filtered inside the service by merchant liveOn day
    @Scheduled(cron = "0 0 8 * * *", zone = "UTC")
    public void weeklyJob() {
        disbursementService.processWeeklyDisbursements();
    }

    // Monthly job: 1st day of month, 08:00 UTC
    @Scheduled(cron = "0 0 8 1 * *", zone = "UTC")
    public void monthlyJob() {
        disbursementService.processMonthlyTopUp();
    }
}