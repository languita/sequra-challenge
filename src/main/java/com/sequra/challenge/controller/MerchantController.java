package com.sequra.challenge.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sequra.challenge.config.DataLoader;
import com.sequra.challenge.model.Merchant;
import com.sequra.challenge.model.PaymentFrequency;
import com.sequra.challenge.repository.MerchantRepository;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/merchants")
public class MerchantController {

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private DataLoader dataLoader;

    @PostMapping("/import")
    public String importMerchants(@RequestParam("file") MultipartFile file) throws Exception {
        List<Merchant> merchants = new ArrayList<>();
        try (Reader reader = new InputStreamReader(file.getInputStream())) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withDelimiter(';')
                    .withFirstRecordAsHeader()
                    .parse(reader);

            for (CSVRecord record : records) {
                Merchant m = new Merchant();
                m.setId(UUID.fromString(record.get("id")));
                m.setReference(record.get("reference"));
                m.setEmail(record.get("email"));
                if (!record.get("live_on").isEmpty()) {
                    m.setLiveOn(LocalDate.parse(record.get("live_on")));
                }
                m.setPaymentFrequency(
                        record.get("disbursement_frequency").equalsIgnoreCase("DAILY")
                                ? PaymentFrequency.DAILY
                                : PaymentFrequency.WEEKLY
                );
                if (!record.get("minimum_monthly_fee").isEmpty()) {
                    m.setMinimumMonthlyFee(new BigDecimal(record.get("minimum_monthly_fee")));
                }
                merchants.add(m);
            }
        }
        merchantRepository.saveAll(merchants);
        return "Imported " + merchants.size() + " merchants.";
    }


    // GET all merchants (paginated)
    @GetMapping
    public Page<Merchant> getAllMerchants(Pageable pageable) {
        return merchantRepository.findAll(pageable);
    }

    // GET merchant by ID
    @GetMapping("/{id}")
    public Merchant getMerchantById(@PathVariable UUID id) {
        return merchantRepository.findById(id).orElse(null);
    }    
}