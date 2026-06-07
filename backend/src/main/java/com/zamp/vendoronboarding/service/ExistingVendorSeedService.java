package com.zamp.vendoronboarding.service;

import com.zamp.vendoronboarding.config.ExistingVendorSeedRecord;
import com.zamp.vendoronboarding.entity.ExistingVendor;
import com.zamp.vendoronboarding.entity.enums.VendorStatus;
import com.zamp.vendoronboarding.repository.ExistingVendorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExistingVendorSeedService {

    public static final List<ExistingVendorSeedRecord> SEED_RECORDS = List.of(
            new ExistingVendorSeedRecord(
                    "BrightLayer Technologies Pvt Ltd", "India", "29ABCDE1234F1Z5", "8821",
                    VendorStatus.ACTIVE, null),
            new ExistingVendorSeedRecord(
                    "Nova Logistics LLP", "India", "27PQRSX9876K1Z2", "1190",
                    VendorStatus.ACTIVE, null),
            new ExistingVendorSeedRecord(
                    "Blackstone Imports", "India", "12BADXX9999Z1Z9", "7712",
                    VendorStatus.BLOCKED, "Previously blocked due to suspicious bank account mismatch"),
            new ExistingVendorSeedRecord(
                    "Acme Cloud Services Pvt Ltd", "India", "29ZZZZZ1234F1Z5", "4422",
                    VendorStatus.ACTIVE, null)
    );

    private final ExistingVendorRepository existingVendorRepository;

    public ExistingVendorSeedService(ExistingVendorRepository existingVendorRepository) {
        this.existingVendorRepository = existingVendorRepository;
    }

    @Transactional(readOnly = true)
    public long count() {
        return existingVendorRepository.count();
    }

    @Transactional
    public long seedAll() {
        if (existingVendorRepository.count() > 0) {
            return 0;
        }
        long inserted = 0;
        for (ExistingVendorSeedRecord record : SEED_RECORDS) {
            existingVendorRepository.save(toEntity(record));
            inserted++;
        }
        return inserted;
    }

    @Transactional
    public long reseedAll() {
        existingVendorRepository.deleteAll();
        return seedAll();
    }

    private ExistingVendor toEntity(ExistingVendorSeedRecord record) {
        ExistingVendor vendor = new ExistingVendor();
        vendor.setLegalName(record.legalName());
        vendor.setCountry(record.country());
        vendor.setTaxId(record.taxId());
        vendor.setBankAccountLast4(record.bankAccountLast4());
        vendor.setStatus(record.status());
        vendor.setRiskNote(record.riskNote());
        return vendor;
    }
}
