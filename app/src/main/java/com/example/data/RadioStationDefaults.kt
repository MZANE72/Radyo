package com.example.data

import com.example.model.RadioStation

object RadioStationDefaults {
    val STATIONS: List<RadioStation> = listOf(
        RadioStation(
            id = "trt_turku",
            frequency = 87.8f,
            name = "TRT Türkü",
            genre = "Halk Müziği",
            location = "Ulusal",
            description = "Anadolu'nun her köşesinden türküler, bozlaklar ve deyişler.",
            currentProgram = "Gönül Bağından Türküler",
            isFavorite = false,
            signalStrengthDbm = -52
        ),
        RadioStation(
            id = "alem_fm",
            frequency = 89.2f,
            name = "Alem FM",
            genre = "Türkçe Pop",
            location = "Ulusal",
            description = "En popüler Türkçe şarkılar ve eğlenceli sabah programları.",
            currentProgram = "Serdar Gökalp ile Alem FM",
            isFavorite = true,
            signalStrengthDbm = -48
        ),
        RadioStation(
            id = "super_fm",
            frequency = 90.8f,
            name = "Süper FM",
            genre = "Hit Pop",
            location = "Ulusal",
            description = "Türkiye'nin ilk özel radyosu, en yeni hit parçalar.",
            currentProgram = "Süper Sabah Şovu",
            isFavorite = false,
            signalStrengthDbm = -55
        ),
        RadioStation(
            id = "trt_fm",
            frequency = 92.4f,
            name = "TRT FM",
            genre = "Genel & Pop",
            location = "Ulusal",
            description = "Türkiye'nin en köklü ve yaygın ulusal radyo kanalı.",
            currentProgram = "Günün İçinden & Hit Melodiler",
            isFavorite = true,
            signalStrengthDbm = -42
        ),
        RadioStation(
            id = "kral_fm",
            frequency = 94.8f,
            name = "Kral FM",
            genre = "Arabesk & Fantezi",
            location = "Ulusal",
            description = "İlaç gibi radyo! Arabesk ve fantezi müziğin efsane adresi.",
            currentProgram = "Afrikalı Ali ile Kral İstekler",
            isFavorite = true,
            signalStrengthDbm = -45
        ),
        RadioStation(
            id = "rad_eksen",
            frequency = 96.2f,
            name = "Radyo Eksen",
            genre = "Rock & Alternatif",
            location = "İstanbul",
            description = "Klasik rock, indie ve alternatif müziğin vazgeçilmez frekansı.",
            currentProgram = "Eksen Rock Kuşağı",
            isFavorite = false,
            signalStrengthDbm = -65
        ),
        RadioStation(
            id = "best_fm",
            frequency = 98.6f,
            name = "Best FM",
            genre = "Hit & Sohbet",
            location = "Ulusal",
            description = "Radyonuz Açık Olsun! Dinamik yayın ve kesintisiz müzik.",
            currentProgram = "Best Hit 40",
            isFavorite = true,
            signalStrengthDbm = -49
        ),
        RadioStation(
            id = "power_fm",
            frequency = 100.0f,
            name = "Power FM",
            genre = "Yabancı Hit",
            location = "Ulusal",
            description = "En popüler dünya hitleri, dans ve elektronik müzik.",
            currentProgram = "Power Morning Show",
            isFavorite = true,
            signalStrengthDbm = -44
        ),
        RadioStation(
            id = "joy_fm",
            frequency = 101.4f,
            name = "Joy FM",
            genre = "Slow & Romantik",
            location = "Ulusal",
            description = "Ruhunuzu dinlendiren romantik şarkılar ve akustik melodiler.",
            currentProgram = "Joy Love Sessions",
            isFavorite = true,
            signalStrengthDbm = -50
        ),
        RadioStation(
            id = "ntv_radyo",
            frequency = 103.0f,
            name = "NTV Radyo",
            genre = "Haber & Güncel",
            location = "Ulusal",
            description = "Dakika dakika son dakika haberleri, analiz ve kültür sanat.",
            currentProgram = "Haber Bülteni & Yol Durumu",
            isFavorite = false,
            signalStrengthDbm = -53
        ),
        RadioStation(
            id = "radyo_d",
            frequency = 104.2f,
            name = "Radyo D",
            genre = "Türkçe Pop",
            location = "Ulusal",
            description = "Sarı Şeker Sema ve en sevilen güncel Türkçe hitler.",
            currentProgram = "Dinamik Pop Listesi",
            isFavorite = false,
            signalStrengthDbm = -58
        ),
        RadioStation(
            id = "show_radyo",
            frequency = 105.4f,
            name = "Show Radyo",
            genre = "Nostalji & Pop",
            location = "Ulusal",
            description = "Türkçe popun efsaneleri, 90'lar ve bugünün şarkıları.",
            currentProgram = "Nostalji Rüzgarı",
            isFavorite = false,
            signalStrengthDbm = -54
        ),
        RadioStation(
            id = "fenomen",
            frequency = 107.0f,
            name = "Radyo Fenomen",
            genre = "Dans & Trend",
            location = "Ulusal",
            description = "Maksimum Hit Müzik! Trend şarkılar ve festival ritimleri.",
            currentProgram = "Fenomen Club Mix",
            isFavorite = true,
            signalStrengthDbm = -47
        )
    )
}
