package com.unify.api.storage

import com.azure.storage.blob.BlobServiceClientBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class StorageConfig {

    private val log = LoggerFactory.getLogger(StorageConfig::class.java)

    @Bean
    fun imageStorage(
        @Value($$"${azure.storage.connection-string:}") connectionString: String,
        @Value($$"${azure.storage.container}") containerName: String
    ): ImageStorage {
        if (connectionString.isBlank()) {
            log.warn("AZURE_STORAGE_CONNECTION_STRING is not set - image uploads are disabled")
            return UnconfiguredImageStorage()
        }
        val container = BlobServiceClientBuilder()
            .connectionString(connectionString)
            .buildClient()
            .getBlobContainerClient(containerName)
        return AzureBlobImageStorage(container)
    }
}
