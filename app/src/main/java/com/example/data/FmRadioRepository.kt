package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.EqualizerPreset
import com.example.model.RadioRecording
import com.example.model.RadioStation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class FmRadioRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("fm_radio_prefs", Context.MODE_PRIVATE)

    private val _stationsFlow = MutableStateFlow<List<RadioStation>>(emptyList())
    val stationsFlow: StateFlow<List<RadioStation>> = _stationsFlow.asStateFlow()

    private val _recordingsFlow = MutableStateFlow<List<RadioRecording>>(emptyList())
    val recordingsFlow: StateFlow<List<RadioRecording>> = _recordingsFlow.asStateFlow()

    private val _presetsFlow = MutableStateFlow<List<Float>>(listOf(89.2f, 92.4f, 94.8f, 98.6f, 100.0f, 101.4f))
    val presetsFlow: StateFlow<List<Float>> = _presetsFlow.asStateFlow()

    init {
        loadStations()
        loadPresets()
        loadRecordings()
    }

    private fun loadStations() {
        val jsonStr = prefs.getString("custom_stations_json", null)
        val list = mutableListOf<RadioStation>()
        // Load default stations
        list.addAll(RadioStationDefaults.STATIONS)

        if (!jsonStr.isNullOrEmpty()) {
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        RadioStation(
                            id = obj.getString("id"),
                            frequency = obj.getDouble("frequency").toFloat(),
                            name = obj.getString("name"),
                            genre = obj.getString("genre"),
                            location = obj.optString("location", "Yerel"),
                            description = obj.optString("description", ""),
                            currentProgram = obj.optString("currentProgram", "Özel Frekans"),
                            isFavorite = obj.optBoolean("isFavorite", false),
                            isCustom = true,
                            signalStrengthDbm = obj.optInt("signalStrengthDbm", -50)
                        )
                    )
                }
            } catch (_: Throwable) {}
        }

        // Apply favorite overrides
        val favIds = prefs.getStringSet("favorite_ids", emptySet()) ?: emptySet()
        val merged = list.map { st ->
            if (favIds.contains(st.id)) st.copy(isFavorite = true) else st
        }.sortedBy { it.frequency }

        _stationsFlow.value = merged
    }

    fun toggleFavorite(stationId: String) {
        val currentFavs = prefs.getStringSet("favorite_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
        if (currentFavs.contains(stationId)) {
            currentFavs.remove(stationId)
        } else {
            currentFavs.add(stationId)
        }
        prefs.edit().putStringSet("favorite_ids", currentFavs).apply()
        loadStations()
    }

    fun addCustomStation(name: String, frequency: Float, genre: String, location: String): RadioStation {
        val newStation = RadioStation(
            id = "custom_${UUID.randomUUID()}",
            frequency = (Math.round(frequency * 10f) / 10f),
            name = name.ifBlank { "FM ${"%.1f".format(frequency)}" },
            genre = genre.ifBlank { "Yerel Radyo" },
            location = location.ifBlank { "Yerel" },
            description = "Kullanıcı tarafından eklenen yerel frekans",
            currentProgram = "Yerel Yayın Akışı",
            isFavorite = true,
            isCustom = true,
            signalStrengthDbm = -40
        )

        val customList = _stationsFlow.value.filter { it.isCustom }.toMutableList()
        customList.add(newStation)
        saveCustomStations(customList)
        loadStations()
        return newStation
    }

    fun removeCustomStation(stationId: String) {
        val customList = _stationsFlow.value.filter { it.isCustom && it.id != stationId }
        saveCustomStations(customList)
        loadStations()
    }

    private fun saveCustomStations(customStations: List<RadioStation>) {
        val array = JSONArray()
        for (st in customStations) {
            val obj = JSONObject().apply {
                put("id", st.id)
                put("frequency", st.frequency.toDouble())
                put("name", st.name)
                put("genre", st.genre)
                put("location", st.location)
                put("description", st.description)
                put("currentProgram", st.currentProgram)
                put("isFavorite", st.isFavorite)
                put("signalStrengthDbm", st.signalStrengthDbm)
            }
            array.put(obj)
        }
        prefs.edit().putString("custom_stations_json", array.toString()).apply()
    }

    private fun loadPresets() {
        val list = mutableListOf<Float>()
        for (i in 0 until 6) {
            val defaultFreq = when (i) {
                0 -> 89.2f
                1 -> 92.4f
                2 -> 94.8f
                3 -> 98.6f
                4 -> 100.0f
                else -> 101.4f
            }
            list.add(prefs.getFloat("preset_$i", defaultFreq))
        }
        _presetsFlow.value = list
    }

    fun savePreset(index: Int, frequency: Float) {
        if (index in 0..5) {
            prefs.edit().putFloat("preset_$index", frequency).apply()
            loadPresets()
        }
    }

    private fun loadRecordings() {
        val jsonStr = prefs.getString("recordings_json", null)
        val list = mutableListOf<RadioRecording>()
        if (!jsonStr.isNullOrEmpty()) {
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val path = obj.getString("filePath")
                    val file = File(path)
                    if (file.exists()) {
                        list.add(
                            RadioRecording(
                                id = obj.getString("id"),
                                title = obj.getString("title"),
                                frequency = obj.getDouble("frequency").toFloat(),
                                stationName = obj.getString("stationName"),
                                filePath = path,
                                durationSeconds = obj.getInt("durationSeconds"),
                                timestamp = obj.getLong("timestamp"),
                                fileSizeBytes = file.length()
                            )
                        )
                    }
                }
            } catch (_: Throwable) {}
        }
        _recordingsFlow.value = list.sortedByDescending { it.timestamp }
    }

    fun addRecording(recording: RadioRecording) {
        val current = _recordingsFlow.value.toMutableList()
        current.add(0, recording)
        saveRecordings(current)
    }

    fun deleteRecording(recordingId: String) {
        val current = _recordingsFlow.value.toMutableList()
        val toRemove = current.find { it.id == recordingId }
        if (toRemove != null) {
            try {
                File(toRemove.filePath).delete()
            } catch (_: Throwable) {}
            current.remove(toRemove)
            saveRecordings(current)
        }
    }

    private fun saveRecordings(list: List<RadioRecording>) {
        val array = JSONArray()
        for (rec in list) {
            val obj = JSONObject().apply {
                put("id", rec.id)
                put("title", rec.title)
                put("frequency", rec.frequency.toDouble())
                put("stationName", rec.stationName)
                put("filePath", rec.filePath)
                put("durationSeconds", rec.durationSeconds)
                put("timestamp", rec.timestamp)
            }
            array.put(obj)
        }
        prefs.edit().putString("recordings_json", array.toString()).apply()
        _recordingsFlow.value = list
    }

    // Settings getters & setters
    fun getLastFrequency(): Float = prefs.getFloat("last_frequency", 92.4f)
    fun setLastFrequency(freq: Float) = prefs.edit().putFloat("last_frequency", freq).apply()

    fun getVolume(): Float = prefs.getFloat("volume", 0.85f)
    fun setVolume(vol: Float) = prefs.edit().putFloat("volume", vol).apply()

    fun isSquelchEnabled(): Boolean = prefs.getBoolean("squelch_enabled", true)
    fun setSquelchEnabled(enabled: Boolean) = prefs.edit().putBoolean("squelch_enabled", enabled).apply()

    fun isAntennaBoostEnabled(): Boolean = prefs.getBoolean("antenna_boost_enabled", true)
    fun setAntennaBoostEnabled(enabled: Boolean) = prefs.edit().putBoolean("antenna_boost_enabled", enabled).apply()
}
