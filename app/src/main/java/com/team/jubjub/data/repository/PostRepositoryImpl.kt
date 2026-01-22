package com.team.jubjub.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.team.jubjub.data.model.Post
import com.team.jubjub.data.model.enum.PostType
import kotlinx.coroutines.tasks.await

class PostRepositoryImplementation : PostRepository {

    private val db = FirebaseFirestore.getInstance()
    private val postRef = db.collection("posts")

    // 1. [홈] 학교 및 게시물 타입 기준 전체 게시물 목록 조회
    override suspend fun getPostList(
        school: String,
        type: PostType
    ): Result<List<Post>> {
        return try {
            val snapshot = postRef
                .whereEqualTo("school", school) // 학교 필터링
                .whereEqualTo("postType", type)         // 타입 필터링 (LOST/SHARING)
                .orderBy("createdAt", Query.Direction.DESCENDING) // 최신순 정렬
                .get()
                .await()

            val list = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Post::class.java)?.copy(postId = doc.id)
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 2. [홈] 게시물 검색 (제목 및 내용 기준)
    override suspend fun searchPosts(
        school: String,
        keyword: String
    ): Result<List<Post>> {
        return try {
            // Firestore는 부분 검색(LIKE)이 안 되므로, 학교 데이터만 가져와서 앱에서 필터링
            val snapshot = postRef
                .whereEqualTo("school", school)
                .get()
                .await()

            val filteredList = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Post::class.java)?.copy(postId = doc.id)
            }.filter { post ->
                post.title.contains(keyword) || post.content.contains(keyword)
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
                .whereEqualTo("writerUserId", userId) // 내 ID로 필터링
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val list = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Post::class.java)?.copy(postId = doc.id)
            }
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
            // 1단계: 유저의 스크랩 폴더에서 스크랩한 글의 ID 목록 가져오기
            val scrapSnapshot = db.collection("users").document(userId)
                .collection("scraps")
                .get()
                .await()

            val scrapIds = scrapSnapshot.documents.map { it.id }

            if (scrapIds.isEmpty()) {
                return Result.success(emptyList())
            }

            // 2단계: 가져온 ID들에 해당하는 실제 게시물 데이터 조회
            // (주의: whereIn은 한 번에 최대 10개까지만 조회 가능. 10개 이상일 경우 로직 분리 필요)
            val postsSnapshot = postRef
                .whereIn("postId", scrapIds)
                .get()
                .await()

            val list = postsSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Post::class.java)?.copy(postId = doc.id)
            }
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
            val newPost = post.copy(postId = newDoc.id) // ID 주입

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
            val post = doc.toObject(Post::class.java)?.copy(postId = doc.id)
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
            // 기존 문서 ID에 덮어쓰기
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

    // 9. [상세] 게시물 상태 변경 (예: 거래중, 거래완료)
    override suspend fun updatePostStatus(
        postId: String,
        status: String
    ): Result<Boolean> {
        return try {
            // 전체 덮어쓰기가 아니라 'status' 필드만 업데이트
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
                // 스크랩 추가: 간단한 타임스탬프 정보 저장
                val data = mapOf("scrappedAt" to System.currentTimeMillis())
                scrapRef.set(data).await()
            } else {
                // 스크랩 취소: 문서 삭제
                scrapRef.delete().await()
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}