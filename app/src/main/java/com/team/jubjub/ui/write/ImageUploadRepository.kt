package com.team.jubjub.data.repository

import android.net.Uri

interface ImageUploadRepository {
    suspend fun uploadImage(uri: Uri, folder: String = "posts"): Result<String>
}
