package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.FmHardwareDetector
import com.example.data.FmRadioRepository
import com.example.data.RadioStationDefaults
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("FM Radyo", appName)
    }

    @Test
    fun `default stations are loaded correctly`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val repo = FmRadioRepository(app)
        val stations = repo.stationsFlow.value

        assertTrue(stations.isNotEmpty())
        assertTrue(stations.any { it.name == "TRT FM" })
        assertTrue(stations.any { it.name == "Power FM" })
        assertTrue(stations.any { it.name == "Kral FM" })
    }

    @Test
    fun `add custom station persists and appears in station list`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val repo = FmRadioRepository(app)

        val newStation = repo.addCustomStation("Radyo Ege", 95.5f, "Yerel Pop", "İzmir")
        assertNotNull(newStation)
        assertEquals(95.5f, newStation.frequency, 0.01f)

        val updatedStations = repo.stationsFlow.value
        assertTrue(updatedStations.any { it.name == "Radyo Ege" && it.isCustom })
    }

    @Test
    fun `presets are saved and restored properly`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val repo = FmRadioRepository(app)

        repo.savePreset(0, 107.0f)
        val presets = repo.presetsFlow.value
        assertEquals(107.0f, presets[0], 0.01f)
    }

    @Test
    fun `hardware detector returns valid report without crashing`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val detector = FmHardwareDetector(app)
        val report = detector.detect()

        assertNotNull(report)
        assertNotNull(report.currentAudioRoute)
        assertNotNull(report.diagnosticSummary)
    }
}
