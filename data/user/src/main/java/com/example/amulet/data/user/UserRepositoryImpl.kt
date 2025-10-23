package com.example.amulet.data.user

import com.example.amulet.data.user.datasource.local.UserLocalDataSource
import com.example.amulet.data.user.datasource.remote.UserRemoteDataSource
import com.example.amulet.data.user.mapper.UserDtoMapper
import com.example.amulet.data.user.mapper.UserEntityMapper
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.user.model.User
import com.example.amulet.shared.domain.user.model.UserId
import com.example.amulet.shared.domain.user.repository.UserRepository
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.fold
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val remoteDataSource: UserRemoteDataSource,
    private val localDataSource: UserLocalDataSource,
    private val dtoMapper: UserDtoMapper,
    private val entityMapper: UserEntityMapper
) : UserRepository {

    override suspend fun fetchProfile(userId: UserId): AppResult<User> {
        val remoteResult = remoteDataSource.fetchCurrentUser()
        return remoteResult.fold(
            success = { dto ->
                val user = dtoMapper.toDomain(dto)
                localDataSource.upsert(dtoMapper.toEntity(dto))
                Ok(user)
            },
            failure = { error ->
                val cached = localDataSource.findById(userId.value)
                if (cached != null) {
                    Ok(entityMapper.toDomain(cached))
                } else {
                    Err(error)
                }
            }
        )
    }

    override fun observeUser(userId: UserId): Flow<User?> {
        return localDataSource.observeById(userId.value)
            .map { entity -> entity?.let { entityMapper.toDomain(it) } }
    }
}
