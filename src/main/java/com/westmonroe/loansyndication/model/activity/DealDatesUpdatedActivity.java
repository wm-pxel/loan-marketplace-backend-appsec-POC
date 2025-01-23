package com.westmonroe.loansyndication.model.activity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.westmonroe.loansyndication.exception.ActivityCreationException;
import com.westmonroe.loansyndication.model.event.Event;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import static com.westmonroe.loansyndication.utils.ActivityUtils.activityValueToString;

@Slf4j
public class DealDatesUpdatedActivity implements ActivityFormat {

    @Override
    public String getJson(Map<String, Object> activityMap) {

        Map<String, Object> jsonMap = new HashMap<>();

        // Get the changed event info map, along with the old and new event information from the activity map
        Map<String, Object> eventDateMap = (Map) activityMap.get("eventDateMap");
        Event oldEvent = (Event) activityMap.get("oldEvent");

        if (eventDateMap.containsKey("projectedLaunchDate") &&
                !activityValueToString(oldEvent.getProjectedLaunchDate()).equals(activityValueToString(eventDateMap.get("projectedLaunchDate")))) {
            jsonMap.put("projectedLaunchDate", Map.of(
                    "old", activityValueToString(oldEvent.getProjectedLaunchDate()),
                    "new", activityValueToString(eventDateMap.get("projectedLaunchDate")))
            );
        }

        if (eventDateMap.containsKey("commitmentDate") &&
                !activityValueToString(oldEvent.getCommitmentDate()).equals(activityValueToString(eventDateMap.get("commitmentDate")))) {
            jsonMap.put("commitmentDate", Map.of(
                    "old", activityValueToString(oldEvent.getCommitmentDate()),
                    "new", activityValueToString(eventDateMap.get("commitmentDate")))
            );
        }

        if (eventDateMap.containsKey("projectedCloseDate") &&
                !activityValueToString(oldEvent.getProjectedCloseDate()).equals(activityValueToString(eventDateMap.get("projectedCloseDate")))) {
            jsonMap.put("projectedCloseDate", Map.of(
                    "old", activityValueToString(oldEvent.getProjectedCloseDate()),
                    "new", activityValueToString(eventDateMap.get("projectedCloseDate")))
            );
        }

        if (eventDateMap.containsKey("commentsDueByDate") &&
                !activityValueToString(oldEvent.getCommentsDueByDate()).equals(activityValueToString(eventDateMap.get("commentsDueByDate")))) {
            jsonMap.put("commentsDueByDate", Map.of(
                    "old", activityValueToString(oldEvent.getCommentsDueByDate()),
                    "new", activityValueToString(eventDateMap.get("commentsDueByDate")))
            );
        }

        if (eventDateMap.containsKey("effectiveDate") &&
                !activityValueToString(oldEvent.getEffectiveDate()).equals(activityValueToString(eventDateMap.get("effectiveDate")))) {
            jsonMap.put("effectiveDate", Map.of(
                    "old", activityValueToString(oldEvent.getEffectiveDate()),
                    "new", activityValueToString(eventDateMap.get("effectiveDate")))
            );
        }

        if (eventDateMap.containsKey("launchDate")) {
            jsonMap.put("launchDate", Map.of(
                    "old", activityValueToString(oldEvent.getLaunchDate()),
                    "new", activityValueToString(eventDateMap.get("launchDate"))));
        }

        if (eventDateMap.containsKey("closeDate")) {
            jsonMap.put("closeDate", Map.of(
                    "old", activityValueToString(oldEvent.getCloseDate()),
                    "new", activityValueToString(eventDateMap.get("closeDate"))));
        }

        ObjectMapper objectMapper = new ObjectMapper();
        String json = "";

        try {
            json = objectMapper.writeValueAsString(jsonMap);
        } catch ( JsonProcessingException e ) {
            // Throw exception.
            throw new ActivityCreationException("There was an error creating the activity.");
        }

        return json;
    }

}