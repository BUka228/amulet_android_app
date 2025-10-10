package com.example.amulet.data.user.datasource.remote

import com.example.amulet.core.network.NetworkExceptionMapper
import com.example.amulet.core.network.safeApiCall
import com.example.amulet.core.network.service.UsersApiService
import com.example.amulet.core.network.dto.user.UserDto
import com.example.amulet.shared.core.AppResult
import javax.inject.Inject
import javax.inject.Singleton

interface UserRemoteDataSource {
    suspend fun fetchCurrentUser(): AppResult<UserDto>
}

@Singleton
class UserRemoteDataSourceImpl @Inject constructor(
    private val apiService: UsersApiService,
    private val exceptionMapper: NetworkExceptionMapper
) : UserRemoteDataSource {

    override suspend fun fetchCurrentUser(): AppResult<UserDto> =
        safeApiCall(exceptionMapper) { apiService.getCurrentUser().user }
}
