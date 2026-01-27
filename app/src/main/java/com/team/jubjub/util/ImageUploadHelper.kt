package com.team.jubjub.util

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageUploadHelper @Inject constructor(
    private val storage: FirebaseStorage
) {
    /**
     * @param path : Storage 내 저장 경로 (예: "profiles/user123.jpg")
     * @param uri : 업로드할 파일의 Uri
     * @return Result<String> : 성공 시 다운로드 URL
     */
    suspend fun uploadImage(path: String, uri: Uri): Result<String> {
        return try {
            val storageRef = storage.reference.child(path)

            // 1. 업로드
            storageRef.putFile(uri).await()

            // 2. 다운로드 URL 획득
            val downloadUrl = storageRef.downloadUrl.await()

            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // (옵션) 여러 장 업로드 기능이 필요할 때 사용
    /*
    suspend fun uploadImages(pathPrefix: String, uris: List<Uri>): Result<List<String>> {
        // .. awaitAll() 등을 사용하여 구현
    }
    */
}