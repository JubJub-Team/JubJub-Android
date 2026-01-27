enum class DialogChoice { LEFT, RIGHT }

sealed class ConfirmDialogSpec(
    val key: String,
    val message: String,
    val leftText: String,
    val rightText: String
) {
    data object Logout : ConfirmDialogSpec("logout",
        "정말로 로그아웃 하시겠습니까?", "예", "아니요"
    )

    data object Withdraw : ConfirmDialogSpec("withdraw",
        "정말로 회원탈퇴 하시겠습니까?", "예", "아니요"
    )

    data object PostType : ConfirmDialogSpec("postType",
        "분실물/나눔 게시글을 선택해주세요.", "분실물", "나눔"
    )

    data object ShareStatus : ConfirmDialogSpec("shareStatus",
        "나눔 상태를 선택해 주세요.", "나눔 완료", "나눔 중"
    )

    data object WriteStatus : ConfirmDialogSpec(
        "writeStatus",
        "작성 게시글을 선택해 주세요.", "분실물", "나눔"
    )

    companion object {
        fun fromKey(key: String): ConfirmDialogSpec = when (key) {
            Logout.key -> Logout
            Withdraw.key -> Withdraw
            PostType.key -> PostType
            ShareStatus.key -> ShareStatus
            WriteStatus.key -> WriteStatus
            else -> Logout
        }
    }
}
