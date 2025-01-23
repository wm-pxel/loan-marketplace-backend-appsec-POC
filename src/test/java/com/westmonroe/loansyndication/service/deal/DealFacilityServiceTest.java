package com.westmonroe.loansyndication.service.deal;

import com.westmonroe.loansyndication.exception.ValidationException;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.deal.DealFacility;
import com.westmonroe.loansyndication.service.PicklistService;
import com.westmonroe.loansyndication.service.UserService;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static com.westmonroe.loansyndication.utils.TestConstants.TEST_DEAL_UUID_2;
import static com.westmonroe.loansyndication.utils.TestConstants.TEST_USER_UUID_3;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
public class DealFacilityServiceTest {

    @Autowired
    private DealFacilityService dealFacilityService;
    @Autowired
    private DealService dealService;
    @Autowired
    private UserService userService;
    @Autowired
    private PicklistService picklistService;

    @BeforeEach
    void setUp(@Autowired Flyway flyway) {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    void givenInvalidFacilityTypeAndFieldPair_whenCreatingDealFacility_thenValidationError() {
        User testUser = userService.getUserByUid(TEST_USER_UUID_3);
        Deal testDeal = dealService.getDealByUid(TEST_DEAL_UUID_2, testUser);
        BigDecimal facilityAmount = BigDecimal.valueOf(12500000.00);

        DealFacility testFacility = new DealFacility();
        testFacility.setDeal(testDeal);
        testFacility.setFacilityAmount(facilityAmount);
        testFacility.setFacilityType(picklistService.getPicklistForCategoryAndOption("Facility Type", "Term"));
        testFacility.setRevolverUtil(1000);

        assertThatThrownBy(() -> dealFacilityService.save(testFacility, testUser))
            .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> dealFacilityService.update(testFacility, testUser))
            .isInstanceOf(ValidationException.class);

        testFacility.setFacilityType(picklistService.getPicklistForCategoryAndOption("Facility Type", "Revolver"));
        testFacility.setRevolverUtil(null);
        testFacility.setAmortization("This is the new amortization");

        assertThatThrownBy(() -> dealFacilityService.save(testFacility, testUser))
            .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> dealFacilityService.update(testFacility, testUser))
            .isInstanceOf(ValidationException.class);
    }
}
