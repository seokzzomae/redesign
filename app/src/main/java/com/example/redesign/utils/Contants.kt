package com.example.redesign.utils

object Constants {
    const val TAG : String = "로그"
}

enum class SEARCH_TYPE {
    PHOTO,
    USER
}

enum class RESPONSE_STATE {
    OKAY,
    FAIL
}

object API {
    const val DW_CRTFC_KEY = "c972c649306ac0f8dca6c84d3d147da2bdf3d8c6"
    const val YH_CRTFC_KEY = "1c018f7ca35780ad1acfb854f7bcd08a8191ebc8"
    const val SH_CRTFC_KEY = "c0f14263a3fabda238ca64a9832b6cbb27cd24f6"
    var CRTFC_KEY = SH_CRTFC_KEY

    const val BASE_URL : String = "https://opendart.fss.or.kr/api/"

    const val SEARCH_CORP_CLASS : String = "list.json"
    const val SEARCH_CORP_DATA : String = "fnlttSinglAcnt.json"
}