package com.jepretaja.app.ui.screens.profile

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PersonAddAlt
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jepretaja.app.core.theme.AppColors
import com.jepretaja.app.core.theme.AppSpacing
import com.jepretaja.app.data.model.BookingModel
import com.jepretaja.app.data.model.ExplorePostModel
import com.jepretaja.app.ui.components.AppAvatar
import com.jepretaja.app.ui.components.EmptyState
import com.jepretaja.app.ui.components.PremiumCard
import com.jepretaja.app.ui.components.QrCode
import com.jepretaja.app.ui.components.SkeletonPostTile
import com.jepretaja.app.ui.components.StatusBadge
import com.jepretaja.app.ui.state.AuthViewModel
import kotlinx.coroutines.flow.flowOf

private enum class ProfileCollection(val label: String) {
    POSTS("Karya"), LIKED("Disukai"), SAVED("Tersimpan"), BOOKINGS("Booking"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernProfileScreen(
    onLogin: () -> Unit,
    onMyBookings: () -> Unit,
    onFavorites: () -> Unit,
    onMyReviews: () -> Unit,
    onReports: () -> Unit,
    onNotifications: () -> Unit,
    onHelp: () -> Unit,
    onChat: () -> Unit,
    onCreatorStudio: () -> Unit,
    onInterests: () -> Unit,
    onWatchHistory: () -> Unit,
    onFollowList: (Int) -> Unit,
    onMyWorks: () -> Unit,
    onSaved: () -> Unit,
    onLoggedOut: () -> Unit,
    onEditProfile: () -> Unit,
    onPostClick: (String) -> Unit,
    onCreateWork: () -> Unit,
    authViewModel: AuthViewModel,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val authState by authViewModel.uiState.collectAsState()
    val uid = authState.uid
    val creator by remember(uid, authState.isCreator) {
        if (uid != null && authState.isCreator) viewModel.creator(uid) else flowOf(null)
    }.collectAsState(initial = null)
    val posts by remember(uid, authState.isCreator) {
        if (uid != null && authState.isCreator) viewModel.myPosts(uid) else flowOf(emptyList())
    }.collectAsState(initial = emptyList())
    val saved by remember(uid) { if (uid != null) viewModel.savedPosts(uid) else flowOf(emptyList()) }
        .collectAsState(initial = emptyList())
    val liked by remember(uid) { if (uid != null) viewModel.likedPosts(uid) else flowOf(emptyList()) }
        .collectAsState(initial = emptyList())
    val bookings by remember(uid) {
        if (uid != null && !authState.isCreator) viewModel.myBookings(uid) else flowOf(emptyList())
    }.collectAsState(initial = emptyList())
    val followingCount by remember(uid) {
        if (uid != null) viewModel.followingCount(uid) else flowOf(0)
    }.collectAsState(initial = 0)

    var selected by rememberSaveable { mutableStateOf(ProfileCollection.POSTS) }
    var showSettings by remember { mutableStateOf(false) }
    var showShare by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(containerColor = AppColors.Background) { padding ->
        if (!authState.isLoggedIn) {
            GuestProfileModern(modifier = Modifier.padding(padding), onLogin = onLogin)
            return@Scaffold
        }

        val name = authState.profile?.name?.takeIf { it.isNotBlank() } ?: "Pengguna JepretAja"
        val username = "@" + name.lowercase().replace("[^a-z0-9]".toRegex(), "").take(20).ifBlank { "pengguna" }
        val collection = when (selected) {
            ProfileCollection.POSTS -> posts
            ProfileCollection.LIKED -> liked
            ProfileCollection.SAVED -> saved
            ProfileCollection.BOOKINGS -> emptyList()
        }
        val likes = if (authState.isCreator) posts.sumOf { it.likeCount } else liked.size.toLong()
        val followerCount = creator?.followerCount ?: 0
        val bookingCount = if (authState.isCreator) creator?.totalBookings ?: 0 else bookings.size

        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(onClick = { showShare = true }) {
                    Icon(Icons.Default.Share, contentDescription = "Bagikan profil", tint = AppColors.TextPrimary)
                }
                IconButton(onClick = { showSettings = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Pengaturan", tint = AppColors.TextPrimary)
                }
            }

            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                AppAvatar(url = authState.profile?.photoUrl, name = name, size = 84.dp, verified = creator?.verified == true)
                Spacer(Modifier.height(AppSpacing.sm))
                Text(name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.W700, color = AppColors.TextPrimary)
                Text(username, style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary)
                creator?.bio?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(AppSpacing.sm))
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = AppSpacing.xxl))
                }
                Spacer(Modifier.height(AppSpacing.md))
                Row(Modifier.padding(horizontal = AppSpacing.xl), horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    OutlinedButton(onClick = onEditProfile, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f).height(42.dp)) {
                        Text("Edit Profil", style = MaterialTheme.typography.labelLarge)
                    }
                    if (authState.isCreator) {
                        Button(onClick = onCreateWork, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f).height(42.dp)) {
                            Text("Buat Karya", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            Spacer(Modifier.height(AppSpacing.lg))
            Row(Modifier.fillMaxWidth().padding(horizontal = AppSpacing.sm), horizontalArrangement = Arrangement.SpaceEvenly) {
                ProfileMetric("$followingCount", "Mengikuti")
                ProfileMetric("$followerCount", "Pengikut")
                ProfileMetric("$likes", "Suka")
                ProfileMetric("${saved.size}", "Tersimpan")
                ProfileMetric("$bookingCount", "Booking")
            }

            Spacer(Modifier.height(AppSpacing.lg))
            ScrollableTabRow(selectedTabIndex = selected.ordinal, edgePadding = AppSpacing.sm, containerColor = Color.Transparent, divider = { HorizontalDivider(color = AppColors.Border) }) {
                ProfileCollection.entries.forEach { tab ->
                    Tab(selected = selected == tab, onClick = { selected = tab }, text = { Text(tab.label, style = MaterialTheme.typography.labelLarge) }, icon = { Icon(if (tab == ProfileCollection.POSTS) Icons.Default.GridView else if (tab == ProfileCollection.SAVED) Icons.Default.BookmarkBorder else if (tab == ProfileCollection.LIKED) Icons.Default.FavoriteBorder else Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp)) })
                }
            }

            Spacer(Modifier.height(AppSpacing.md))
            if (selected == ProfileCollection.BOOKINGS) {
                BookingCollection(bookings = bookings, onOpen = onMyBookings)
            } else if (collection.isEmpty()) {
                EmptyState(
                    icon = if (selected == ProfileCollection.SAVED) Icons.Default.BookmarkBorder else if (selected == ProfileCollection.LIKED) Icons.Default.FavoriteBorder else Icons.Default.GridView,
                    title = if (selected == ProfileCollection.POSTS) "Belum ada karya" else "Belum ada konten",
                    description = if (selected == ProfileCollection.POSTS) "Mulai bagikan karya pertama kamu di JepretAja." else "Konten yang kamu pilih akan muncul di sini.",
                    actionLabel = if (selected == ProfileCollection.POSTS && authState.isCreator) "+ Buat Karya" else null,
                    onAction = if (selected == ProfileCollection.POSTS && authState.isCreator) onCreateWork else null,
                )
            } else {
                PostGrid(posts = collection, onPostClick = onPostClick)
            }
            Spacer(Modifier.height(AppSpacing.xxxl))
        }

        if (showSettings) {
            ModalBottomSheet(onDismissRequest = { showSettings = false }) {
                Column(Modifier.padding(horizontal = AppSpacing.xl).padding(bottom = AppSpacing.xxxl)) {
                    Text("Pengaturan", style = MaterialTheme.typography.headlineSmall, color = AppColors.TextPrimary)
                    Spacer(Modifier.height(AppSpacing.md))
                    Text("Aktivitas", style = MaterialTheme.typography.labelLarge, color = AppColors.Primary)
                    SettingsAction(Icons.Default.CalendarMonth, "Booking saya", onMyBookings)
                    SettingsAction(Icons.AutoMirrored.Filled.Chat, "Chat", onChat)
                    SettingsAction(Icons.Default.NotificationsNone, "Notifikasi", onNotifications)
                    SettingsAction(Icons.Default.RateReview, "Review saya", onMyReviews)
                    SettingsAction(Icons.Default.ReportProblem, "Laporan saya", onReports)
                    Spacer(Modifier.height(AppSpacing.md))
                    Text("Koleksi & sosial", style = MaterialTheme.typography.labelLarge, color = AppColors.Primary)
                    SettingsAction(Icons.Default.Favorite, "Favorit creator", onFavorites)
                    SettingsAction(Icons.Default.BookmarkBorder, "Karya tersimpan", onSaved)
                    SettingsAction(Icons.Default.GridView, "Karya saya", onMyWorks)
                    SettingsAction(Icons.Default.PersonAddAlt, "Mengikuti", { onFollowList(0) })
                    SettingsAction(Icons.Default.People, "Pengikut", { onFollowList(1) })
                    Spacer(Modifier.height(AppSpacing.md))
                    Text("Preferensi", style = MaterialTheme.typography.labelLarge, color = AppColors.Primary)
                    SettingsAction(Icons.Default.PersonAddAlt, "Minat dan preferensi", onInterests)
                    SettingsAction(Icons.Default.HelpOutline, "Bantuan", onHelp)
                    SettingsAction(Icons.Default.History, "Riwayat tontonan", onWatchHistory)
                    if (authState.isCreator) {
                        Spacer(Modifier.height(AppSpacing.md))
                        Text("Creator", style = MaterialTheme.typography.labelLarge, color = AppColors.Primary)
                        SettingsAction(Icons.Default.CameraAlt, "Creator Studio", onCreatorStudio)
                        SettingsAction(Icons.Default.GridView, "Kelola karya", onMyWorks)
                    }
                    Spacer(Modifier.height(AppSpacing.md))
                    TextButton(onClick = onLoggedOut, modifier = Modifier.fillMaxWidth()) {
                        Text("Keluar dari akun", color = AppColors.Danger)
                    }
                }
            }
        }

        if (showShare) {
            val link = if (authState.isCreator && uid != null) "https://jepretaja.app/creator/$uid" else "https://jepretaja.app"
            AlertDialog(
                onDismissRequest = { showShare = false },
                title = { Text("Bagikan profil") },
                text = { Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) { QrCode(content = link, modifier = Modifier.size(180.dp)); Spacer(Modifier.height(AppSpacing.sm)); Text(link, style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary, textAlign = TextAlign.Center) } },
                confirmButton = { TextButton(onClick = { val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "Lihat profilku di JepretAja: $link") }; runCatching { context.startActivity(Intent.createChooser(send, "Bagikan lewat")) }; showShare = false }) { Text("Bagikan") } },
                dismissButton = { TextButton(onClick = { showShare = false }) { Text("Tutup") } },
            )
        }
    }
}

