package com.team.jubjub.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.team.jubjub.data.model.Comment
import com.team.jubjub.data.model.Notification
import com.team.jubjub.data.model.Post
import com.team.jubjub.data.model.enums.NotificationType
import com.team.jubjub.data.model.enums.PostType
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val userRepository: UserRepository
) : PostRepository {

    private val postRef = db.collection("posts")

    // 1. [홈] 학교 및 게시물 타입 기준 전체 게시물 목록 조회
    override suspend fun getPostList(
        schoolName: String,
        type: PostType
    ): Result<List<Post>> {
        return try {
            // ❌ orderBy 제거 (Firestore 인덱스 문제 해결)
            val snapshot = postRef
                .whereEqualTo("school", schoolName)
                .whereEqualTo("postType", type.name)
                .get()
                .await()

            // ⭐ 클라이언트에서 정렬
            val list = snapshot
                .toObjects(Post::class.java)
                .sortedByDescending { it.createdAt }

            Result.success(list)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // 2. [홈] 게시물 검색
    override suspend fun searchPosts(
        schoolName: String,
        keyword: String
    ): Result<List<Post>> {
        return try {
            val snapshot = postRef
                .whereEqualTo("school", schoolName)
                .get()
                .await()

            val filteredList = snapshot
                .toObjects(Post::class.java)
                .filter {
                    it.title.contains(keyword, ignoreCase = true) ||
                            it.content.contains(keyword, ignoreCase = true)
                }

            Result.success(filteredList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 3. [마이페이지] 내가 작성한 게시물
    override suspend fun getMyPostList(userId: String): Result<List<Post>> {
        return try {
            val snapshot = postRef
                .whereEqualTo("writerUserId", userId)
                .get()
                .await()

            val list = snapshot
                .toObjects(Post::class.java)
                .sortedByDescending { it.createdAt }

            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // 4. [마이페이지] 스크랩한 게시물
    override suspend fun getScrappedPostList(userId: String): Result<List<Post>> {
        return try {
            val scrapSnapshot = db.collection("users")
                .document(userId)
                .collection("scraps")
                .get()
                .await()

            val scrapIds = scrapSnapshot.documents.map { it.id }
            if (scrapIds.isEmpty()) return Result.success(emptyList())

            val resultList = mutableListOf<Post>()
            val chunks = scrapIds.chunked(10)

            for (chunk in chunks) {
                val postsSnapshot = postRef
                    .whereIn("postId", chunk)
                    .get()
                    .await()
                resultList.addAll(postsSnapshot.toObjects(Post::class.java))
            }

            Result.success(resultList.sortedByDescending { it.createdAt })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 5. 게시물 업로드
    override suspend fun uploadPost(post: Post): Result<Boolean> {
        return try {
            val newDoc = postRef.document()
            val newPost = post.copy(id = newDoc.id)
            newDoc.set(newPost).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 6. 게시물 상세 조회
    override suspend fun getPostDetail(postId: String): Result<Post?> {
        return try {
            val doc = postRef.document(postId).get().await()
            Result.success(doc.toObject(Post::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 7. 게시물 수정
    override suspend fun updatePost(post: Post): Result<Boolean> {
        return try {
            postRef.document(post.id).set(post).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 8. 게시물 삭제
    override suspend fun deletePost(postId: String): Result<Boolean> {
        return try {
            postRef.document(postId).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 9. 게시물 상태 변경
    override suspend fun updatePostStatus(postId: String, status: String): Result<Boolean> {
        return try {
            postRef.document(postId).update("status", status).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 10. 스크랩 토글
    override suspend fun toggleScrap(
        postId: String,
        userId: String,
        isScrap: Boolean
    ): Result<Boolean> {
        return try {
            val scrapRef = db.collection("users")
                .document(userId)
                .collection("scraps")
                .document(postId)

            if (isScrap) {
                scrapRef.set(
                    mapOf(
                        "postId" to postId,
                        "scrappedAt" to java.util.Date()
                    )
                ).await()
            } else {
                scrapRef.delete().await()
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 11. 댓글 목록
    override suspend fun getCommentList(postId: String): Result<List<Comment>> {
        return try {
            val snapshot = postRef.document(postId)
                .collection("comments")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .await()

            Result.success(snapshot.toObjects(Comment::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 12. 댓글 작성 + 알림
    override suspend fun addComment(
        postId: String,
        comment: Comment,
        postWriterId: String
    ): Result<Boolean> {
        return try {
            val commentDoc = postRef.document(postId)
                .collection("comments")
                .document()

            val newComment = comment.copy(commentId = commentDoc.id)
            commentDoc.set(newComment).await()

            if (comment.writerUserId != postWriterId) {
                val notification = Notification(
                    notificationType = NotificationType.COMMENT,
                    notificationMessage = "${comment.writerNickname}님이 게시글에 댓글을 남겼습니다.",
                    targetPostId = postId,
                    isRead = false,
                    createdAt = java.util.Date()
                )
                userRepository.sendNotification(postWriterId, notification)
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 13. 댓글 삭제
    override suspend fun deleteComment(
        postId: String,
        commentId: String
    ): Result<Boolean> {
        return try {
            postRef.document(postId)
                .collection("comments")
                .document(commentId)
                .delete()
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
