package com.westmonroe.loansyndication.utils;

import java.util.Arrays;
import java.util.List;

public enum DocumentCategoryEnum {

    COLLATERAL("Collateral", true),
    ENTITY_DOCS("Entity Documents", true),
    FINANCIALS("Financials", true),
    OTHER("Other / Misc", true),
    PARTICIPANT_DOCS("Participant Documents", true),
    LOAN_DOCS("Loan Documents", true),
    UNDERWRITING("Underwriting", true),
    COMMIT_LTR("Commitment Letter", false),
    PART_CERT("Participant Certificate", false),
    SIGNED_PART_CERT("Signed Participant Certificate", false),
    PRICING_GRID("Pricing Grid", false);

    private final String name;
    private final Boolean isDealDocument;

    private DocumentCategoryEnum(String name, Boolean isDealDocument) {
        this.name = name;
        this.isDealDocument = isDealDocument;
    }

    public String getName() {
        return this.name;
    }

    public static DocumentCategoryEnum valueOfName(String name) {

        for ( DocumentCategoryEnum dce : values() ) {
            if ( dce.name.equals(name) ) {
                return dce;
            }
        }

        return null;
    }

    /**
     * Based on the document category, calculate the max file size in megabytes.  Currently, only the pricing grid has
     * a different limitation.
     *
     * @return  long    the max file size in MBs.
     */
    public long getMaxFileSize() {
        if ( this.equals(PRICING_GRID) ) {
            return 25;
        } else {
            return 500;
        }
    }

    /**
     * Based on the document category, return the list of supported file types.  Currently, all categories support PNG,
     * JPG and PDF file types. Added inclusion of GIF, Word, PowerPoint and Excel files.
     *
     * @return  List<>    the list of supported file types.
     */
    public List<String> getSupportedFileExtensions() {
        if ( this.equals(PRICING_GRID) ) {
            return Arrays.asList("gif", "jpg", "jpeg", "png");
        } else {
            return Arrays.asList("doc", "docx", "gif", "jpg", "jpeg", "pdf", "png", "ppt", "pptx", "xls", "xlsx");
        }
    }

}