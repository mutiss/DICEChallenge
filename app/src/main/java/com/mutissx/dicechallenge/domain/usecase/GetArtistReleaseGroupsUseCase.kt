package com.mutissx.dicechallenge.domain.usecase

import com.mutissx.dicechallenge.core.domain.DataError
import com.mutissx.dicechallenge.domain.model.ReleaseGroup
import com.mutissx.dicechallenge.domain.repository.ArtistRepository
import com.mutissx.dicechallenge.core.domain.Result

class GetArtistReleaseGroupsUseCase(
    private val repository: ArtistRepository
) {
    suspend operator fun invoke(mbid: String): Result<List<ReleaseGroup>, DataError.Network> =
        repository.getReleaseGroups(mbid)
}
