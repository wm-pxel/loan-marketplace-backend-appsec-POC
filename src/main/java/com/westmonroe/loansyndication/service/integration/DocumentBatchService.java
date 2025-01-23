package com.westmonroe.loansyndication.service.integration;

import com.westmonroe.loansyndication.dao.integration.DocumentBatchDao;
import com.westmonroe.loansyndication.dao.integration.DocumentBatchDetailDao;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.integration.DocumentBatch;
import com.westmonroe.loansyndication.model.integration.DocumentBatchDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class DocumentBatchService {

    private final DocumentBatchDao documentBatchDao;
    private final DocumentBatchDetailDao documentBatchDetailDao;

    public DocumentBatchService(DocumentBatchDao documentBatchDao, DocumentBatchDetailDao documentBatchDetailDao) {
        this.documentBatchDao = documentBatchDao;
        this.documentBatchDetailDao = documentBatchDetailDao;
    }

    public DocumentBatch save(DocumentBatch batch, User currentUser) {

        // Save the batch record.
        documentBatchDao.save(batch, currentUser);

        if ( batch.getDetails() != null ) {

            // Loop through and save the detail records.
            for ( DocumentBatchDetail documentBatchDetail : batch.getDetails() ) {

                // If the external id was not supplied then we should assign one.
                if ( documentBatchDetail.getDocumentExternalId() == null ) {
                    documentBatchDetail.setDocumentExternalId(UUID.randomUUID().toString());
                }

                documentBatchDetail.setDocumentBatchId(batch.getId());
                documentBatchDetailDao.save(documentBatchDetail, currentUser);

            }

        }

        return getDocumentBatchForId(batch.getId());
    }

    public void updateDocumentBatchProcessStartDate(Long batchId) {
        documentBatchDao.updateProcessStartDate(batchId);
    }

    public void updateDocumentBatchProcessEndDate(Long batchId) {

        List<DocumentBatchDetail> details = documentBatchDetailDao.findAllByDocumentBatchId(batchId);

        // Get number of detail documents that weren't processed.
        long incompleteCount = details.parallelStream().filter(d -> d.getProcessEndDate() == null).count();

        // Update the batch end date if all of the files were processed.
        if ( incompleteCount == 0 ) {
            documentBatchDao.updateProcessEndDate(batchId);
        }
    }

    public void updateDocumentBatchDetailProcessStartDate(Long batchId, Long detailId) {
        documentBatchDetailDao.updateProcessStartDate(batchId, detailId);
    }

    public void updateDocumentBatchDetailProcessEndDate(Long batchId, Long detailId) {
        documentBatchDetailDao.updateProcessEndDate(batchId, detailId);
    }

    public DocumentBatch getDocumentBatchForId(Long documentBatchId) {

        // Get the document batch record.
        DocumentBatch batch = documentBatchDao.findById(documentBatchId);

        List<DocumentBatchDetail> details = documentBatchDetailDao.findAllByDocumentBatchId(documentBatchId);

        // Assign
        batch.setDetails(details);

        return batch;
    }

}