package com.westmonroe.loansyndication.model.integration.mapper;

import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.event.Event;
import com.westmonroe.loansyndication.model.event.EventType;
import com.westmonroe.loansyndication.model.integration.EventDto;
import com.westmonroe.loansyndication.service.DefinitionService;

import java.util.Map;

import static com.westmonroe.loansyndication.utils.DealStageEnum.STAGE_1;
import static com.westmonroe.loansyndication.utils.EventTypeEnum.ORIGINATION;

public class EventMapper {

    private final DefinitionService definitionService;

    public EventMapper(DefinitionService definitionService) {
        this.definitionService = definitionService;
    }

    /**
     * This method transforms an {@link EventDto} object to an {@link Event} object.  If null is passed in for the
     * {@link EventDto} then only the default fields (deal, name, eventType and stage) are set.
     *
     * @param eventDto
     * @param deal
     * @return {@link Event}
     */
    public Event eventDtoToEvent(EventDto eventDto, Deal deal) {

        Event event = new Event();

        event.setDeal(deal);
        event.setName(ORIGINATION.getName());
        event.setEventType(definitionService.getEventTypeById(ORIGINATION.getId()));
        event.setStage(definitionService.getStageByOrder(STAGE_1.getOrder()));

        if ( eventDto != null ) {

            event.setEventExternalId(eventDto.getEventExternalId());
            event.setName(eventDto.getName());
            event.setProjectedLaunchDate(eventDto.getProjectedLaunchDate());
            event.setCommitmentDate(eventDto.getCommitmentDate());
            event.setCommentsDueByDate(eventDto.getCommentsDueByDate());
            event.setEffectiveDate(eventDto.getEffectiveDate());
            event.setProjectedCloseDate(eventDto.getProjectedCloseDate());

        }

        return event;
    }

    public EventDto eventToEventDto(Event event) {

        EventDto eventDto = new EventDto();

        eventDto.setEventExternalId(event.getEventExternalId());
        eventDto.setName(event.getName());
        eventDto.setEventType(event.getEventType().getName());
        eventDto.setProjectedLaunchDate(event.getProjectedLaunchDate());
        eventDto.setLaunchDate(event.getLaunchDate());
        eventDto.setCommitmentDate(event.getCommitmentDate());
        eventDto.setEffectiveDate(event.getEffectiveDate());
        eventDto.setProjectedCloseDate(event.getProjectedCloseDate());
        eventDto.setCloseDate(event.getCloseDate());

        return eventDto;
    }

    public Map<String, Object> eventDtoMapToEventMap(Map<String, Object> eventMap) {

        if ( eventMap.containsKey("eventType") ) {
            EventType eventType = definitionService.getEventTypeByName(eventMap.get("eventType").toString());
            eventMap.put("eventType", Map.of("id", eventType.getId(), "name", eventType.getName()));
        }

        return eventMap;
    }

}