package com.westmonroe.loansyndication.utils;

import com.westmonroe.loansyndication.model.NaicsCode;
import com.westmonroe.loansyndication.model.PicklistItem;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Slf4j
public class ActivityUtils {

    private ActivityUtils() {
        throw new IllegalStateException("The class cannot be instantiated. It is a utility class.");
    }

    public static <T> String activityValueToString(T value) {

        String result = "";

        if ( value == null ) {
            result = "null";
        } else if ( value instanceof String ) {
            result = value.toString();
        } else if ( value instanceof Long ) {
            result = new DecimalFormat("#,###,##0").format(value);
        } else if ( value instanceof BigDecimal ) {
            result = new DecimalFormat("#,###,##0.00").format(value);
        } else if ( value instanceof Double ) {
            result = new DecimalFormat("#,###,##0.0####").format(value);
        } else if ( value instanceof LocalDate) {
            result = value.toString();
        } else if ( value instanceof OffsetDateTime offsetDateTime ) {
            result = offsetDateTime.toLocalDate().toString();
        } else if ( value instanceof PicklistItem picklistItem ) {
            result = picklistItem.getOption();
        } else if ( value instanceof NaicsCode naicsCode ) {
            result = naicsCode.getCode();
        } else {
            result = value.toString();
        }

        return result;
    }

}