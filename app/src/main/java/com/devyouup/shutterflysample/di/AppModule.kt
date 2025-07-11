package com.devyouup.shutterflysample.di

import com.devyouup.shutterflysample.data.repository.ImageRepositoryImpl
import com.devyouup.shutterflysample.domain.repository.ImageRepository
import com.devyouup.shutterflysample.domain.usecase.*
import com.devyouup.shutterflysample.presentation.viewmodel.EditorViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<ImageRepository> { ImageRepositoryImpl() }
    factory { GetSampleImagesUseCase(get()) }
    viewModel {
        EditorViewModel(
            getSampleImagesUseCase = get(),
        )
    }
}