package com.team.jubjub.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.team.jubjub.data.model.Post
import com.team.jubjub.data.model.enums.PostType
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : PostRepository {

    private val postRef = db.collection("posts")

    // 1. [홈] 학교 및 게시물 타입 기준 전체 게시물 목록 조회
    override suspend fun getPostList(
        schoolName: String,
        type: PostType
    ): Result<List<Post>> {
        return try {
            val snapshot = postRef
                .whereEqualTo("school", schoolName) // 학교 필터링
                .whereEqualTo("postType", type.name)         // 타입 필터링
                .orderBy("createdAt", Query.Direction.DESCENDING) // 최신순 정렬
                .get()
                .await()

            // @DocumentId 덕분에 copy로 ID를 넣을 필요가 없음
            val list = snapshot.toObjects(Post::class.java)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 2. [홈] 게시물 검색 (제목 및 내용 기준)
    override suspend fun searchPosts(
        schoolName: String,
        keyword: String
    ): Result<List<Post>> {
        return try {
            val snapshot = postRef
                .whereEqualTo("school", schoolName)
                .get()
                .await()

            val filteredList = snapshot.toObjects(Post::class.java).filter { post ->
                post.title.contains(keyword, ignoreCase = true) ||
                        post.content.contains(keyword, ignoreCase = true)
            }
            Result.success(filteredList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 3. [마이페이지] 사용자가 작성한 게시물 목록 조회
    override suspend fun getMyPostList(
        userId: String
    ): Result<List<Post>> {
        return try {
            val snapshot = postRef
                .whereEqualTo("writerUserId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val list = snapshot.toObjects(Post::class.java)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 4. [마이페이지] 사용자가 스크랩한 게시물 목록 조회
    override suspend fun getScrappedPostList(
        userId: String
    ): Result<List<Post>> {
        return try {
            // 1단계: ID 목록 가져오기
            val scrapSnapshot = db.collection("users").document(userId)
                .collection("scraps")
                .get()
                .await()

            val scrapIds = scrapSnapshot.documents.map { it.id }

            if (scrapIds.isEmpty()) {
                return Result.success(emptyList())
            }

            // 2단계: 실제 게시물 조회
            val postsSnapshot = postRef
                .whereIn("postId", scrapIds)
                .get()
                .await()

            val list = postsSnapshot.toObjects(Post::class.java)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 5. [작성] 게시물 업로드
    override suspend fun uploadPost(
        post: Post
    ): Result<Boolean> {
        return try {
            val newDoc = postRef.document() // 새 문서 ID 생성

            // ID만 주입 (createdAt은 @ServerTimestamp가 자동 처리)
            val newPost = post.copy(postId = newDoc.id)

            newDoc.set(newPost).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 6. [상세] 게시물 상세 정보 조회
    override suspend fun getPostDetail(
        postId: String
    ): Result<Post?> {
        return try {
            val doc = postRef.document(postId).get().await()
            val post = doc.toObject(Post::class.java)
            Result.success(post)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 7. [수정] 게시물 내용 수정
    override suspend fun updatePost(
        post: Post
    ): Result<Boolean> {
        return try {
            postRef.document(post.postId).set(post).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 8. [삭제] 게시물 삭제
    override suspend fun deletePost(
        postId: String
    ): Result<Boolean> {
        return try {
            postRef.document(postId).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 9. [상세] 게시물 상태 변경
    override suspend fun updatePostStatus(
        postId: String,
        status: String
    ): Result<Boolean> {
        return try {
            postRef.document(postId).update("status", status).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 10. [상세] 게시물 스크랩 상태 변경
    override suspend fun toggleScrap(
        postId: String,
        userId: String,
        isScrap: Boolean
    ): Result<Boolean> {
        return try {
            val scrapRef = db.collection("users").document(userId)
                .collection("scraps").document(postId)

            if (isScrap) {
                val data = mapOf("scrappedAt" to System.currentTimeMillis())
                scrapRef.set(data).await()
            } else {
                scrapRef.delete().await()
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}