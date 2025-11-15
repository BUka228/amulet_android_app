package com.example.amulet.data.courses.di

import com.example.amulet.data.courses.CoursesRepositoryImpl
import com.example.amulet.data.courses.datasource.LocalCoursesDataSource
import com.example.amulet.data.courses.datasource.LocalCoursesDataSourceImpl
import com.example.amulet.data.courses.datasource.RemoteCoursesDataSource
import com.example.amulet.data.courses.datasource.RemoteCoursesDataSourceStub
import com.example.amulet.shared.domain.courses.CoursesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface CoursesDataModule {

    @Binds
    @Singleton
    fun bindCoursesRepository(impl: CoursesRepositoryImpl): CoursesRepository

    @Binds
    @Singleton
    fun bindLocalCoursesDataSource(impl: LocalCoursesDataSourceImpl): LocalCoursesDataSource

    @Binds
    @Singleton
    fun bindRemoteCoursesDataSource(impl: RemoteCoursesDataSourceStub): RemoteCoursesDataSource
}
