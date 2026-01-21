package com.example.ewallet.service;

import com.example.ewallet.entity.ClaimRecord;
import com.example.ewallet.entity.User;
import com.example.ewallet.repository.ClaimRepository;
import com.example.ewallet.repository.PolicyRepository;
import com.example.ewallet.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class InsuranceIntegrationTest {

    @Autowired
    private InsuranceService insuranceService;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        claimRepository.deleteAll();
        policyRepository.deleteAll();
        userRepository.deleteAll();

        userRepository.save(new User("insUser", "0111111111"));
    }

    @Test
    void testSubmitClaim_Integration() {
        ClaimRecord claim = insuranceService.submitClaim(
                "0111111111",
                "POLICY001",
                200.0
        );

        assertNotNull(claim);
        assertEquals("Pending", claim.getStatus());
        assertEquals("POLICY001", claim.getPolicyId());

        // Verify persistence
        assertEquals(1, claimRepository.count());
    }
}
