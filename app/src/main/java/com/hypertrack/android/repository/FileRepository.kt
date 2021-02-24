package com.hypertrack.android.repository

import java.io.File

interface FileRepository {

    fun deleteIfExists(path: String)

}

class FileRepositoryImpl: FileRepository {

    override fun deleteIfExists(path: String) {
        File(path).apply { if (exists()) delete() }
    }

}