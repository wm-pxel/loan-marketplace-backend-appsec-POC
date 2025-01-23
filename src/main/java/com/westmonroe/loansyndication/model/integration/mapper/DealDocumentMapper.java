package com.westmonroe.loansyndication.model.integration.mapper;

import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.DealDocument;
import com.westmonroe.loansyndication.model.integration.DealDocumentDto;
import com.westmonroe.loansyndication.model.integration.DocumentBatchDetail;
import com.westmonroe.loansyndication.service.DefinitionService;

public class DealDocumentMapper {

    private final DefinitionService definitionService;

    public DealDocumentMapper(DefinitionService definitionService) {
        this.definitionService = definitionService;
    }

    public DealDocumentDto dealDocumentToDealDocumentDto(DealDocument dealDocument) {

        DealDocumentDto dealDocumentDto = new DealDocumentDto();

        dealDocumentDto.setDocumentExternalId(dealDocument.getDocumentExternalId());
        dealDocumentDto.setDisplayName(dealDocument.getDisplayName());
        dealDocumentDto.setDocumentName(dealDocument.getDocumentName());
        dealDocumentDto.setType(dealDocument.getDocumentType());
        dealDocumentDto.setCategory(dealDocument.getCategory().getName());
        dealDocumentDto.setCreatedById(dealDocument.getCreatedBy().getId());

        return dealDocumentDto;
    }

    public DealDocument dealDocumentDtoToDealDocument(DealDocumentDto dealDocumentDto) {

        DealDocument dealDocument = new DealDocument();

        dealDocument.setDocumentExternalId(dealDocumentDto.getDocumentExternalId());
        dealDocument.setDisplayName(dealDocumentDto.getDisplayName());
        dealDocument.setDocumentName(dealDocumentDto.getDocumentName());
        dealDocument.setDocumentType(dealDocumentDto.getType());
        dealDocument.setCategory(definitionService.getDocumentCategoryByName(dealDocumentDto.getCategory()));
        dealDocument.setCreatedBy(new User(dealDocumentDto.getCreatedById()));

        return dealDocument;
    }

    public DocumentBatchDetail dealDocumentDtoToDocumentBatchDetail(DealDocumentDto dealDocumentDto) {

        DocumentBatchDetail documentBatchDetail = new DocumentBatchDetail();

        documentBatchDetail.setDocumentExternalId(dealDocumentDto.getDocumentExternalId());
        documentBatchDetail.setUrl(dealDocumentDto.getUrl());
        documentBatchDetail.setExtension(dealDocumentDto.getExtension());
        documentBatchDetail.setDisplayName(dealDocumentDto.getDisplayName());
        documentBatchDetail.setCategory(dealDocumentDto.getCategory());

        return documentBatchDetail;
    }

}