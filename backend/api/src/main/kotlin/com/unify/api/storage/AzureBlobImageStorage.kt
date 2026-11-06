package com.unify.api.storage

import com.azure.core.util.BinaryData
import com.azure.core.util.Context
import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.models.BlobHttpHeaders
import com.azure.storage.blob.models.ListBlobsOptions
import com.azure.storage.blob.options.BlobParallelUploadOptions
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

class AzureBlobImageStorage(private val container: BlobContainerClient) : ImageStorage {

    private val log = LoggerFactory.getLogger(AzureBlobImageStorage::class.java)

    override fun store(folder: String, bytes: ByteArray, type: ImageType): String {
        val name = "$folder/${UUID.randomUUID()}.${type.extension}"
        val blob = container.getBlobClient(name)

        val headers = BlobHttpHeaders()
            .setContentType(type.contentType)
            .setCacheControl("public, max-age=31536000, immutable")

        try {
            blob.uploadWithResponse(
                BlobParallelUploadOptions(BinaryData.fromBytes(bytes)).setHeaders(headers),
                null,
                Context.NONE
            )
        } catch (e: Exception) {
            log.error("Azure upload failed", e)
            throw ResponseStatusException(HttpStatus.BAD_GATEWAY, "Couldn't upload the image, please try again")
        }
        return urlFor(container.blobContainerUrl, name)
    }

    override fun delete(url: String) {
        val name = nameFrom(container.blobContainerUrl, url) ?: return
        try {
            container.getBlobClient(name).deleteIfExists()
        } catch (e: Exception) {
            log.warn("Couldn't delete image $url", e)
        }
    }

    override fun deleteFolderExcept(folder: String, keepUrl: String) {
        val keepName = nameFrom(container.blobContainerUrl, keepUrl)
        try {
            container.listBlobs(ListBlobsOptions().setPrefix("$folder/"), null)
                .map { it.name }
                .filter { it != keepName }
                .forEach { container.getBlobClient(it).deleteIfExists() }
        } catch (e: Exception) {
            log.warn("Couldn't clean up $folder", e)
        }
    }

    companion object {
        fun urlFor(containerUrl: String, blobName: String): String = "$containerUrl/$blobName"

        fun nameFrom(containerUrl: String, url: String): String? {
            val prefix = "$containerUrl/"
            return if (url.startsWith(prefix)) url.removePrefix(prefix) else null
        }
    }
}
