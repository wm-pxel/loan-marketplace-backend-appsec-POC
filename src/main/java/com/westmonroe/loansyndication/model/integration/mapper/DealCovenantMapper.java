package com.westmonroe.loansyndication.model.integration.mapper;

import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.deal.DealCovenant;
import com.westmonroe.loansyndication.model.integration.DealCovenantDto;

public class DealCovenantMapper {

    public DealCovenantMapper() { }

    public DealCovenant dealCovenantDtoToDealCovenant(DealCovenantDto dealCovenantDto, Deal deal) {

        DealCovenant dealCovenant = new DealCovenant();

        dealCovenant.setCovenantExternalId(dealCovenantDto.getCovenantExternalId());
        dealCovenant.setDeal(deal);
        dealCovenant.setEntityName(dealCovenantDto.getEntityName());
        dealCovenant.setCategoryName(dealCovenantDto.getCategoryName());
        dealCovenant.setCovenantType(dealCovenantDto.getCovenantType());
        dealCovenant.setFrequency(dealCovenantDto.getFrequency());
        dealCovenant.setNextEvalDate(dealCovenantDto.getNextEvalDate());
        dealCovenant.setEffectiveDate(dealCovenantDto.getEffectiveDate());

        return dealCovenant;
    }

    public DealCovenantDto dealCovenantToDealCovenantDto(DealCovenant dealCovenant) {

        DealCovenantDto dealCovenantDto = new DealCovenantDto();

        dealCovenantDto.setCovenantExternalId(dealCovenant.getCovenantExternalId());
        dealCovenantDto.setEntityName(dealCovenant.getEntityName());
        dealCovenantDto.setCategoryName(dealCovenant.getCategoryName());
        dealCovenantDto.setCovenantType(dealCovenant.getCovenantType());
        dealCovenantDto.setFrequency(dealCovenant.getFrequency());
        dealCovenantDto.setNextEvalDate(dealCovenant.getNextEvalDate());
        dealCovenantDto.setEffectiveDate(dealCovenant.getEffectiveDate());

        return dealCovenantDto;
    }

}