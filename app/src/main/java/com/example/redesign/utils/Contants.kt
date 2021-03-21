package com.example.redesign.utils

object Constants {
    // TODO 파일명 Contants로 오타인 것 같은데 맞나요?
    // TODO 잠깐 지나가면서 보니 이렇게 되어있더라구요. 저는 각각의 클래스에 TAG로 클래스 명 선언해뒀는데 전부 "로그" 로 통일시킬까요?
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

    const val BASE_URL : String = "http://teamsemi.iptime.org:5000/"

    const val SEARCH_CORP_CLASS : String = "list.json"
    const val SEARCH_CORP_DATA : String = "fnlttSinglAcnt.json"
}