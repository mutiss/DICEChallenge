package com.mutissx.dicechallenge.domain.usecase

import com.mutissx.dicechallenge.core.domain.DataError
import com.mutissx.dicechallenge.domain.model.Artist
import com.mutissx.dicechallenge.domain.repository.ArtistRepository
import com.mutissx.dicechallenge.core.domain.Result

class GetArtistDetailUseCase(
    private val repository: ArtistRepository
) {
    suspend operator fun invoke(mbid: String): Result<Artist, DataError.Network> =
        repository.getArtist(mbid)
}
