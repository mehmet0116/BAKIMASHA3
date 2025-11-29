package com.assanhanil.techassist.domain.usecase

import com.assanhanil.techassist.domain.model.Bearing
import com.assanhanil.techassist.domain.model.BearingSearchResult
import com.assanhanil.techassist.domain.repository.BearingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for FindBearingUseCase.
 */
class FindBearingUseCaseTest {

    private lateinit var findBearingUseCase: FindBearingUseCase
    private lateinit var fakeBearingRepository: FakeBearingRepository

    @Before
    fun setup() {
        fakeBearingRepository = FakeBearingRepository()
        findBearingUseCase = FindBearingUseCase(fakeBearingRepository)
    }

    @Test
    fun `invoke with valid dimensions returns found bearings`() = runBlocking {
        // Given
        val testBearing = Bearing(
            id = 1,
            isoCode = "6204-ZZ",
            innerDiameter = 20.0,
            outerDiameter = 47.0,
            width = 14.0,
            type = "Deep Groove Ball Bearing",
            sealType = "ZZ"
        )
        fakeBearingRepository.setBearingsToReturn(listOf(testBearing))

        // When
        val result = findBearingUseCase(
            innerDiameter = 20.0,
            outerDiameter = 47.0,
            width = 14.0,
            tolerance = 0.5
        )

        // Then
        assertTrue(result is BearingSearchResult.Found)
        val foundResult = result as BearingSearchResult.Found
        assertEquals(1, foundResult.bearings.size)
        assertEquals("6204-ZZ", foundResult.bearings[0].isoCode)
    }

    @Test
    fun `invoke with zero inner diameter returns error`() = runBlocking {
        // When
        val result = findBearingUseCase(
            innerDiameter = 0.0,
            outerDiameter = 47.0,
            width = 14.0,
            tolerance = 0.5
        )

        // Then
        assertTrue(result is BearingSearchResult.Error)
        val error = result as BearingSearchResult.Error
        assertTrue(error.exception is IllegalArgumentException)
    }

    @Test
    fun `invoke with negative dimensions returns error`() = runBlocking {
        // When
        val result = findBearingUseCase(
            innerDiameter = -5.0,
            outerDiameter = 47.0,
            width = 14.0,
            tolerance = 0.5
        )

        // Then
        assertTrue(result is BearingSearchResult.Error)
    }

    @Test
    fun `invoke with inner diameter greater than outer diameter returns error`() = runBlocking {
        // When
        val result = findBearingUseCase(
            innerDiameter = 50.0,
            outerDiameter = 47.0,
            width = 14.0,
            tolerance = 0.5
        )

        // Then
        assertTrue(result is BearingSearchResult.Error)
        val error = result as BearingSearchResult.Error
        assertTrue(error.exception.message?.contains("Inner diameter must be less than outer diameter") == true)
    }

    @Test
    fun `invoke with no matching bearings returns not found`() = runBlocking {
        // Given
        fakeBearingRepository.setBearingsToReturn(emptyList())

        // When
        val result = findBearingUseCase(
            innerDiameter = 20.0,
            outerDiameter = 47.0,
            width = 14.0,
            tolerance = 0.5
        )

        // Then
        assertTrue(result is BearingSearchResult.NotFound)
    }

    /**
     * Fake implementation of BearingRepository for testing.
     */
    private class FakeBearingRepository : BearingRepository {
        private var bearingsToReturn: List<Bearing> = emptyList()
        private var shouldThrowException: Exception? = null

        fun setBearingsToReturn(bearings: List<Bearing>) {
            bearingsToReturn = bearings
        }

        fun setShouldThrowException(exception: Exception) {
            shouldThrowException = exception
        }

        override suspend fun findByDimensions(
            innerDiameter: Double,
            outerDiameter: Double,
            width: Double,
            tolerance: Double
        ): BearingSearchResult {
            shouldThrowException?.let { throw it }
            
            return if (bearingsToReturn.isEmpty()) {
                BearingSearchResult.NotFound("No bearings found")
            } else {
                BearingSearchResult.Found(bearingsToReturn)
            }
        }

        override suspend fun findByIsoCode(isoCode: String): Bearing? {
            return bearingsToReturn.find { it.isoCode == isoCode }
        }

        override fun getAllBearings(): Flow<List<Bearing>> {
            return flowOf(bearingsToReturn)
        }

        override suspend fun insertBearing(bearing: Bearing): Long {
            return 1L
        }

        override suspend fun insertBearings(bearings: List<Bearing>) {
            // No-op for testing
        }
    }
}
