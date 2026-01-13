package com.example.ewallet.service;

import com.example.ewallet.entity.BasePolicy;
import com.example.ewallet.entity.ClaimRecord;
import com.example.ewallet.entity.MotorPolicy;
import com.example.ewallet.entity.TravelPolicy;
import com.example.ewallet.repository.ClaimRepository;
import com.example.ewallet.repository.PolicyRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import com.example.ewallet.service.PaymentService;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class InsuranceService {

    private final PolicyRepository policyRepository;
    private final ClaimRepository claimRepository;
    private final PaymentService paymentService;
    private final NotificationService notificationService;

    public InsuranceService(PolicyRepository policyRepository,
                            ClaimRepository claimRepository,
                            PaymentService paymentService,
                            @Lazy NotificationService notificationService) {
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
        this.paymentService = paymentService;
        this.notificationService = notificationService;
    }

    // Purchase Motor Insurance
    public void purchaseMotorPolicy(String username, String plateNo, String model) {
        double price = 500.0;

        boolean paymentSuccess = paymentService.processPayment(username, price, "Purchase Motor Insurance");

        if (paymentSuccess) {
            MotorPolicy policy = new MotorPolicy();
            policy.setUserId(username);
            policy.setStatus("Active");

            policy.setPurchaseDate(new Date());
            policy.setPolicyType("Motor");
            policy.setExpiryDate(new Date());

            policy.setPlateNumber(plateNo);
            policy.setCarModel(model);

            policyRepository.save(policy);
            System.out.println("Payment Successful! Motor Policy Created for " + username);
        } else {
            System.out.println("Payment Failed: Insufficient Balance for " + username);
        }
    }

    // Purchase Travel Insurance
    public void purchaseTravelPolicy(String username, String destination, int pax) {
        double price = 80.0 * pax;

        if (paymentService.processPayment(username, price, "Purchase Travel Insurance")) {
            TravelPolicy policy = new TravelPolicy();
            policy.setUserId(username);
            policy.setStatus("Active");
            policy.setPurchaseDate(new Date());
            policy.setPolicyType("Travel");

            policy.setDestination(destination);
            policy.setTravelPax(pax);

            policyRepository.save(policy);
            System.out.println("Payment Successful! Travel Policy Created for " + username);
        } else {
            System.out.println("Payment Failed: Insufficient Balance.");
        }
    }

    // Submit Insurance Claim
    public void submitClaim(String policyId, double amount) {
        ClaimRecord claim = new ClaimRecord();
        claim.setPolicyId(policyId);
        claim.setAmount(amount);
        claim.setStatus("Pending");
        claim.setIncidentDate(new Date());

        claimRepository.save(claim);
        System.out.println("Claim Submitted for Policy: " + policyId);
        
        // Get policy to determine type and username
        Optional<BasePolicy> policy = policyRepository.findById(policyId);
        if (policy.isPresent()) {
            String username = policy.get().getUserId();
            String policyType = policy.get().getPolicyType();
            notificationService.notifyClaimUpdate(username, policyType, "Pending", amount);
        }
    }

    // Get User Policies
    public List<BasePolicy> getPolicyList(String userId) {
        return policyRepository.findByUserId(userId);
    }

    //Get Claim/Policy Status (Interfaces)
    public String getClaimStatus(String claimId) {
        Optional<ClaimRecord> claim = claimRepository.findById(claimId);
        return claim.map(ClaimRecord::getStatus).orElse("Not Found");
    }

    public String getPolicyStatus(String policyId) {
        Optional<BasePolicy> policy = policyRepository.findById(policyId);
        return policy.map(BasePolicy::getStatus).orElse("Not Found");
    }
}