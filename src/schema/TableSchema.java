package schema;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public enum TableSchema {

    CI_BANK_MST("CI_BANK_MST", "CI_BANK_LDTL", new String[]{"FNLT_NM"}),
    CI_COA_MST("CI_COA_MST", "CI_COA_LDTL", new String[]{"ACCT_NM"}),
    CI_ITEM("CI_ITEM", "CI_ITEMLANG_SDTL", new String[]{"ITEM_NM", "ITEM_SPEC_DC", "ITEM_DTL_SPEC_DC", "ITEM_MTRQLT_DC"}),
    CI_ITEM_DEF("CI_ITEM_DEF", "CI_ITEM_DEF_SDTL", new String[]{"UNTD_ITEM_STD_NM"}),
    CI_MOVETYPE_MST("CI_MOVETYPE_MST", "CI_MOVETYPE_LDTL", new String[]{"INVTRX_TP_NM"}),
    CI_MNG_DTL("CI_MNG_DTL", "CI_MODULE_SDTL", new String[]{"MODULE_NM"}),
    CI_PAYMENT_MST("CI_PAYMENT_MST", "CI_PAYMENTLANG_DTL", new String[]{"LANG1_NM"}),
    CI_PARTNER("CI_PARTNER", "CI_PARTNER_LDTL", new String[]{"PARTNER_NM"}),
    CI_UOM_INFO("CI_UOM_INFO", "CI_UOMLANG_INFO", new String[]{"UNIT_NM"}),
    CI_PARTNER_MST("CI_PARTNER_MST", "CI_PARTNER_LDTL", new String[]{"PARTNER_NM"}),
    CI_USER_MST("CI_USER_MST", "CI_USER_LDTL", new String[]{"USER_NM"}),
    CI_CODEDTL("CI_CODEDTL", "CI_CODEDTL_SDTL", new String[]{"FIELD_NM", "SYSDEF_NM"}),

    LE_ROUTE_MST("LE_ROUTE_MST", "LE_ROUTE_LDTL", new String[]{"TRNSPROUTE_NM"}),
    LE_SHIPPINGTYPE_MST("LE_SHIPPINGTYPE_MST", "LE_SHIPPINGTYPE_LDTL", new String[]{"TRSP_SHAPE_NM"}),
    LE_TRNSPZONE_MST("LE_TRNSPZONE_MST", "LE_TRNSPZONELANG_SDTL", new String[]{"TRNSPZONE_NM"}),

    MA_BIZAREA_MST("MA_BIZAREA_MST", "MA_BIZAREA_LDTL", new String[]{"BIZAREA_NM"}),
    MA_CC_MST("MA_CC_MST", "MA_CCLANG_DTL", new String[]{"CC_NM"}),
    MA_CODEDTL("MA_CODEDTL", "MA_CODEDTL_SDTL", new String[]{"SYSDEF_NM"}),
    MA_COMPANY("MA_COMPANY", "MA_COMPANY_LDTL", new String[]{"COMPANY_NM"}),
    MA_DEPT_MST("MA_DEPT_MST", "MA_DEPT_LDTL", new String[]{"DEPT_NM"}),
    MA_DOCUCTRL_MST("MA_DOCUCTRL_MST", "MA_DOCUCTRL_LDTL", new String[]{"EXTNO_FG_NM"}),
    MA_MOVETYPE_MST("MA_MOVETYPE_MST", "MA_MOVELANG_INFO", new String[]{"INVTRX_TP_NM"}),
//    MA_NSMETA_CODEDTL("MA_NSMETA_CODEDTL", "MA_NSMETA_CODEDTL_LDTL", new String[]{"INVTRX_TP_NM"}),
    MA_PAYMENT_MST("MA_PAYMENT_MST", "MA_PAYMENTLANG_DTL", new String[]{"LANG1_NM"}),
    MA_PCA_MST("MA_PCA_MST", "MA_PCAL_SDTL", new String[]{"PCA_NM"}),
    MA_PLANT_MST("MA_PLANT_MST", "MA_PLANTLANG_SDTL", new String[]{"PLANT_NM"}),
    MA_SAORG_MST("MA_SAORG_MST", "MA_SAORG_LDTL", new String[]{"SALESORGN_NM"}),
    MA_SL_INFO("MA_SL_INFO", "MA_SLLANG_INFO", new String[]{"SL_NM"}),
    MA_WRK_INFO("MA_WRK_INFO", "MA_WRK_INFO_SDTL", new String[]{"WC_NM"}),
    MA_ELEMENT_MST("MA_ELEMENT_MST", "MA_ELEMENTLANG_DTL", new String[]{"ELEMENT_NM"}),
    MA_ELEMGRP_MST("MA_ELEMGRP_MST", "MA_ELEMGRPL_SDTL", new String[]{"ELEMGRP_NM"}),
    MA_IO_MST("MA_IO_MST", "MA_IOLANG_DTL", new String[]{"IO_NM"}),
    MA_IOGRP_MST("MA_IOGRP_MST", "MA_IOGRPL_SDTL", new String[]{"IO_GRP_NM"}),
    MA_PC_MST("MA_PC_MST", "MA_PC_LDTL", new String[]{"PC_NM"}),
    MA_ACTIVITY_MST("MA_ACTIVITY_MST", "MA_ACTIVITYLANG_DTL", new String[]{"ACTIVITY_NM"}),
    MA_SERVICE_MST("MA_SERVICE_MST", "MA_SERVICE_SDTL", new String[]{"SERVICE_NM"}),
    MA_SERVICECTGRY_MST("MA_SERVICECTGRY_MST", "MA_SERVICECTGRY_SDTL", new String[]{"SERVICE_CTGRY_NM"}),
    MA_SERVICESPEC_MST("MA_SERVICESPEC_MST", "MA_SERVICESPEC_SDTL", new String[]{"SERVICE_SPEC_NM"}),
    MA_ITEMGRP_INFO("MA_ITEMGRP_INFO", "MA_ITEMGRPLANG_DTL", new String[]{"ITEM_GRP_NM"}),
    MA_INVEVALCTGRY_MST("MA_INVEVALCTGRY_MST", "MA_INVEVALCTGRYL_SDTL", new String[]{"INV_EVAL_CTGRY_NM"}),
    MA_COSTCOMP_MST("MA_COSTCOMP_MST", "MA_COSTCOMPLANG_DTL", new String[]{"COSTCOM_NM"}),
    MA_PURGRP_MST("MA_PURGRP_MST", "MA_PURGRPLANG_INFO", new String[]{"PURGRP_NM"}),
    MA_PURORG_MST("MA_PURORG_MST", "MA_PURORGLANG_INFO", new String[]{"PURORG_NM"}),
    MA_COA_DTL("MA_COA_DTL", "MA_COA_LDTL", new String[]{"ACCT_NM"}),
    MA_COA_MST("MA_COA_MST", "MA_COA_LDTL", new String[]{"ACCT_NM"}),
    MA_REPORT_DTL("MA_REPORT_DTL", "MA_REPORTD_LDTL", new String[]{"OBJECT_NM"}),
    MA_COMENU_MST("MA_COMENU_MST", "MA_COMENU_SDTL", new String[]{"MENU_NM"}),

    PU_PURTYPE_MST("PU_PURTYPE_MST", "PU_PURTYPE_LDTL", new String[]{"PO_TP_NM"}),
    PU_REQTYPE_MST("PU_REQTYPE_MST", "PU_REQTYPE_LDTL", new String[]{"PURREQ_TP_NM"}),

    PS_WBS_DTL("PS_WBS_DTL", "PS_WBS_SDTL", new String[]{"WBS_NM"}),
    PS_PROJ_MST("PS_PROJ_MST", "PS_PROJ_SDTL", new String[]{"PJT_NM"}),

    CM_TABLE_INFO("CM_TABLE_INFO", "CM_TABLE_LDTL", new String[]{"TABLE_NM"}),
    CM_COLUMN_INFO("CM_COLUMN_INFO", "CM_COLUMN_LDTL", new String[]{"COL_LANG_NM"}),

    LO_CLSTYP_MST("LO_CLSTYP_MST", "LO_CLSTYP_SDTL", new String[]{"CLASS_TP_NM"}),
    LO_CHAR_MST("LO_CHAR_MST", "LO_CHAR_SDTL", new String[]{"CHRTR_NM", "CHRTR_VAL_NM"}),
    LO_BATCH_INFO("LO_BATCH_INFO", "LO_BATCHLANG_INFO", new String[]{"BATCH_DC", "BATCH_NO"}),
    LO_CLASS_MST("LO_CLASS_MST", "LO_CLASS_SDTL", new String[]{"CLASS_NM"}),
    LO_BATSTD_INFO("LO_BATSTD_INFO", "LO_BATSTDLANG_INFO", new String[]{"BATCH_STD_NM", "BATCH_STD_CD"});

    private final String tableName;
    private final String langTableName;
    private final Set<String> columns;

    // 생성자에서 String 배열을 받아 Set으로 변환
    TableSchema(String tableName, String langTableName, String[] columns) {
        this.tableName = tableName;
        this.langTableName = langTableName;
        // Arrays.asList를 거쳐 HashSet으로 변환하여 불변 Set으로 만듦
        this.columns = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(columns)));
    }

    public String getTableName() {
        return tableName;
    }

    public String getLangTableName() {
        return langTableName;
    }

    public Set<String> getColumns() {
        return columns;
    }

    /**
     * 테이블명으로 Enum 상수를 찾는 역방향 조회 (Java 8 Stream API 사용)
     */
    public static TableSchema fromString(String text) {
        return Arrays.stream(TableSchema.values())
                .filter(t -> t.tableName.equalsIgnoreCase(text))
                .findFirst()
                .orElse(null);
    }
}