package com.charanya.app

//        TODO are these the same for ETFs? do we need ot create a map of exchanges/RIC chains to field ids?
/**
 *  See: [https://refinitiv.fixspec.com/stack/specs/elektron] to explore field ID's (FID) if needing to add more
 *  FID Look UP - enter FID Number(FID) or FID Acronym in the Global Search Box located on the top right corner of the screen. (Field -> Refinitiv Elektron (RTMD) -> Standard Definition)
 */

val AUD_FIELD_IDS = mapOf(
    "RDN_EXCHID" to 4L,
    "BID" to 22L,
    "ASK" to 25L,
    "BIDSIZE" to 30L,
    "ASKSIZE" to 31L,
    "QUOTIM" to 1025L,
    "QUOTE_DATE" to 3386L,
    "NETCHNG_1" to 11L,
    "PCTCHNG" to 56L,
    "HST_CLOSE" to 21L,
    "CURRENCY" to 15L,
    "TRDPRC_1" to 6L,
    "TRDTIM_1" to 18L,
    "TRD_STATUS" to 6614L,
    "BCAST_REF" to 728L,
    "PROV_SYMB" to 3422L,
    "HIGH_1" to 12L,
    "LOW_1" to 13L,
    "OPEN_PRC" to 19L,
    "ADJUST_CLS" to 1465L,
    "OFF_CLOSE" to 3372L,
    "ACVOL_1" to 32L,
    "VWAP" to 3404L,
    "MKT_VALUE" to 2150L,
    "YIELD" to 35L,
    "YRHIGH" to 90L,
    "YRLOW" to 91L,
    "INST_PHASE" to 8927L,
    "HSTCLSDATE" to 79L,
    "CUM_EX_MKR" to 117L,
    "PERATIO" to 36L
)
