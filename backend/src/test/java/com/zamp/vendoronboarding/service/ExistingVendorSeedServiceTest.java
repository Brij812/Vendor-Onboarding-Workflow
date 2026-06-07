package com.zamp.vendoronboarding.service;

import com.zamp.vendoronboarding.entity.ExistingVendor;
import com.zamp.vendoronboarding.repository.ExistingVendorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExistingVendorSeedServiceTest {

    @Mock
    private ExistingVendorRepository existingVendorRepository;

    @InjectMocks
    private ExistingVendorSeedService existingVendorSeedService;

    @Test
    void seedAll_whenEmpty_insertsFourVendors() {
        when(existingVendorRepository.count()).thenReturn(0L);

        long inserted = existingVendorSeedService.seedAll();

        assertEquals(4L, inserted);
        verify(existingVendorRepository, times(4)).save(org.mockito.ArgumentMatchers.any(ExistingVendor.class));
    }

    @Test
    void seedAll_whenAlreadySeeded_insertsNothing() {
        when(existingVendorRepository.count()).thenReturn(4L);

        long inserted = existingVendorSeedService.seedAll();

        assertEquals(0L, inserted);
        verify(existingVendorRepository, times(0)).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void reseedAll_replacesSeedRecords() {
        ArgumentCaptor<ExistingVendor> captor = ArgumentCaptor.forClass(ExistingVendor.class);

        long inserted = existingVendorSeedService.reseedAll();

        verify(existingVendorRepository).deleteAll();
        verify(existingVendorRepository, times(ExistingVendorSeedService.SEED_RECORDS.size())).save(captor.capture());
        assertEquals(ExistingVendorSeedService.SEED_RECORDS.size(), inserted);
        assertEquals("29ABCDE1234F1Z5", captor.getAllValues().get(0).getTaxId());
    }
}
