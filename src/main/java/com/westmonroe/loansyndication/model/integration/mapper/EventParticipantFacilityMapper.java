package com.westmonroe.loansyndication.model.integration.mapper;

import com.westmonroe.loansyndication.model.event.EventParticipantFacility;
import com.westmonroe.loansyndication.model.integration.EventParticipantFacilityDto;

public class EventParticipantFacilityMapper {

    public EventParticipantFacilityDto eventParticipantFacilityToEventParticipantFacilityDto(EventParticipantFacility participantFacility) {

        EventParticipantFacilityDto eventParticipantFacilityDto = new EventParticipantFacilityDto();

        eventParticipantFacilityDto.setFacilityExternalId(participantFacility.getEventDealFacility().getDealFacility().getFacilityExternalId());
        eventParticipantFacilityDto.setParticipantId(participantFacility.getEventParticipant().getParticipant().getUid());
        eventParticipantFacilityDto.setParticipantName(participantFacility.getEventParticipant().getParticipant().getName());
        eventParticipantFacilityDto.setCommitmentAmount(participantFacility.getCommitmentAmount());
        eventParticipantFacilityDto.setAllocationAmount(participantFacility.getAllocationAmount());
        eventParticipantFacilityDto.setInvitationAmount(participantFacility.getInvitationAmount());

        return eventParticipantFacilityDto;
    }

}