package com.jobtrail.backend.service;

import com.jobtrail.backend.dto.StatusHistoryDto.StatusUpdateRequest;
import com.jobtrail.backend.exception.IllegalStatusTransitionException;
import com.jobtrail.backend.mapper.ApplicationMapper;
import com.jobtrail.backend.model.Application;
import com.jobtrail.backend.model.Application.StatusEnum;
import com.jobtrail.backend.repository.ApplicationRepository;
import com.jobtrail.backend.repository.RawEmailRepository;
import com.jobtrail.backend.repository.StatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private StatusHistoryRepository statusHistoryRepository;

    @Mock
    private RawEmailRepository rawEmailRepository;

    @Mock
    private ReminderService reminderService;

    @Mock
    private ApplicationMapper applicationMapper;

    @InjectMocks
    private ApplicationService applicationService;

    private Application mockApplication;

    @BeforeEach
    void setUp() {
        mockApplication = new Application();
        mockApplication.setId(1L);
        mockApplication.setCompany("Stripe");
        mockApplication.setRole("Software Engineer");
        mockApplication.setStatus(StatusEnum.APPLIED);
    }

    @Test
    void testValidTransition_AppliedToInterview() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(mockApplication));
        when(applicationRepository.save(any(Application.class))).thenReturn(mockApplication);

        StatusUpdateRequest request = new StatusUpdateRequest(Application.StatusEnum.INTERVIEW, "Interview scheduled", false);

        assertDoesNotThrow(() -> applicationService.updateApplicationStatus(1L, request));

        assertEquals(StatusEnum.INTERVIEW, mockApplication.getStatus());
        verify(statusHistoryRepository, times(1)).save(any());
        verify(applicationRepository, times(1)).save(mockApplication);
    }

    @Test
    void testInvalidTransition_RejectedToOffer() {
        mockApplication.setStatus(StatusEnum.REJECTED);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(mockApplication));

        StatusUpdateRequest request = new StatusUpdateRequest(Application.StatusEnum.OFFER, "Got offer!", false);

        IllegalStatusTransitionException exception = assertThrows(
                IllegalStatusTransitionException.class,
                () -> applicationService.updateApplicationStatus(1L, request)
        );

        assertTrue(exception.getMessage().contains("Cannot transition from REJECTED to OFFER"));
        verify(statusHistoryRepository, never()).save(any());
    }

    @Test
    void testNoOpTransition_AppliedToApplied() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(mockApplication));

        StatusUpdateRequest request = new StatusUpdateRequest(Application.StatusEnum.APPLIED, "Reverted", false);

        assertDoesNotThrow(() -> applicationService.updateApplicationStatus(1L, request));

        // Status history should not be recorded for no-op changes
        verify(statusHistoryRepository, never()).save(any());
    }
}
