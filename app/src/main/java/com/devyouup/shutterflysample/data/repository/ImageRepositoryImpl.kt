package com.devyouup.shutterflysample.data.repository

import com.devyouup.shutterflysample.R
import com.devyouup.shutterflysample.domain.repository.ImageRepository

class ImageRepositoryImpl : ImageRepository {
    override fun getSampleImages(): List<Int> {
        return listOf(
            R.drawable.sample_1,
            R.drawable.sample_2,
            R.drawable.sample_3,
            R.drawable.sample_4,
            R.drawable.sample_5,
        )
    }
}