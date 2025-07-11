package com.devyouup.shutterflysample.domain.usecase

import com.devyouup.shutterflysample.domain.repository.ImageRepository

class GetSampleImagesUseCase(private val repository: ImageRepository) {
    operator fun invoke() = repository.getSampleImages()
}