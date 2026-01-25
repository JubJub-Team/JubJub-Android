package com.team.jubjub.util

object LabelMapper {
    // TM 사전
    private val tmDictionary = mapOf(
        "airpods" to listOf("에어팟", "무선 이어폰", "이어폰"),
        "galaxybuds" to listOf("버즈", "갤럭시 버즈", "무선 이어폰", "이어폰"),
        "tumbler" to listOf("텀블러", "물통", "컵"),
        "adapter" to listOf("어댑터", "충전기", "충전기 헤드"),
        "cable" to listOf("케이블", "충전선", "충전기"),
        "wallet" to listOf("지갑", "카드 지갑"),
        "card" to listOf("카드", "신용카드", "체크카드", "교통카드", "학생증"),
        "mouse" to listOf("마우스"),
        "powerbank" to listOf("보조배터리", "배터리"),
        "eraser" to listOf("지우개"),
        "umbrella" to listOf("우산")
    )

    // ML Kit용 사전
    private val mlKitDictionary = mapOf(
        "wallet" to listOf("지갑", "카드 지갑"),
        "purse" to listOf("지갑", "파우치"),
        "mobile phone" to listOf("휴대전화", "스마트폰", "핸드폰", "폰"),
        "smartphone" to listOf("휴대전화", "스마트폰", "핸드폰", "폰"),
        "earbuds" to listOf("이어폰", "무선 이어폰"),
        "headphones" to listOf("헤드셋", "헤드폰"),
        "laptop" to listOf("노트북", "컴퓨터"),
        "computer mouse" to listOf("마우스"),
        "mouse" to listOf("마우스"),
        "computer keyboard" to listOf("키보드"),
        "backpack" to listOf("가방", "백팩"),
        "bag" to listOf("가방"),
        "watch" to listOf("시계", "스마트워치"),
        "glasses" to listOf("안경"),
        "sunglasses" to listOf("선글라스"),
        "umbrella" to listOf("우산"),
        "bottle" to listOf("텀블러", "물병"),
        "cup" to listOf("컵", "머그컵")
    )

    fun mapToKorean(englishLabel: String): List<String>? {
        val key = englishLabel.lowercase().trim()
        // TM 사전에서 먼저 검색
        if (tmDictionary.containsKey(key)) return tmDictionary[key]
        // ML Kit 사전에서 검색
        return mlKitDictionary[key]
    }
}