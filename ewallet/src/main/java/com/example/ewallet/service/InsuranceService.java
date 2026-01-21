package com.example.ewallet.service;

import com.example.ewallet.entity.BasePolicy;
import com.example.ewallet.entity.ClaimRecord;
import com.example.ewallet.entity.MotorPolicy;
import com.example.ewallet.entity.TravelPolicy;
import com.example.ewallet.entity.User;
import com.example.ewallet.repository.ClaimRepository;
import com.example.ewallet.repository.PolicyRepository;
import com.example.ewallet.repository.UserRepository;
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
    private final UserRepository userRepository;

    public InsuranceService(PolicyRepository policyRepository,
                            ClaimRepository claimRepository,
                            PaymentService paymentService,
                            @Lazy NotificationService notificationService,
                            UserRepository userRepository) {
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
        this.paymentService = paymentService;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    // Purchase Motor Insurance
    public void purchaseMotorPolicy(String phoneNumber, String username, String plateNo, String model) {
        double price = 500.0;

        boolean paymentSuccess = paymentService.processPayment(phoneNumber, username, price, "Purchase Motor Insurance");

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
            System.out.println("Motor Policy Created for " + username);
            
        } else {
            System.out.println("Payment Failed: Insufficient Balance for " + username);
        }
    }

    // Purchase Travel Insurance
    public void purchaseTravelPolicy(String phoneNumber, String username, String destination, int pax) {
        double price = 80.0 * pax;

        if (paymentService.processPayment(phoneNumber, username, price, "Purchase Travel Insurance")) {
            TravelPolicy policy = new TravelPolicy();
            policy.setUserId(username);
            policy.setStatus("Active");
            policy.setPurchaseDate(new Date());
            policy.setPolicyType("Travel");

            policy.setDestination(destination);
            policy.setTravelPax(pax);

            policyRepository.save(policy);
            System.out.println("Travel Policy Created for " + username);

        } else {
            System.out.println("Payment Failed: Insufficient Balance.");
        }
    }

    // Submit Insurance Claim
    public ClaimRecord submitClaim(String phoneNumber, String policyId, double amount) {
        ClaimRecord claim = new ClaimRecord();
        claim.setPolicyId(policyId);
        claim.setAmount(amount);
        claim.setStatus("Pending");
        claim.setIncidentDate(new Date());

        // The save method returns the saved object (including the generated ID).
        ClaimRecord savedClaim = claimRepository.save(claim);

        System.out.println("Claim Submitted for Policy: " + policyId + " (Status: Pending Review)");

        // Always generate notification
        notificationService.generateNotification(phoneNumber, "CLAIM",
                String.format("Claim submitted for Policy %s - RM %.2f (Status: Pending Review)", 
                        policyId, amount));

        return savedClaim;
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