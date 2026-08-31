package com.tukimtk.farmsync.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class StorageSyncEngine(private val context: Context) {

    suspend fun copySafFileToLocal(sourceUri: Uri, destinationFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val documentFile = DocumentFile.fromSingleUri(context, sourceUri) ?: return@withContext false
            if (!documentFile.exists() || !documentFile.canRead()) return@withContext false

            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            val outputStream: OutputStream = FileOutputStream(destinationFile)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
}
