package com.westmonroe.loansyndication.service;

import com.westmonroe.loansyndication.dao.InitialLenderDao;
import com.westmonroe.loansyndication.exception.MissingDataException;
import com.westmonroe.loansyndication.model.InitialLender;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class InitialLenderService {

    private final InitialLenderDao initialLenderDao;

    public InitialLenderService(InitialLenderDao initialLenderDao) {
        this.initialLenderDao = initialLenderDao;
    }

    public List<InitialLender> getAllInitialLenders() {
        return initialLenderDao.findAll();
    }

    public List<InitialLender> searchInitialLendersByLender(String lenderName) {
        return initialLenderDao.searchByLenderName(lenderName);
    }

    public InitialLender getInitialLenderById(Long id) {
        return initialLenderDao.findById(id);
    }

    public InitialLender save(InitialLender lender) {

        // Save the initial lender.
        lender = initialLenderDao.save(lender);

        return initialLenderDao.findById(lender.getId());
    }

    public InitialLender update(InitialLender lender) {

        initialLenderDao.update(lender);
        return initialLenderDao.findById(lender.getId());
    }

    /**
     * This method was created to support the GraphQL API.  This method will take a map of values and only update the
     * initial lender fields that were sent.
     *
     * @param  lenderMap
     * @return lender
     */
    public InitialLender update(Map<String, Object> lenderMap) {

        if ( !lenderMap.containsKey("id") ) {
            throw new MissingDataException("The initial lender must contain the id for an update.");
        }

        // Get the initial lender by the id.
        InitialLender lender = initialLenderDao.findById(Long.valueOf(lenderMap.get("id").toString()));

        /*
         * Check the fields in the map and update the initial lender object with the values from the map.
         * This will allow the user to only send the fields they want to update.
         */
        if ( lenderMap.containsKey("lenderName") ) {
            lender.setLenderName((String) lenderMap.get("lenderName"));
        }

        if ( lenderMap.containsKey("active") ) {
            lender.setActive((String) lenderMap.get("active"));
        }

        // Update the initial lender.
        initialLenderDao.update(lender);

        return initialLenderDao.findById(lender.getId());
    }

    /**
     * This method will delete an initial lender.
     *
     * @param   id    The id of the initial lender to delete.
     * @return  The number of rows deleted.
     */
    public int deleteById(Long id) {

        // Delete the institution.
        return initialLenderDao.deleteById(id);
    }

}