package com.team.jubjub.data.repository

import com.team.jubjub.data.model.Comment
import com.team.jubjub.data.model.Post
import com.team.jubjub.data.model.enums.PostType

interface PostRepository {

    /**
     * 1. [홈] 전체 게시물 목록 조회
     * 특정 학교의 분실(LOST) 또는 나눔(SHARING) 게시물을 최신순으로 가져옴
     *
     * @param schoolName : 조회할 학교 이름 (예: "서울여자대학교")
     * @param type : 게시물 타입 (PostType.LOST 또는 PostType.SHARING)
     * @return Result<List<Post>> : 성공 시 게시물 리스트 반환
     */
    suspend fun getPostList(
        schoolName: String,
        type: PostType
    ): Result<List<Post>>

    /**
     * 2. [홈] 게시물 검색
     * 제목이나 본문 내용에 키워드가 포함된 게시물을 검색함
     *
     * @param schoolName : 검색을 수행할 학교 이름
     * @param keyword : 검색어
     * @return Result<List<Post>> : 검색 결과 리스트
     */
    suspend fun searchPosts(
        schoolName: String,
        keyword: String
    ): Result<List<Post>>

    /**
     * 3. [마이페이지] 내가 작성한 게시물 조회
     * 현재 로그인한 사용자가 작성한 글 목록을 가져옴
     *
     * @param userId : 사용자 고유 ID (Writer ID)
     * @return Result<List<Post>> : 작성한 게시물 리스트
     */
    suspend fun getMyPostList(
        userId: String
    ): Result<List<Post>>

    /**
     * 4. [마이페이지] 내가 스크랩한 게시물 조회
     * 사용자가 관심 목록(찜)에 추가한 게시물들을 가져옴
     *
     * @param userId : 사용자 고유 ID
     * @return Result<List<Post>> : 스크랩한 게시물 리스트
     */
    suspend fun getScrappedPostList(
        userId: String
    ): Result<List<Post>>

    /**
     * 5. [작성] 게시물 업로드
     * 새로운 게시물을 Firestore 'posts' 컬렉션에 저장함
     *
     * @param post : 업로드할 게시물 객체 (imageUrl 포함 완료된 상태)
     * @return Result<Boolean> : 업로드 성공 여부
     */
    suspend fun uploadPost(
        post: Post
    ): Result<Boolean>

    /**
     * 6. [상세] 게시물 상세 정보 조회
     * 리스트에서 아이템 클릭 시, 해당 게시물의 전체 정보를 가져옴
     *
     * @param postId : 게시물 고유 ID
     * @return Result<Post?> : 성공 시 Post 객체, 실패하거나 없으면 null
     */
    suspend fun getPostDetail(
        postId: String
    ): Result<Post?>

    /**
     * 7. [수정] 게시물 내용 수정
     * 기존 게시물의 내용을 덮어씌워 수정함
     *
     * @param post : 수정된 내용이 담긴 Post 객체 (postId는 유지)
     * @return Result<Boolean> : 수정 성공 여부
     */
    suspend fun updatePost(
        post: Post
    ): Result<Boolean>

    /**
     * 8. [삭제] 게시물 삭제
     * 해당 게시물을 DB에서 영구 삭제함
     *
     * @param postId : 삭제할 게시물 ID
     * @return Result<Boolean> : 삭제 성공 여부
     */
    suspend fun deletePost(
        postId: String
    ): Result<Boolean>

    /**
     * 9. [상세] 게시물 상태 변경
     * 판매중 -> 거래완료 등으로 상태를 변경함
     *
     * @param postId : 대상 게시물 ID
     * @param status : 변경할 상태 문자열 (예: "TRADING", "COMPLETED")
     * @return Result<Boolean> : 변경 성공 여부
     */
    suspend fun updatePostStatus(
        postId: String,
        status: String
    ): Result<Boolean>

    /**
     * 10. [상세] 게시물 스크랩(찜) 토글
     * 스크랩을 추가하거나 취소함 (트랜잭션 처리 권장)
     *
     * @param postId : 대상 게시물 ID
     * @param userId : 요청하는 사용자 ID
     * @param isScrap : true=스크랩 추가, false=스크랩 취소
     * @return Result<Boolean> : 처리 성공 여부
     */
    suspend fun toggleScrap(
        postId: String,
        userId: String,
        isScrap: Boolean
    ): Result<Boolean>

    /**
     * 11. [상세] 댓글 목록 조회
     * 해당 게시물에 달린 모든 댓글을 작성 순서(오래된순)대로 가져옴
     *
     * @param postId : 게시물 ID
     * @return Result<List<Comment>> : 댓글 리스트
     */
    suspend fun getCommentList(
        postId: String
    ): Result<List<Comment>>

    /**
     * 12. [상세] 댓글 작성 (및 알림 전송)
     * 게시물에 댓글을 저장하고, 게시글의 댓글 수(commentCount)를 증가시킴
     * 작성자가 본인이 아닐 경우 알림을 전송함
     *
     * @param postId : 게시물 ID
     * @param comment : 저장할 댓글 객체
     * @param postWriterId : 게시글 작성자의 UID (알림 수신자)
     * @return Result<Boolean> : 작성 성공 여부
     */
    suspend fun addComment(
        postId: String,
        comment: Comment,
        postWriterId: String
    ): Result<Boolean>

    /**
     * 13. [상세] 댓글 삭제
     * 본인이 작성한 댓글을 삭제하고, 게시글의 댓글 수(commentCount)를 감소시킴
     *
     * @param postId : 게시물 ID (SubCollection 접근용)
     * @param commentId : 삭제할 댓글 ID
     * @return Result<Boolean> : 삭제 성공 여부
     */
    suspend fun deleteComment(
        postId: String,
        commentId: String
    ): Result<Boolean>
}