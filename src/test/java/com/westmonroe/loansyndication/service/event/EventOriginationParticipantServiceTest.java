package com.westmonroe.loansyndication.service.event;

import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.event.EventOriginationParticipant;
import com.westmonroe.loansyndication.model.event.EventParticipantFacility;
import com.westmonroe.loansyndication.service.UserService;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static com.westmonroe.loansyndication.utils.TestConstants.TEST_USER_UUID_5;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.flyway.clean-disabled=false")
@Testcontainers
class EventOriginationParticipantServiceTest {

    @Autowired
    private EventParticipantService eventParticipantService;

    @Autowired
    private EventOriginationParticipantService eventOriginationParticipantService;

    @Autowired
    private EventParticipantFacilityService eventParticipantFacilityService;

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp(@Autowired Flyway flyway) {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    void givenVariedEventParticipantFacilityCommitments_whenGettingEventParticipant_thenValidTotalCommitment() {

        Long eventParticipantId = 4L;
        User currentUser = userService.getUserByUid(TEST_USER_UUID_5, true); // Thomas H. Truitt

        // Get the Deal Participant Facilities and verify that none exist
        List<EventParticipantFacility> participantFacilities = eventParticipantFacilityService.getEventParticipantFacilitiesByEventParticipantId(eventParticipantId);
        assertThat(participantFacilities)
            .isNotNull()
            .hasSize(4);

        EventOriginationParticipant eventOriginationParticipant = eventOriginationParticipantService.getEventOriginationParticipantById(eventParticipantId);

        EventParticipantFacility firstFacility = participantFacilities.get(0);
        EventParticipantFacility secondFacility = participantFacilities.get(1);
        EventParticipantFacility thirdFacility = participantFacilities.get(2);

        // Verify that eventParticipant has null totalCommitmentAmount
        assertThat(firstFacility.getCommitmentAmount()).isNull();
        assertThat(secondFacility.getCommitmentAmount()).isNull();
        assertThat(thirdFacility.getCommitmentAmount()).isNull();
        assertThat(eventOriginationParticipant.getTotalCommitmentAmount()).isNull();

        // Set some commitmentAmount to a number and don't set allocationAmount (will all be null)

        BigDecimal commitmentAmount = BigDecimal.valueOf(2000000.00);

        firstFacility.setCommitmentAmount(commitmentAmount);

        secondFacility.setCommitmentAmount(commitmentAmount);

        eventParticipantFacilityService.update(firstFacility, currentUser);
        eventParticipantFacilityService.update(secondFacility, currentUser);

        eventOriginationParticipant = eventOriginationParticipantService.getEventOriginationParticipantById(eventParticipantId);

        BigDecimal totalCommitmentAmount = BigDecimal.valueOf(4000000.00);

        assertThat(eventOriginationParticipant.getTotalCommitmentAmount().compareTo(totalCommitmentAmount) == 0).isTrue();
    }

    @Test
    void givenVariedEventParticipantFacilityAllocations_whenGettingEventParticipant_thenValidTotalAllocation() {

        Long eventParticipantId = 4L;
        User currentUser = userService.getUserByUid(TEST_USER_UUID_5, true);    // Thomas H. Truitt

        // Get the Deal Participant Facilities and verify that none exist.
        List<EventParticipantFacility> participantFacilities = eventParticipantFacilityService.getEventParticipantFacilitiesByEventParticipantId(eventParticipantId);
        assertThat(participantFacilities)
            .isNotNull()
            .hasSize(4);

        // Verify that eventParticipant has null totalAllocationAmount
        EventOriginationParticipant eventOriginationParticipant = eventOriginationParticipantService.getEventOriginationParticipantById(eventParticipantId);
        assertThat(participantFacilities.get(0).getAllocationAmount()).isNull();
        assertThat(participantFacilities.get(1).getAllocationAmount()).isNull();
        assertThat(participantFacilities.get(2).getAllocationAmount()).isNull();
        assertThat(participantFacilities.get(3).getAllocationAmount()).isNull();
        assertThat(eventOriginationParticipant.getTotalAllocationAmount()).isNull();

        //Set commitmentAmount to number and allocation amount to null

        BigDecimal commitmentAmount = BigDecimal.valueOf(15000000.00);

        EventParticipantFacility firstFacility = participantFacilities.get(0);
        EventParticipantFacility secondFacility = participantFacilities.get(1);
        EventParticipantFacility thirdFacility = participantFacilities.get(2);
        EventParticipantFacility fourthFacility = participantFacilities.get(3);

        firstFacility.setCommitmentAmount(commitmentAmount);
        firstFacility.setAllocationAmount(null);

        secondFacility.setCommitmentAmount(commitmentAmount);
        secondFacility.setAllocationAmount(null);

        thirdFacility.setCommitmentAmount(commitmentAmount);
        thirdFacility.setAllocationAmount(null);

        fourthFacility.setCommitmentAmount(commitmentAmount);
        fourthFacility.setAllocationAmount(null);

        eventParticipantFacilityService.update(firstFacility, currentUser);
        eventParticipantFacilityService.update(secondFacility, currentUser);
        eventParticipantFacilityService.update(thirdFacility, currentUser);
        eventParticipantFacilityService.update(fourthFacility, currentUser);

        eventOriginationParticipant = eventOriginationParticipantService.getEventOriginationParticipantById(eventParticipantId);

        BigDecimal totalCommitmentAmount = BigDecimal.valueOf(60000000.00);

        assertThat(eventOriginationParticipant.getTotalCommitmentAmount()).isEqualByComparingTo(totalCommitmentAmount);
        assertThat(eventOriginationParticipant.getTotalAllocationAmount()).isNull();

        BigDecimal allocationAmount = BigDecimal.valueOf(12500000);

        firstFacility.setAllocationAmount(allocationAmount);
        secondFacility.setAllocationAmount(null);
        thirdFacility.setAllocationAmount(allocationAmount);
        fourthFacility.setAllocationAmount(allocationAmount);

        eventParticipantFacilityService.update(firstFacility, currentUser);
        eventParticipantFacilityService.update(secondFacility, currentUser);
        eventParticipantFacilityService.update(thirdFacility, currentUser);
        eventParticipantFacilityService.update(fourthFacility, currentUser);

        eventOriginationParticipant = eventOriginationParticipantService.getEventOriginationParticipantById(eventParticipantId);

        BigDecimal totalAllocationAmount = BigDecimal.valueOf(37500000.00);
        assertThat(eventOriginationParticipant.getTotalAllocationAmount()).isEqualByComparingTo(totalAllocationAmount);
    }

}
