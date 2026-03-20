package com.sequra.challenge.controller;

import com.sequra.challenge.model.Merchant;
import com.sequra.challenge.model.Order;
import com.sequra.challenge.config.DataLoader;
import com.sequra.challenge.repository.MerchantRepository;
import com.sequra.challenge.repository.OrderRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private DataLoader dataLoader;

    @PostMapping("/import")
    public String importOrders(@RequestParam("file") MultipartFile file) throws Exception {
        List<Order> orders = new ArrayList<>();

        try (Reader reader = new InputStreamReader(file.getInputStream())) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withDelimiter(';') // CSV usa punto y coma
                    .withFirstRecordAsHeader()
                    .parse(reader);

            for (CSVRecord record : records) {
                Order o = new Order();
                o.setId(record.get("id"));

                String merchantRef = record.get("merchant_reference");
                Merchant merchant = merchantRepository.findByReference(merchantRef)
                        .orElseThrow(() -> new RuntimeException("Merchant not found: " + merchantRef));
                o.setMerchant(merchant);

                o.setAmount(new BigDecimal(record.get("amount")));
                o.setCreatedAt(LocalDate.parse(record.get("created_at")));
                o.setDisbursed(false);

                orders.add(o);
            }
        }

        orderRepository.saveAll(orders);
        return "Imported " + orders.size() + " orders.";
    }

    // GET all orders (paginated)
    @GetMapping
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    // GET order by ID
    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable String id) {
        return orderRepository.findById(id).orElse(null);
    }
}