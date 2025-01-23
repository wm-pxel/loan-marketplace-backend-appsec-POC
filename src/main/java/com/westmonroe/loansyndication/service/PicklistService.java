package com.westmonroe.loansyndication.service;

import com.westmonroe.loansyndication.dao.PicklistCategoryDao;
import com.westmonroe.loansyndication.dao.PicklistDao;
import com.westmonroe.loansyndication.model.PicklistCategory;
import com.westmonroe.loansyndication.model.PicklistItem;
import com.westmonroe.loansyndication.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PicklistService {

    private final PicklistCategoryDao picklistCategoryDao;
    private final PicklistDao picklistDao;

    public PicklistService(PicklistCategoryDao picklistCategoryDao, PicklistDao picklistDao) {
        this.picklistCategoryDao = picklistCategoryDao;
        this.picklistDao = picklistDao;
    }

    public List<PicklistCategory> getPicklistCategories() {
        return picklistCategoryDao.findAll();
    }

    public PicklistItem getPicklistForCategoryAndOption(String categoryName, String optionName) {

        // If the option is null then return a null picklist item.
        if ( optionName == null ) {
            return null;
        }

        return picklistDao.findByCategoryNameAndOptionName(categoryName, optionName);
    }

    public List<PicklistItem> getPicklistByCategoryId(Long categoryId) {
        return picklistDao.findAllByCategoryId(categoryId);
    }

    public List<PicklistItem> getPicklistByCategoryName(String categoryName, User currentUser) {
        List<PicklistItem> picklistItems = picklistDao.findAllByCategoryName(categoryName);

        // Filter out CoBank Eligibility option for non CoBank institutions.
        if (categoryName.equalsIgnoreCase("Farm Credit Eligibility")
                && !currentUser.getInstitution().getName().equalsIgnoreCase("CoBank")) {
            picklistItems.removeIf(p -> p.getOption().equalsIgnoreCase("CoBank Eligible"));
        }

        return picklistItems;
    }

    public boolean validPicklistIdForCategory(Long itemId, String categoryName) {
        return picklistDao.findCountByIdAndCategoryName(itemId, categoryName) == 1 ? true : false;
    }

    public boolean validPicklistOptionForCategory(String option, String categoryName) {
        return picklistDao.findCountByOptionAndCategoryName(option, categoryName) == 1 ? true : false;
    }

}