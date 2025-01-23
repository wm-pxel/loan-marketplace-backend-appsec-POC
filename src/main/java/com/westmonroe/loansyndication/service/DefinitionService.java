package com.westmonroe.loansyndication.service;

import com.westmonroe.loansyndication.dao.*;
import com.westmonroe.loansyndication.dao.event.EventTypeDao;
import com.westmonroe.loansyndication.model.*;
import com.westmonroe.loansyndication.model.event.EventType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefinitionService {

    private final ParticipantStepDao participantStepDao;
    private final NaicsCodeDao naicsCodeDao;
    private final RoleDao roleDao;
    private final StageDao stageDao;
    private final StateDao stateDao;
    private final DocumentCategoryDao documentCategoryDao;
    private final DealUserConfidentialityAgreementDao dealUserConfidentialityAgreementDao;
    private final EventTypeDao eventTypeDao;

    public DefinitionService(ParticipantStepDao participantStepDao, NaicsCodeDao naicsCodeDao, RoleDao roleDao
            , StageDao stageDao, StateDao stateDao, DocumentCategoryDao documentCategoryDao
            , DealUserConfidentialityAgreementDao dealUserConfidentialityAgreementDao, EventTypeDao eventTypeDao) {
        this.participantStepDao = participantStepDao;
        this.naicsCodeDao = naicsCodeDao;
        this.roleDao = roleDao;
        this.stageDao = stageDao;
        this.stateDao = stateDao;
        this.documentCategoryDao = documentCategoryDao;
        this.dealUserConfidentialityAgreementDao = dealUserConfidentialityAgreementDao;
        this.eventTypeDao = eventTypeDao;
    }

    public List<ParticipantStep> getParticipantSteps() {
        return participantStepDao.findAll();
    }

    public ParticipantStep getParticipantStepById(Long id) {
        return participantStepDao.findById(id);
    }

    public ParticipantStep getParticipantStepByName(String name) {
        return participantStepDao.findByName(name);
    }

    public ParticipantStep getParticipantStepByOrder(Integer order) {
        return participantStepDao.findByOrder(order);
    }

    public List<NaicsCode> getAllNaicsCodes() {
        return naicsCodeDao.findAll();
    }

    public NaicsCode getNaicsCodeByCode(String code) {
        return naicsCodeDao.findByCode(code);
    }

    public List<NaicsCode> searchNaicsCodesByTitle(String search) {
        return naicsCodeDao.searchByTitle(search);
    }

    public List<Role> getAllRoles() {
        return roleDao.findAll();
    }

    public Role getRoleById(Long id) {
        return roleDao.findById(id);
    }

    public Role saveRole(Role role) {
        return roleDao.save(role);
    }

    public void updateRole(Role role) {
        roleDao.update(role);
    }

    public void deleteRole(Long id) {
        roleDao.delete(id);
    }

    public List<Stage> getStages() {
        return stageDao.findAll();
    }

    public Stage getStageById(Long id) {
        return stageDao.findById(id);
    }

    public Stage getStageByOrder(Integer order) {
        return stageDao.findByOrder(order);
    }

    public List<State> getStates() {
        return stateDao.findAll();
    }

    public State getStateByCode(String code) {
        return stateDao.findByCode(code);
    }

    public List<DocumentCategory> getDocumentCategories() {
        return documentCategoryDao.findAll();
    }

    public DocumentCategory getDocumentCategoryById(Long id) {
        return documentCategoryDao.findById(id);
    }

    public DocumentCategory getDocumentCategoryByName(String name) {
        return documentCategoryDao.findByName(name);
    }

    public ConfidentialityAgreement getConfidentialityAgreementByDealIdAndUserId(Long dealId, Long userId) {
        DealUserConfidentialityAgreement dealUserConfidentialityAgreement = dealUserConfidentialityAgreementDao.findDealUserConfidentialityAgreementByDealIdAndUserId(dealId, userId);

        return dealUserConfidentialityAgreement.getConfidentialityAgreement();
    }

    public boolean agreeToConfidentialityAgreement(Long dealId, User user, Integer confidentialityAgreementId) {
        return dealUserConfidentialityAgreementDao.save(dealId, user, confidentialityAgreementId);
    }

    public List<EventType> getEventTypes() {
        return eventTypeDao.findAll();
    }

    public EventType getEventTypeById(Long id) {
        return eventTypeDao.findById(id);
    }

    public EventType getEventTypeByName(String name) {
        return eventTypeDao.findByName(name);
    }

}