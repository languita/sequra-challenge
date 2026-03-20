package com.sequra.challenge.service;

import com.sequra.challenge.model.Disbursement;
import com.sequra.challenge.model.Merchant;
import com.sequra.challenge.model.Order;
import com.sequra.challenge.model.PaymentFrequency;
import com.sequra.challenge.repository.DisbursementRepository;
import com.sequra.challenge.repository.MerchantRepository;
import com.sequra.challenge.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DisbursementService {

    private static final BigDecimal COMMISSION_LOW = new BigDecimal("0.0100");
    private static final BigDecimal COMMISSION_MID = new BigDecimal("0.0095");
    private static final BigDecimal COMMISSION_HIGH = new BigDecimal("0.0085");

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DisbursementRepository disbursementRepository;

    /** DAILY DISBURSEMENTS */
    @Transactional
    public void processDailyDisbursements() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate yesterday = today.minusDays(1);

        List<Merchant> dailyMerchants = merchantRepository.findByPaymentFrequency(PaymentFrequency.DAILY);

        for (Merchant merchant : dailyMerchants) {
            processOrders(merchant, yesterday, yesterday);
        }
    }

    /** WEEKLY DISBURSEMENTS */
    @Transactional
    public void processWeeklyDisbursements() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        List<Merchant> weeklyMerchants = merchantRepository.findByPaymentFrequency(PaymentFrequency.WEEKLY);

        for (Merchant merchant : weeklyMerchants) {
            if (merchant.getLiveOn() != null && merchant.getLiveOn().getDayOfWeek() == today.getDayOfWeek()) {
                // semana pasada
                LocalDate startOfWeek = today.minusWeeks(1).with(DayOfWeek.MONDAY);
                LocalDate endOfWeek = startOfWeek.plusDays(6);
                processOrders(merchant, startOfWeek, endOfWeek);
            }
        }
    }

    /** MONTHLY TOP-UP TO MINIMUM FEE */
    @Transactional
    public void processMonthlyTopUp() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate firstDayPrevMonth = today.minusMonths(1).withDayOfMonth(1);
        LocalDate lastDayPrevMonth = firstDayPrevMonth.withDayOfMonth(firstDayPrevMonth.lengthOfMonth());

        List<Merchant> allMerchants = merchantRepository.findAll();

        for (Merchant merchant : allMerchants) {
            BigDecimal totalDisbursed = disbursementRepository
                    .findByMerchantAndDateBetween(merchant, firstDayPrevMonth, lastDayPrevMonth)
                    .stream()
                    .map(Disbursement::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalDisbursed.compareTo(merchant.getMinimumMonthlyFee()) < 0) {
                BigDecimal topUp = merchant.getMinimumMonthlyFee().subtract(totalDisbursed);

                Disbursement d = new Disbursement();
                d.setMerchant(merchant);
                d.setReference(buildDisbursementReference(merchant, today, true));
                d.setOrderAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
                d.setFeeAmount(topUp.setScale(2, RoundingMode.HALF_UP));
                d.setAmount(topUp.setScale(2, RoundingMode.HALF_UP));
                d.setDate(today); // o el último día del mes anterior
                d.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
                d.setMonthlyFeeDisbursement(true);

                disbursementRepository.save(d);
            }
        }
    }

    /** HISTORICAL BACKFILL FOR ALL PENDING ORDERS */
    @Transactional
    public int processHistoricalDisbursements() {
        List<Merchant> allMerchants = merchantRepository.findAll();
        int createdDisbursements = 0;

        for (Merchant merchant : allMerchants) {
            List<Order> pendingOrders = orderRepository.findByMerchantAndDisbursedFalseOrderByCreatedAtAsc(merchant);
            if (pendingOrders.isEmpty()) {
                continue;
            }

            if (merchant.getPaymentFrequency() == PaymentFrequency.DAILY) {
                createdDisbursements += processHistoricalDaily(merchant, pendingOrders);
            } else if (merchant.getPaymentFrequency() == PaymentFrequency.WEEKLY) {
                createdDisbursements += processHistoricalWeekly(merchant, pendingOrders);
            }
        }

        return createdDisbursements;
    }

    /** UTIL: procesar orders de un merchant entre fechas y marcar disbursed */
    private void processOrders(Merchant merchant, LocalDate start, LocalDate end) {
        List<Order> orders = orderRepository.findByMerchantAndDisbursedFalseAndCreatedAtBetween(
                merchant, start, end
        );

        if (orders.isEmpty()) return;

        DisbursementAmounts amounts = calculateDisbursementAmounts(orders);

        Disbursement d = new Disbursement();
        d.setMerchant(merchant);
        d.setOrderAmount(amounts.orderAmount());
        d.setFeeAmount(amounts.feeAmount());
        d.setAmount(amounts.netAmount());
        d.setDate(end); // puedes usar start o end según convenga
        d.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        d.setMonthlyFeeDisbursement(false);

        disbursementRepository.save(d);

        orders.forEach(o -> o.setDisbursed(true));
        orderRepository.saveAll(orders);
    }

    private int processHistoricalDaily(Merchant merchant, List<Order> pendingOrders) {
        Map<LocalDate, List<Order>> ordersByDate = pendingOrders.stream()
            .collect(Collectors.groupingBy(Order::getCreatedAt));

        int created = 0;
        for (Map.Entry<LocalDate, List<Order>> entry : ordersByDate.entrySet()) {
            if (createDisbursementForOrders(merchant, entry.getValue(), entry.getKey())) {
                created++;
            }
        }
        return created;
    }

    private int processHistoricalWeekly(Merchant merchant, List<Order> pendingOrders) {
        DayOfWeek payoutDay = merchant.getLiveOn() != null
            ? merchant.getLiveOn().getDayOfWeek()
            : DayOfWeek.MONDAY;

        Map<LocalDate, List<Order>> ordersByDisbursementDate = pendingOrders.stream()
            .collect(Collectors.groupingBy(o -> calculateWeeklyDisbursementDate(o.getCreatedAt(), payoutDay)));

        int created = 0;
        for (Map.Entry<LocalDate, List<Order>> entry : ordersByDisbursementDate.entrySet()) {
            if (createDisbursementForOrders(merchant, entry.getValue(), entry.getKey())) {
                created++;
            }
        }
        return created;
    }

    private boolean createDisbursementForOrders(Merchant merchant, List<Order> orders, LocalDate disbursementDate) {
        if (orders.isEmpty()) {
            return false;
        }

        DisbursementAmounts amounts = calculateDisbursementAmounts(orders);

        Disbursement d = new Disbursement();
        d.setMerchant(merchant);
        d.setReference(buildDisbursementReference(merchant, disbursementDate, false));
        d.setOrderAmount(amounts.orderAmount());
        d.setFeeAmount(amounts.feeAmount());
        d.setAmount(amounts.netAmount());
        d.setDate(disbursementDate);
        d.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        d.setMonthlyFeeDisbursement(false);
        disbursementRepository.save(d);

        orders.forEach(o -> {
            o.setDisbursement(d);
            o.setDisbursed(true);
        });
        orderRepository.saveAll(orders);
        return true;
    }

    private BigDecimal calculateOrderFee(Order order) {
        BigDecimal amount = order.getAmount();
        BigDecimal commissionRate = getCommissionRate(amount);
        return amount.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);
    }

    private DisbursementAmounts calculateDisbursementAmounts(List<Order> orders) {
        BigDecimal orderAmount = orders.stream()
                .map(Order::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal feeAmount = orders.stream()
                .map(this::calculateOrderFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal netAmount = orderAmount.subtract(feeAmount).setScale(2, RoundingMode.HALF_UP);
        return new DisbursementAmounts(orderAmount, feeAmount, netAmount);
    }

    private LocalDate calculateWeeklyDisbursementDate(LocalDate orderDate, DayOfWeek payoutDay) {
        LocalDate orderWeekStart = orderDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate nextWeekStart = orderWeekStart.plusWeeks(1);
        return nextWeekStart.with(TemporalAdjusters.nextOrSame(payoutDay));
    }

    private String buildDisbursementReference(Merchant merchant, LocalDate disbursementDate, boolean monthlyFee) {
        String merchantRef = merchant.getReference() == null ? "UNKNOWN" : merchant.getReference().replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (merchantRef.isEmpty()) {
            merchantRef = "M";
        }
        String type = monthlyFee ? "MF" : "ORD";
        String datePart = disbursementDate.toString().replace("-", "");
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return type + "-" + merchantRef + "-" + datePart + "-" + suffix;
    }

    private record DisbursementAmounts(BigDecimal orderAmount, BigDecimal feeAmount, BigDecimal netAmount) {
    }

    private BigDecimal getCommissionRate(BigDecimal amount) {
        if (amount.compareTo(new BigDecimal("50")) < 0) {
            return COMMISSION_LOW;
        }
        if (amount.compareTo(new BigDecimal("300")) < 0) {
            return COMMISSION_MID;
        }
        return COMMISSION_HIGH;
    }
}