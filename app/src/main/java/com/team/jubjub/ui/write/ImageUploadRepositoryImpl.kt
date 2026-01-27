package com.team.jubjub.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class ImageUploadRepositoryImpl @Inject constructor(
    private val storage: FirebaseStorage
) : ImageUploadRepository {

    override suspend fun uploadImage(uri: Uri, folder: String): Result<String> = runCatching {
        val fileName = "${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child("$folder/$fileName")

        ref.putFile(uri).await()
        ref.downloadUrl.await().toString()
    }.recoverCatching { t ->
        val msg = if (t is StorageException) {
            "StorageException(code=${t.errorCode}): ${t.message}"
        } else {
            t.message ?: t.toString()
        }
        throw IllegalStateException(msg, t)
    }
}
