package com.westmonroe.loansyndication.service.event;

import com.westmonroe.loansyndication.dao.deal.DealDao;
import com.westmonroe.loansyndication.dao.deal.DealFacilityDao;
import com.westmonroe.loansyndication.dao.event.EventDealFacilityDao;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.DealFacility;
import com.westmonroe.loansyndication.model.event.Event;
import com.westmonroe.loansyndication.model.event.EventDealFacility;
import com.westmonroe.loansyndication.service.ActivityService;
import com.westmonroe.loansyndication.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class EventDealFacilityService {

    private final EventDealFacilityDao eventDealFacilityDao;
    private final DealFacilityDao dealFacilityDao;
    private final DealDao dealDao;
    private final ActivityService activityService;
    private final EmailService emailService;

    public EventDealFacilityService(EventDealFacilityDao eventDealFacilityDao, DealFacilityDao dealFacilityDao
            , DealDao dealDao, ActivityService activityService, EmailService emailService) {
        this.eventDealFacilityDao = eventDealFacilityDao;
        this.dealFacilityDao = dealFacilityDao;
        this.dealDao = dealDao;
        this.activityService = activityService;
        this.emailService = emailService;
    }

    public EventDealFacility getEventDealFacilityForId(Long id) {
        return eventDealFacilityDao.findById(id);
    }

    public List<EventDealFacility> getEventDealFacilitiesForEvent(Long eventId) {
        return eventDealFacilityDao.findAllByEventId(eventId);
    }

    public List<EventDealFacility> getEventDealFacilitiesForEvent(String eventUid) {
        return eventDealFacilityDao.findAllByEventUid(eventUid);
    }

    public EventDealFacility save(EventDealFacility eventDealFacility, User currentUser) {

        // Set the created by user to the event deal facility record.
        eventDealFacility.setCreatedBy(currentUser);

        // Save the event deal facility.
        return eventDealFacilityDao.save(eventDealFacility);
    }

    public void createEventDealFacilitiesForEvent(Event event, User currentUser) {

        // Get all of the deal facilities.
        List<DealFacility> dealFacilities = dealFacilityDao.findAllByDealId(event.getDeal().getId());

        // Loop through all of the deal facilities and create an event deal facility record for each.
        for ( DealFacility dealFacility : dealFacilities ) {
            save(new EventDealFacility(event, dealFacility), currentUser);
        }

    }

    public int deleteById(Long id) {
        return eventDealFacilityDao.deleteById(id);
    }

    public int deleteAllByEventId(Long eventId) {
        return eventDealFacilityDao.deleteAllByEventId(eventId);
    }

    public int deleteByFacilityExternalId(String facilityExternalId) {
        return eventDealFacilityDao.deleteAllByFacilityExternalId(facilityExternalId);
    }

}