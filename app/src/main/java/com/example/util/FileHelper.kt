package com.example.util

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object FileHelper {
    private const val TAG = "FileHelper"

    /**
     * Unpacks a ZIP file from a Uri into a custom folder inside filesDir, and returns
     * sorted absolute paths of any unpacked image files (jpg, jpeg, png, webp).
     */
    fun unpackZipManhwa(context: Context, zipUri: Uri, destinationFolderName: String): List<String> {
        val imagePaths = mutableListOf<File>()
        val rootDir = File(context.filesDir, "manhwas/$destinationFolderName")
        if (!rootDir.exists()) {
            rootDir.mkdirs()
        }

        try {
            context.contentResolver.openInputStream(zipUri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zipStream ->
                    var entry: ZipEntry? = zipStream.nextEntry
                    while (entry != null) {
                        if (!entry.isDirectory) {
                            val name = entry.name
                            if (!name.startsWith(".") && !name.contains("__MACOSX") && isImageFile(name)) {
                                val file = File(rootDir, File(name).name)
                                FileOutputStream(file).use { out ->
                                    zipStream.copyTo(out)
                                }
                                imagePaths.add(file)
                            }
                        }
                        zipStream.closeEntry()
                        entry = zipStream.nextEntry
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unpack ZIP file: ${e.message}", e)
        }

        val sortedFiles = imagePaths.sortedBy { it.name.lowercase() }
        return sortedFiles.map { it.absolutePath }
    }

    /**
     * Copies a list of selected image URIs to a custom folder inside filesDir sequentially
     * and returns their local absolute paths.
     */
    fun copyImagesToStorage(context: Context, uris: List<Uri>, destinationFolderName: String): List<String> {
        val paths = mutableListOf<String>()
        val rootDir = File(context.filesDir, "manhwas/$destinationFolderName")
        if (!rootDir.exists()) {
            rootDir.mkdirs()
        }

        uris.forEachIndexed { index, uri ->
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val extension = getExtensionFromUri(context, uri) ?: "jpg"
                    val filename = String.format("page_%03d.%s", index + 1, extension)
                    val destFile = File(rootDir, filename)
                    FileOutputStream(destFile).use { outStream ->
                        inputStream.copyTo(outStream)
                    }
                    paths.add(destFile.absolutePath)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to copy image URI at index $index: ${e.message}", e)
            }
        }
        return paths
    }

    private fun isImageFile(filename: String): Boolean {
        val ext = valDotExt(filename)
        return listOf("jpg", "jpeg", "png", "webp", "gif").contains(ext)
    }

    private fun valDotExt(filename: String): String {
        val idx = filename.lastIndexOf('.')
        return if (idx != -1) filename.substring(idx + 1).lowercase() else ""
    }

    private fun getExtensionFromUri(context: Context, uri: Uri): String? {
        val mimeType = context.contentResolver.getType(uri)
        return if (mimeType != null) {
            val idx = mimeType.lastIndexOf('/')
            if (idx != -1) mimeType.substring(idx + 1) else null
        } else {
            val filename = uri.lastPathSegment ?: ""
            val ext = valDotExt(filename)
            if (ext.isNotEmpty()) ext else null
        }
    }
}
