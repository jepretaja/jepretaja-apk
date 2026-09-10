package com.jepretaja.app.ui.screens.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jepretaja.app.core.theme.AppColors
import com.jepretaja.app.core.util.Formatters
import com.jepretaja.app.data.model.BookingModel
import com.jepretaja.app.data.model.ChatModel
import com.jepretaja.app.data.model.NotificationModel
import com.jepretaja.app.ui.components.AppAvatar
import com.jepretaja.app.ui.components.AppTopBar
import com.jepretaja.app.ui.components.EmptyState
import com.jepretaja.app.ui.components.PremiumCard
import com.jepretaja.app.ui.components.StatusBadge
import com.jepretaja.app.ui.components.rememberAppTopBarScrollBehavior
import com.jepretaja.app.ui.screens.chat.ChatListViewModel
import com.jepretaja.app.ui.screens.home.MyBookingsViewModel
import com.jepretaja.app.ui.screens.notifications.NotificationsViewModel
import com.jepretaja.app.ui.state.AuthViewModel

private enum class InboxTab(val label: String) {
    ALL("Semua"), MESSAGES("Pesan"), BOOKINGS("Booking"), SYSTEM("Notifikasi")
}

private sealed interface InboxItem {
    val sortTime: Long
    data class Chat(val value: ChatModel, val unread: Int) : InboxItem {
        override val sortTime: Long = value.updatedAt?.toDate()?.time ?: 0L
    }
    data class Booking(val value: BookingModel) : InboxItem {
        override val sortTime: Long = value.createdAt?.toDate()?.time ?: value.date?.toDate()?.time ?: 0L
    }
    data class Notification(val value: NotificationModel) : InboxItem {
        override val sortTime: Long = value.createdAt?.toDate()?.time ?: 0L
    }
}

