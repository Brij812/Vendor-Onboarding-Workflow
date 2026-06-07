package com.zamp.vendoronboarding.repository;

import com.zamp.vendoronboarding.entity.VendorSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VendorSubmissionRepository extends JpaRepository<VendorSubmission, UUID> {
}
