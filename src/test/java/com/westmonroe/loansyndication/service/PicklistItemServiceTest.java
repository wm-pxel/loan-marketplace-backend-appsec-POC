package com.westmonroe.loansyndication.service;

import com.westmonroe.loansyndication.dao.UserDao;
import com.westmonroe.loansyndication.model.PicklistCategory;
import com.westmonroe.loansyndication.model.PicklistItem;
import com.westmonroe.loansyndication.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static com.westmonroe.loansyndication.utils.TestConstants.TEST_USER_UUID_1;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class PicklistItemServiceTest {

    @Autowired
    private PicklistService picklistService;
    @Autowired
    private UserDao userDao;

    @Test
    void givenExistingPicklistCategories_whenGettingAll_thenVerifySize() {

        List<PicklistCategory> categories = picklistService.getPicklistCategories();
        assertThat(categories).hasSize(9);
    }

    @Test
    void givenExistingPicklist_whenGettingPicklistByCategoryId_thenVerifySize() {

        List<PicklistItem> picklist = picklistService.getPicklistByCategoryId(2L);
        assertThat(picklist).hasSize(2);
    }

    @Test
    void givenExistingPicklist_whenGettingPicklistByCategoryName_thenVerifySize() {
        User currentUser = userDao.findByUid(TEST_USER_UUID_1);

        // This is a non CoBank user, and it's expected to have 2 options.
        List<PicklistItem> picklist = picklistService.getPicklistByCategoryName("Farm Credit Eligibility", currentUser);
        assertThat(picklist).hasSize(2);
    }

    @Test
    void givenExistingPicklist_whenValidatingItemAndCategory_thenVerify() {

        // Happy path ... this is true.
        boolean isValid = picklistService.validPicklistIdForCategory(1L,"Farm Credit Eligibility");
        assertThat(isValid).isTrue();

        // Invalid id for category ... this is false.
        isValid = picklistService.validPicklistIdForCategory(4L,"Farm Credit Eligibility");
        assertThat(isValid).isFalse();

        // Invalid category name ... this is false.
        isValid = picklistService.validPicklistIdForCategory(4L,"Test Category Name");
        assertThat(isValid).isFalse();
    }

}