@Composable
fun InboxScreen(
    onChatClick: (String) -> Unit,
    onBookingClick: (String) -> Unit,
    authViewModel: AuthViewModel,
    chatViewModel: ChatListViewModel = hiltViewModel(),
    bookingViewModel: MyBookingsViewModel = hiltViewModel(),
    notificationViewModel: NotificationsViewModel = hiltViewModel(),
) {
    val authState by authViewModel.uiState.collectAsState()
    val uid = authState.uid
    var tab by remember { mutableStateOf(InboxTab.ALL) }
    val scrollBehavior = rememberAppTopBarScrollBehavior()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = { AppTopBar(title = "Kotak Masuk", scrollBehavior = scrollBehavior) },
    ) { padding ->
        if (uid == null) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.Default.NotificationsNone,
                    title = "Masuk untuk membuka kotak masuk",
                    description = "Pesan, booking, dan kabar penting akan terkumpul di sini.",
                )
            }
            return@Scaffold
        }

        val chats by remember(uid) { chatViewModel.chats(uid) }.collectAsState()
        val unreadByChat by remember(uid) { chatViewModel.belumDibaca(uid) }.collectAsState()
        val bookings by remember(uid) { bookingViewModel.bookings(uid) }.collectAsState()
        val notifications by remember(uid) { notificationViewModel.stream(uid) }.collectAsState(initial = emptyList())
        val items = remember(chats, unreadByChat, bookings, notifications, tab) {
            buildList {
                if (tab == InboxTab.ALL || tab == InboxTab.MESSAGES) {
                    addAll(chats.map { InboxItem.Chat(it, unreadByChat[it.chatId] ?: 0) })
                }
                if (tab == InboxTab.ALL || tab == InboxTab.BOOKINGS) {
                    addAll(bookings.map { InboxItem.Booking(it) })
                }
                if (tab == InboxTab.ALL || tab == InboxTab.SYSTEM) {
                    addAll(notifications.map { InboxItem.Notification(it) })
                }
            }.sortedByDescending { it.sortTime }
        }
        val unreadTotal = unreadByChat.values.sum() + notifications.count { it.readAt == null }
        val activeBookings = bookings.count { it.status !in setOf("completed", "cancelled", "rejected", "reviewed") }

        Column(Modifier.padding(padding).fillMaxSize()) {
            PremiumCard(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                elevation = 5.dp,
                color = AppColors.Surface,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(44.dp).clip(CircleShape).background(AppColors.PrimarySoft),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            if (unreadTotal > 0) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                            contentDescription = null,
                            tint = AppColors.Primary,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (unreadTotal > 0) "$unreadTotal kabar perlu dilihat" else "Semua sudah terbaca",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.W700,
                            color = AppColors.TextPrimary,
                        )
                        Text(
                            "$activeBookings booking aktif · ${chats.size} percakapan",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.TextSecondary,
                        )
                    }
                }
            }

            ScrollableTabRow(
                selectedTabIndex = tab.ordinal,
                containerColor = AppColors.Background,
                contentColor = AppColors.Primary,
                edgePadding = 12.dp,
            ) {
                InboxTab.values().forEach { item ->
                    val count = when (item) {
                        InboxTab.ALL -> unreadTotal
                        InboxTab.MESSAGES -> unreadByChat.values.sum()
                        InboxTab.BOOKINGS -> activeBookings
                        InboxTab.SYSTEM -> notifications.count { it.readAt == null }
                    }
                    Tab(
                        selected = tab == item,
                        onClick = { tab = item },
                        text = { Text(if (count > 0) "${item.label} ($count)" else item.label) },
                    )
                }
            }

            if (items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = when (tab) {
                            InboxTab.MESSAGES -> Icons.Default.ChatBubbleOutline
                            InboxTab.BOOKINGS -> Icons.Default.CalendarMonth
                            else -> Icons.Default.NotificationsNone
                        },
                        title = when (tab) {
                            InboxTab.MESSAGES -> "Belum ada pesan"
                            InboxTab.BOOKINGS -> "Belum ada booking"
                            InboxTab.SYSTEM -> "Belum ada notifikasi"
                            InboxTab.ALL -> "Kotak masuk masih kosong"
                        },
                        description = "Kabar baru akan muncul otomatis di sini.",
                    )
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(items, key = { item ->
                        when (item) {
                            is InboxItem.Chat -> "chat-${item.value.chatId}"
                            is InboxItem.Booking -> "booking-${item.value.bookingId}"
                            is InboxItem.Notification -> "notification-${item.value.notificationId}"
                        }
                    }) { item ->
                        when (item) {
                            is InboxItem.Chat -> ChatInboxRow(item, onClick = { onChatClick(item.value.chatId) })
                            is InboxItem.Booking -> BookingInboxRow(item.value, onClick = { onBookingClick(item.value.bookingId) })
                            is InboxItem.Notification -> NotificationInboxRow(item.value, onClick = { notificationViewModel.markRead(item.value.notificationId) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatInboxRow(item: InboxItem.Chat, onClick: () -> Unit) {
    val chat = item.value
    PremiumCard(modifier = Modifier.fillMaxWidth(), elevation = if (item.unread > 0) 5.dp else 2.dp, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppAvatar(url = chat.otherPartyPhotoUrl, name = chat.otherPartyName, size = 48.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(chat.otherPartyName.ifBlank { "Percakapan" }, style = MaterialTheme.typography.titleSmall, fontWeight = if (item.unread > 0) FontWeight.W700 else FontWeight.W500, color = AppColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(3.dp))
                Text(chat.lastMessage.ifBlank { "Belum ada pesan" }, style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (item.unread > 0) {
                Box(Modifier.size(22.dp).clip(CircleShape).background(AppColors.Primary), contentAlignment = Alignment.Center) {
                    Text(if (item.unread > 9) "9+" else item.unread.toString(), style = MaterialTheme.typography.labelSmall, color = AppColors.OnPrimary)
                }
            }
        }
    }
}

@Composable
private fun BookingInboxRow(booking: BookingModel, onClick: () -> Unit) {
    PremiumCard(modifier = Modifier.fillMaxWidth(), elevation = 3.dp, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(AppColors.Info.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = AppColors.Info)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(booking.packageName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.W700, color = AppColors.TextPrimary)
                Text("${booking.date?.let { Formatters.date(it) } ?: "Jadwal belum diatur"} · ${Formatters.currency(booking.total)}", style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary)
            }
            StatusBadge(status = booking.status)
        }
    }
}

@Composable
private fun NotificationInboxRow(notification: NotificationModel, onClick: () -> Unit) {
    val unread = notification.readAt == null
    PremiumCard(modifier = Modifier.fillMaxWidth(), elevation = if (unread) 5.dp else 2.dp, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(if (unread) AppColors.PrimarySoft else AppColors.SurfaceVariant), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = if (unread) AppColors.Primary else AppColors.TextSecondary)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(notification.title, style = MaterialTheme.typography.titleSmall, fontWeight = if (unread) FontWeight.W700 else FontWeight.W500, color = AppColors.TextPrimary)
                Spacer(Modifier.height(3.dp))
                Text(notification.body, style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            if (unread) Box(Modifier.size(8.dp).clip(CircleShape).background(AppColors.Primary))
        }
    }
}
