package uz.electro.remote.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.electro.remote.data.NewsItemDto
import uz.electro.remote.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** Лента из админки: новости, уведомления, важное. Новое — сверху. */
@Composable
fun NewsScreen(items: List<NewsItemDto>, onBack: () -> Unit) {
    ScreenScaffold("Новости", onBack) {
        if (items.isEmpty()) {
            EmptyNote("Пока ничего нет. Здесь появятся новости и уведомления от оператора.")
        }
        items.forEach { n ->
            val (icon, tint) = when (n.kind) {
                "alert" -> Icons.Outlined.Warning to ElectroColors.Warn
                "news" -> Icons.Outlined.Campaign to ElectroColors.Info
                else -> Icons.Outlined.Notifications to ElectroColors.Accent
            }
            Surface(color = ElectroColors.Surface, shape = Radius.Md, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(Space.x4), verticalAlignment = Alignment.Top) {
                    Box(
                        Modifier.size(36.dp).clip(CircleShape).background(tint.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center,
                    ) { Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp)) }
                    Spacer(Modifier.width(Space.x3))
                    Column(Modifier.weight(1f)) {
                        Text(n.title, style = ElectroType.Body, color = ElectroColors.TextPrimary)
                        if (n.body.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(n.body, style = ElectroType.Caption, color = ElectroColors.TextSecondary)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(newsDate(n.created_at), style = ElectroType.Unit, color = ElectroColors.TextMuted)
                    }
                }
            }
        }
    }
}

private fun newsDate(iso: String): String = runCatching {
    val clean = iso.replace(Regex("[.][0-9]+"), "")     // без дробных секунд
    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
    val d = parser.parse(clean) ?: return ""
    SimpleDateFormat("d MMMM, HH:mm", Locale("ru")).apply { timeZone = TimeZone.getDefault() }.format(d)
}.getOrDefault("")

/** Колокольчик «Новости» с бейджем непрочитанных. */
@Composable
fun NewsBell(unread: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier) {
        Surface(
            onClick = onClick, shape = CircleShape, color = ElectroColors.SurfaceElevated,
            modifier = Modifier.size(44.dp),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Outlined.Notifications, null,
                    tint = if (unread > 0) ElectroColors.Accent else ElectroColors.TextSecondary,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        if (unread > 0) {
            Box(
                Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-2).dp)
                    .height(16.dp).clip(Radius.Pill).background(ElectroColors.Accent)
                    .padding(horizontal = 5.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(if (unread > 9) "9+" else "$unread", color = ElectroColors.OnAccent,
                    fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