@Composable
private fun RowScope.ProfileMetric(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.widthIn(min = 52.dp).weight(1f)) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.W700, color = AppColors.TextPrimary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = AppColors.TextSecondary, maxLines = 1)
    }
}

@Composable
private fun GuestProfileModern(modifier: Modifier, onLogin: () -> Unit) {
    Column(modifier.fillMaxSize().padding(AppSpacing.xxl), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        AppAvatar(url = null, name = "?", size = 76.dp)
        Spacer(Modifier.height(AppSpacing.lg))
        Text("Masuk untuk membuka profil", style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary)
        Spacer(Modifier.height(AppSpacing.sm))
        Text("Simpan karya, ikuti creator, dan lihat aktivitas booking kamu.", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(AppSpacing.lg))
        Button(onClick = onLogin, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("Masuk") }
    }
}

@Composable
private fun PostGrid(posts: List<ExplorePostModel>, onPostClick: (String) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        posts.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                row.forEach { post ->
                    Box(Modifier.weight(1f).aspectRatio(0.82f).clip(RoundedCornerShape(4.dp)).background(AppColors.SurfaceVariant).clickable { onPostClick(post.postId) }) {
                        AsyncImage(model = post.thumbnailUrl ?: post.mediaUrls.firstOrNull(), contentDescription = post.caption, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        Row(Modifier.align(Alignment.BottomStart).padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(3.dp))
                            Text("${post.likeCount}", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(2.dp))
        }
    }
}

@Composable
private fun BookingCollection(bookings: List<BookingModel>, onOpen: () -> Unit) {
    if (bookings.isEmpty()) {
        EmptyState(icon = Icons.Default.CalendarMonth, title = "Belum ada booking", description = "Riwayat booking kamu akan muncul di sini.", actionLabel = "Lihat Booking", onAction = onOpen)
    } else {
        Column(Modifier.padding(horizontal = AppSpacing.xl), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            bookings.take(5).forEach { booking ->
                PremiumCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(AppSpacing.md), onClick = onOpen) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Text(booking.packageName, style = MaterialTheme.typography.titleSmall, color = AppColors.TextPrimary); Text(booking.time, style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary) }
                        StatusBadge(booking.status)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    ListItem(headlineContent = { Text(label) }, leadingContent = { Icon(icon, contentDescription = null, tint = AppColors.Primary) }, modifier = Modifier.clickable(onClick = onClick), colors = ListItemDefaults.colors(containerColor = Color.Transparent))
}
