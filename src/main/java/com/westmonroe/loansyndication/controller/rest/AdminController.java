package com.westmonroe.loansyndication.controller.rest;

import com.westmonroe.loansyndication.exception.DatabaseException;
import com.westmonroe.loansyndication.model.RestResponse;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.event.EventParticipant;
import com.westmonroe.loansyndication.service.EmailService;
import com.westmonroe.loansyndication.service.UserService;
import com.westmonroe.loansyndication.service.deal.DealService;
import com.westmonroe.loansyndication.service.event.EventParticipantService;
import com.westmonroe.loansyndication.utils.EmailTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@CrossOrigin
@RequestMapping("/api")
public class AdminController {
    @Value("${lamina.email-service.send-address}")
    private String sendAddress;

    private Flyway flyway;
    private DealService dealService;

    private UserService userService;

    private EventParticipantService eventParticipantService;
    private final EmailService emailService;

    public AdminController (Flyway flyway, DealService dealService, UserService userService
            , EventParticipantService eventParticipantService, EmailService emailService) {
        this.flyway = flyway;
        this.dealService = dealService;
        this.eventParticipantService = eventParticipantService;
        this.emailService = emailService;
        this.userService = userService;
    }

    @Operation(
        hidden = true
    )
    @PostMapping("/admin/database/reset")
//    @PreAuthorize("hasRole('SUPER_ADM')")
    public ResponseEntity<RestResponse> resetDatabase() {
        try {
            flyway.clean();
            flyway.migrate();
        } catch (Exception e) {
            throw new DatabaseException(Arrays.toString(e.getStackTrace()));
        }

        return new ResponseEntity<>(
                new RestResponse("Database Reset"
                        , HttpStatus.OK.value()
                        , Instant.now().toString()
                        , "Database was successfully reset.")
                , HttpStatus.OK);
    }

    @Operation(
        hidden = true
    )
    @GetMapping("/admin/emails/send")
    public ResponseEntity<String> sendEmail(@AuthenticationPrincipal User currentUser) {

        // Get the Peanut Farm deal.
        User janeH = userService.getUserByUid("075ad683-bc65-4edb-9ff7-211d0ea3208a"); // Jane Halverson
        Deal deal = dealService.getDealByUid("a94d5ece-26a6-45e9-81c2-54162cdad5c9", janeH); // Peanut Farming and Processing
        User user = userService.getUserById(28L); // Cole Mitchell as user
        EventParticipant ep = eventParticipantService.getEventParticipantById(12L); // United Farm Credit Services
        ArrayList<Map<String, String>> dealDatesArray = new ArrayList<>();
        dealDatesArray.add(Map.of("dealDateField", "Launch Date", "newDealDate", "06/26/2024"));
        dealDatesArray.add(Map.of("dealDateField", "Close Date", "newDealDate", "06/26/2024"));

        Map<String, Object> templateDataMap = new HashMap<>();
        templateDataMap.put("from", sendAddress);
        templateDataMap.put("dealUid", deal.getUid());
        templateDataMap.put("recipient", user);
        templateDataMap.put("dealName", deal.getName());
        templateDataMap.put("leadInstitution", ep.getParticipant().getName());
        templateDataMap.put("leadInstitutionUid", ep.getParticipant().getUid());
        templateDataMap.put("dealDates", dealDatesArray);
        templateDataMap.put("changedDate", "comments due by date");

        emailService.sendEmail(EmailTypeEnum.DEAL_DATES_UPDATED, deal, templateDataMap);

        return new ResponseEntity<>("Success", HttpStatus.OK);
    }

}