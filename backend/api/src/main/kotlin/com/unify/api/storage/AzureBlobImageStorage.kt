package com.unify.api.storage

import com.azure.core.util.BinaryData
import com.azure.core.util.Context
import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.models.BlobHttpHeaders
import com.azure.storage.blob.options.BlobParallelUploadOptions
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

class AzureBlobImageStorage(private val container: BlobContainerClient) : ImageStorage {

    private val log = LoggerFactory.getLogger(AzureBlobImageStorage::class.java)

    override fun store(folder: String, bytes: ByteArray, type: ImageType): String {
        val blob = container.getBlobClient("$folder/${UUID.randomUUID()}.${type.extension}")

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
        return blob.blobUrl
    }

    override fun delete(url: String) {
        val prefix = container.blobContainerUrl + "/"
        if (!url.startsWith(prefix)) return
        try {
            container.getBlobClient(url.removePrefix(prefix)).deleteIfExists()
        } catch (e: Exception) {
            log.warn("Couldn't delete image $url", e)
        }
    }
}
