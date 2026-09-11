package com.jepretaja.app.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.jepretaja.app.core.theme.AppColors
import com.jepretaja.app.ui.components.AppTopBar
import com.jepretaja.app.ui.components.PremiumCard
import com.jepretaja.app.ui.state.AuthViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onMyBookings: () -> Unit,
    onNotifications: () -> Unit,
    onFavorites: () -> Unit,
    onHelp: () -> Unit,
    onCreatorBank: () -> Unit,
    onLoggedOut: () -> Unit,
    authViewModel: AuthViewModel,
) {
    val authState by authViewModel.uiState.collectAsState()
    var publicAccount by remember { mutableStateOf(true) }
    var messagePermission by remember { mutableStateOf(true) }
    var activityVisibility by remember { mutableStateOf(true) }
    var twoStep by remember { mutableStateOf(false) }
    var sound by remember { mutableStateOf(true) }
    var vibration by remember { mutableStateOf(true) }
    var followSystemTheme by remember { mutableStateOf(true) }
    var animations by remember { mutableStateOf(true) }
    var showComingSoon by remember { mutableStateOf<String?>(null) }
    var actionMessage by remember { mutableStateOf<String?>(null) }
    var showLogout by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = AppColors.Background,
        topBar = { AppTopBar(title = "Pengaturan", onBack = onBack) },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            PremiumCard(Modifier.fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountCircle, null, tint = AppColors.Primary, modifier = Modifier.size(42.dp))
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text(authState.profile?.name ?: "Pengguna JepretAja", style = MaterialTheme.typography.titleMedium)
                        Text(authState.profile?.email ?: "Akun belum tersambung", style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary)
                    }
                    IconButton(onClick = onEditProfile) { Icon(Icons.Default.Person, "Edit profil") }
                }
            }

            SettingsSection("Akun", Icons.Default.Person) {
                SettingRow(Icons.Default.Person, "Edit profil", "Nama, email, nomor HP", onEditProfile)
                SettingRow(Icons.Default.Lock, "Ubah password", "Kirim tautan ubah password ke email", { authViewModel.resetPassword { actionMessage = it } })
                SettingRow(Icons.Default.Security, "PIN keamanan", "Tambahkan lapisan keamanan", { showComingSoon = "PIN keamanan" })
                SettingRow(Icons.Default.VerifiedUser, "Verifikasi email", "Kirim ulang email verifikasi akun", { authViewModel.resendVerificationEmail { actionMessage = it } })
                SettingRow(Icons.Default.Devices, "Perangkat yang login", "Tinjau sesi aktif", { showComingSoon = "Perangkat yang login" })
                SettingRow(Icons.Default.Logout, "Keluar dari semua perangkat", "Akhiri sesi selain perangkat ini", { showComingSoon = "Keluar dari semua perangkat" })
            }

            SettingsSection("Privasi & Keamanan", Icons.Default.PrivacyTip) {
                SettingToggle("Akun publik", "Orang dapat menemukan profilmu", publicAccount) { publicAccount = it }
                SettingToggle("Siapa yang dapat mengirim pesan", "Izinkan pesan dari pengguna lain", messagePermission) { messagePermission = it }
                SettingToggle("Tampilkan aktivitas", "Bagikan aktivitas terbaru di profil", activityVisibility) { activityVisibility = it }
                SettingRow(Icons.Default.Block, "Pengguna diblokir", "Kelola daftar blokir", { showComingSoon = "Pengguna diblokir" })
                SettingToggle("Verifikasi dua langkah", "Minta verifikasi tambahan saat login", twoStep) { twoStep = it }
            }

            SettingsSection("Notifikasi", Icons.Default.NotificationsNone) {
                listOf("Pesan", "Booking", "Pembayaran", "Pesanan", "Komentar", "Like", "Followers", "Promo", "Notifikasi sistem").forEach { label ->
                    SettingToggle(label, "Notifikasi $label", true) { }
                }
                SettingToggle("Suara notifikasi", "Putar suara saat ada notifikasi", sound) { sound = it }
                SettingToggle("Getar", "Getar saat ada notifikasi", vibration) { vibration = it }
                SettingRow(Icons.Default.NotificationsNone, "Buka pusat notifikasi", "Lihat semua notifikasi", onNotifications)
            }

            SettingsSection("Tampilan", Icons.Default.DarkMode) {
                SettingToggle("Mengikuti sistem", "Gunakan tema perangkat", followSystemTheme) { followSystemTheme = it }
                SettingRow(Icons.Default.DarkMode, "Mode terang / gelap", "Preferensi tampilan aplikasi", { showComingSoon = "Tema aplikasi" })
                SettingRow(Icons.Default.Language, "Ukuran teks", "Sesuaikan keterbacaan", { showComingSoon = "Ukuran teks" })
                SettingToggle("Animasi", "Gunakan transisi dan gerak antarlayar", animations) { animations = it }
            }

            SettingsSection("Booking & Pembayaran", Icons.Default.Payment) {
                SettingRow(Icons.Default.Payment, "Metode pembayaran", "Kelola metode tersimpan", { showComingSoon = "Metode pembayaran" })
                SettingRow(Icons.Default.History, "Riwayat pembayaran", "Invoice dan status transaksi", { showComingSoon = "Riwayat pembayaran" })
                SettingRow(Icons.Default.Sell, "Booking aktif & riwayat booking", "Lihat semua pesanan", onMyBookings)
                SettingRow(Icons.Default.ReportProblem, "Refund", "Ajukan dan lacak refund", { showComingSoon = "Refund" })
                SettingRow(Icons.Default.AccountCircle, "Rekening / e-wallet Creator", "Tujuan pencairan pendapatan", onCreatorBank)
            }

            SettingsSection("Konten", Icons.Default.BookmarkBorder) {
                SettingRow(Icons.Default.SettingsSuggest, "Pengaturan upload", "Kualitas dan preferensi unggah", { showComingSoon = "Pengaturan upload" })
                SettingRow(Icons.Default.PrivacyTip, "Privasi posting", "Atur siapa yang dapat melihat postingan", { showComingSoon = "Privasi posting" })
                SettingRow(Icons.Default.BookmarkBorder, "Konten tersimpan", "Buka koleksi tersimpan", onFavorites)
                SettingRow(Icons.Default.History, "Riwayat aktivitas", "Lihat aktivitas akun", { showComingSoon = "Riwayat aktivitas" })
            }

            SettingsSection("Bantuan", Icons.Default.HelpOutline) {
                SettingRow(Icons.Default.HelpOutline, "Pusat bantuan & FAQ", "Jawaban untuk pertanyaan umum", onHelp)
                SettingRow(Icons.Default.ReportProblem, "Laporkan masalah", "Kirim laporan teknis", { showComingSoon = "Laporkan masalah" })
                SettingRow(Icons.Default.Block, "Laporkan pengguna", "Bantu menjaga komunitas tetap aman", { showComingSoon = "Laporkan pengguna" })
                SettingRow(Icons.Default.SupportAgent, "Hubungi CS", "Dapatkan bantuan dari tim JepretAja", { showComingSoon = "Hubungi CS" })
                SettingRow(Icons.Default.Info, "Syarat & ketentuan", "Aturan penggunaan layanan", { showComingSoon = "Syarat & ketentuan" })
                SettingRow(Icons.Default.PrivacyTip, "Kebijakan privasi", "Cara data digunakan dan dilindungi", { showComingSoon = "Kebijakan privasi" })
            }

            SettingsSection("Tentang", Icons.Default.Info) {
                SettingRow(Icons.Default.Info, "Tentang JepretAja", "Platform foto dan creator lokal", { showComingSoon = "Tentang JepretAja" })
                SettingRow(Icons.Default.VerifiedUser, "Versi aplikasi", "Versi terpasang saat ini", { showComingSoon = "Versi aplikasi" })
                SettingRow(Icons.Default.Info, "Lisensi", "Lisensi open-source yang digunakan", { showComingSoon = "Lisensi" })
                SettingRow(Icons.Default.SettingsSuggest, "Cek pembaruan", "Pastikan aplikasi tetap terbaru", { showComingSoon = "Cek pembaruan" })
            }

            SettingsSection("Akun", Icons.Default.Security) {
                SettingRow(Icons.Default.Security, "Nonaktifkan akun", "Sembunyikan akun sementara", { showComingSoon = "Nonaktifkan akun" })
                SettingRow(Icons.Default.DeleteOutline, "Hapus akun", "Penghapusan permanen dan tidak dapat dibatalkan", { showComingSoon = "Hapus akun" }, destructive = true)
                SettingRow(Icons.Default.Logout, "Logout", "Keluar dari perangkat ini", { showLogout = true }, destructive = true)
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    showComingSoon?.let { title ->
        AlertDialog(
            onDismissRequest = { showComingSoon = null },
            title = { Text(title) },
            text = { Text("Pengaturan ini sudah disiapkan di pusat Pengaturan dan akan tersambung ke layanan akun saat endpoint terkait diaktifkan.") },
            confirmButton = { TextButton(onClick = { showComingSoon = null }) { Text("Mengerti") } },
        )
    }
    actionMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { actionMessage = null },
            title = { Text("JepretAja") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { actionMessage = null }) { Text("Mengerti") } },
        )
    }
    if (showLogout) {
        AlertDialog(
            onDismissRequest = { showLogout = false },
            title = { Text("Logout dari JepretAja?") },
            text = { Text("Sesi di perangkat ini akan diakhiri.") },
            confirmButton = { Button(onClick = { showLogout = false; authViewModel.logout(); onLoggedOut() }) { Text("Logout") } },
            dismissButton = { TextButton(onClick = { showLogout = false }) { Text("Batal") } },
        )
    }
}

@Composable
private fun SettingsSection(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
            Icon(icon, null, tint = AppColors.Primary, modifier = Modifier.size(20.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, color = AppColors.Primary, modifier = Modifier.padding(start = 8.dp))
        }
        PremiumCard(Modifier.fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)) { content() }
    }
}

@Composable
private fun SettingRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit, destructive: Boolean = false) {
    ListItem(
        headlineContent = { Text(title, color = if (destructive) AppColors.Danger else AppColors.TextPrimary) },
        supportingContent = { Text(subtitle, color = AppColors.TextSecondary) },
        leadingContent = { Icon(icon, null, tint = if (destructive) AppColors.Danger else AppColors.Primary) },
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
private fun SettingToggle(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle, color = AppColors.TextSecondary) },
        trailingContent = { Switch(checked = checked, onCheckedChange = onCheckedChange) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}