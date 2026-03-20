package com.sequra.challenge.config;

import com.sequra.challenge.model.Merchant;
import com.sequra.challenge.model.Order;
import com.sequra.challenge.model.PaymentFrequency;
import com.sequra.challenge.repository.MerchantRepository;
import com.sequra.challenge.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${dataloader.exit-after-load:true}")
    private boolean exitAfterLoad;

    @Override
    public void run(String... args) throws Exception {
        // Comment this line if you want not to reload data on every startup (useful for development)
        loadMerchants();
        loadOrders();
    }

    public void loadOrders() throws Exception {
        loadOrdersInternal();
    }

    private void loadOrdersInternal() throws Exception {
        // Build a map of merchant reference -> merchant id for fast lookups
        Map<String, String> merchantReferenceToId = new HashMap<>();
        merchantRepository.findAll().forEach(m -> merchantReferenceToId.put(m.getReference(), m.getId().toString()));

        Resource resource = resourceLoader.getResource("classpath:orders.csv");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String line;
            boolean firstLine = true;
            int batchSize = 2000;
            int count = 0;
            java.util.List<MapSqlParameterSource> batch = new java.util.ArrayList<>(batchSize);

            final String sql = "INSERT INTO orders (id, merchant_id, amount, created_at, disbursed) " +
                    "VALUES (:id, :merchantId, :amount, :createdAt, :disbursed)";

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] parts = line.split(";");
                if (parts.length < 4) continue; // skip invalid lines

                String id = parts[0];
                String merchantReference = parts[1];
                String merchantId = merchantReferenceToId.get(merchantReference);
                if (merchantId == null) continue; // unknown merchant, skip

                if (orderRepository.existsById(id)) continue; // avoid duplicates

                BigDecimal amount = new BigDecimal(parts[2]);
                LocalDate createdAt = LocalDate.parse(parts[3]);

                MapSqlParameterSource params = new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("merchantId", UUID.fromString(merchantId))
                        .addValue("amount", amount)
                        .addValue("createdAt", createdAt)
                        .addValue("disbursed", false);

                batch.add(params);
                count++;

                if (batch.size() >= batchSize) {
                    jdbc.batchUpdate(sql, batch.toArray(new MapSqlParameterSource[0]));
                    batch.clear();
                    System.out.println("[DataLoader] Inserted " + count + " orders...");
                }
            }

            if (!batch.isEmpty()) {
                jdbc.batchUpdate(sql, batch.toArray(new MapSqlParameterSource[0]));
            }

            System.out.println("[DataLoader] Loaded " + count + " orders from orders.csv");
        }
    }

    private void loadMerchants() throws Exception {
        Resource resource = resourceLoader.getResource("classpath:merchants.csv");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] parts = line.split(";");
                if (parts.length < 6) continue; // skip invalid lines

                UUID id = UUID.fromString(parts[0]);
                String reference = parts[1];
                String email = parts[2];
                LocalDate liveOn = parts[3].isEmpty() ? null : LocalDate.parse(parts[3]);
                PaymentFrequency frequency = PaymentFrequency.valueOf(parts[4]);
                BigDecimal fee = parts[5].isEmpty() ? BigDecimal.ZERO : new BigDecimal(parts[5]);

                if (!merchantRepository.existsById(id)) {
                    Merchant merchant = new Merchant();
                    merchant.setId(id);
                    merchant.setReference(reference);
                    merchant.setEmail(email);
                    merchant.setLiveOn(liveOn);
                    merchant.setPaymentFrequency(frequency);
                    merchant.setMinimumMonthlyFee(fee);

                    merchantRepository.save(merchant);
                }
            }
        }
    }
}