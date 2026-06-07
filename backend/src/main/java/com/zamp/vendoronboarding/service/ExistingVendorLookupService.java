package com.zamp.vendoronboarding.service;

import com.zamp.vendoronboarding.entity.ExistingVendor;
import com.zamp.vendoronboarding.entity.enums.VendorStatus;
import com.zamp.vendoronboarding.repository.ExistingVendorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ExistingVendorLookupService {

    private final ExistingVendorRepository existingVendorRepository;

    public ExistingVendorLookupService(ExistingVendorRepository existingVendorRepository) {
        this.existingVendorRepository = existingVendorRepository;
    }

    public List<ExistingVendor> findAll() {
        return existingVendorRepository.findAllByOrderByLegalNameAsc();
    }

    public Optional<ExistingVendor> findByTaxId(String taxId) {
        if (taxId == null || taxId.isBlank()) {
            return Optional.empty();
        }
        return existingVendorRepository.findByTaxId(taxId.trim());
    }

    public List<ExistingVendor> findByBankAccountLast4(String bankAccountLast4) {
        if (bankAccountLast4 == null || bankAccountLast4.isBlank()) {
            return List.of();
        }
        return existingVendorRepository.findByBankAccountLast4(bankAccountLast4.trim());
    }

    public Optional<ExistingVendor> findByNormalizedLegalName(String normalizedLegalName) {
        if (normalizedLegalName == null || normalizedLegalName.isBlank()) {
            return Optional.empty();
        }
        return existingVendorRepository.findByNormalizedLegalName(normalizedLegalName.trim());
    }

    public List<ExistingVendor> findActiveVendors() {
        return existingVendorRepository.findByStatus(VendorStatus.ACTIVE);
    }

    public List<ExistingVendor> findBlockedVendors() {
        return existingVendorRepository.findByStatus(VendorStatus.BLOCKED);
    }
}
