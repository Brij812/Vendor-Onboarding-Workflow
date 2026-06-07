package com.zamp.vendoronboarding.service;

import com.zamp.vendoronboarding.dto.ExistingVendorResponse;
import com.zamp.vendoronboarding.entity.ExistingVendor;
import com.zamp.vendoronboarding.repository.ExistingVendorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ExistingVendorService {

    private final ExistingVendorRepository existingVendorRepository;

    public ExistingVendorService(ExistingVendorRepository existingVendorRepository) {
        this.existingVendorRepository = existingVendorRepository;
    }

    public List<ExistingVendorResponse> findAll() {
        return existingVendorRepository.findAllByOrderByLegalNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    private ExistingVendorResponse toResponse(ExistingVendor vendor) {
        return new ExistingVendorResponse(
                vendor.getId(),
                vendor.getLegalName(),
                vendor.getCountry(),
                vendor.getTaxId(),
                vendor.getBankAccountLast4(),
                vendor.getStatus(),
                vendor.getRiskNote()
        );
    }
}
