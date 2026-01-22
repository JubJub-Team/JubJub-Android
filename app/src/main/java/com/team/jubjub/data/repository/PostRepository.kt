package com.team.jubjub.data.repository

import com.team.jubjub.data.model.Post
import com.team.jubjub.data.model.enum.PostType

interface PostRepository {

    // 1. [홈] 학교 및 게시물 타입 기준 전체 게시물 목록 조회
    suspend fun getPostList(
        schoolName: String,
        type: PostType
    ): Result<List<Post>>

    // 2. [홈] 게시물 검색 (제목 및 내용 기준)
    suspend fun searchPosts(
        schoolName: String,
        keyword: String
    ): Result<List<Post>>

    // 3. [마이페이지] 사용자가 작성한 게시물 목록 조회
    suspend fun getMyPostList(
        userId: String
    ): Result<List<Post>>

    // 4. [마이페이지] 사용자가 스크랩한 게시물 목록 조회
    suspend fun getScrappedPostList(
        userId: String
    ): Result<List<Post>>

    // 5. [작성] 게시물 업로드
    suspend fun uploadPost(
        post: Post
    ): Result<Boolean>

    // 6. [상세] 게시물 상세 정보 조회
    suspend fun getPostDetail(
        postId: String
    ): Result<Post?>

    // 7. [수정] 게시물 내용 수정
    suspend fun updatePost(
        post: Post
    ): Result<Boolean>

    // 8. [삭제] 게시물 삭제
    suspend fun deletePost(
        postId: String
    ): Result<Boolean>

    // 9. [상세] 게시물 상태 변경 (예: 거래중, 거래완료)
    suspend fun updatePostStatus(
        postId: String,
        status: String
    ): Result<Boolean>

    // 10. [상세] 게시물 스크랩 상태 변경
    suspend fun toggleScrap(
        postId: String,
        userId: String,
        isScrap: Boolean
    ): Result<Boolean>
}