package litun.uxinnovator.ui.feed

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import litun.uxinnovator.components.UserFeedComponent
import litun.uxinnovator.components.UserFeedState
import litun.uxinnovator.components.UserFeedComponent.ModalChild
import litun.uxinnovator.domain.model.User
import litun.uxinnovator.domain.model.UserStatus
import myapplication.composeapp.generated.resources.Res
import myapplication.composeapp.generated.resources.ic_dark_mode
import myapplication.composeapp.generated.resources.ic_light_mode
import org.jetbrains.compose.resources.painterResource
import kotlin.math.PI
import kotlin.math.sin

// Dark mode: dark-tinted bg, medium-green text
private val GreenActiveBgDark = Color(0xFF1B5E20).copy(alpha = 0.4f)
private val GreenActiveTextDark = Color(0xFF4CAF50)
private val GreenActiveBorderDark = Color(0xFF388E3C).copy(alpha = 0.2f)

// Light mode: light-green bg, dark-green text
private val GreenActiveBgLight = Color(0xFFC8E6C9)
private val GreenActiveTextLight = Color(0xFF1B5E20)
private val GreenActiveBorderLight = Color(0xFF4CAF50).copy(alpha = 0.3f)

@Composable
private fun cardBackground(index: Int) =
    if (index % 2 == 0) MaterialTheme.colorScheme.surfaceContainerHigh
    else MaterialTheme.colorScheme.surfaceContainerLow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFeedScreen(
    component: UserFeedComponent,
    darkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {},
) {
    val state by component.state.subscribeAsState()
    val modalSlot by component.modalSlot.subscribeAsState()

    UserFeedContent(
        state = state,
        onRetry = component::refresh,
        darkTheme = darkTheme,
        onToggleTheme = onToggleTheme,
        onAddUser = component::openAddUser,
    )

    (modalSlot.child?.instance as? ModalChild.AddUser)?.let {
        AddUserSheet(component = it.component)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun UserFeedContent(
    state: UserFeedState,
    onRetry: () -> Unit,
    darkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {},
    onAddUser: () -> Unit = {},
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "User Directory",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    )
                },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            painter = painterResource(if (darkTheme) Res.drawable.ic_light_mode else Res.drawable.ic_dark_mode),
                            contentDescription = if (darkTheme) "Switch to light theme" else "Switch to dark theme",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddUser,
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            ) {
                Text(
                    text = "+",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Light,
                    lineHeight = 28.sp,
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when {
                state.isLoading -> ShimmerList()
                state.error != null -> ErrorState(
                    message = state.error,
                    onRetry = onRetry,
                )

                else -> UserList(users = state.users, darkTheme = darkTheme)
            }
        }
    }
}

@Composable
private fun UserList(users: List<User>, darkTheme: Boolean) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
    ) {
        itemsIndexed(users, key = { _, user -> user.id }) { index, user ->
            UserCard(user = user, index = index, darkTheme = darkTheme)
        }
    }
}

@Composable
private fun UserCard(user: User, index: Int, darkTheme: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBackground(index))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarCircle(name = user.name, index = index)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        StatusBadge(status = user.status, darkTheme = darkTheme)
    }
}

@Composable
private fun AvatarCircle(name: String, index: Int) {
    val (bg, fg) = when (index % 3) {
        0 -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        1 -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
    }
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.firstOrNull()?.uppercase() ?: "?",
            style = MaterialTheme.typography.titleMedium,
            color = fg,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun StatusBadge(status: UserStatus, darkTheme: Boolean) {
    val (bgColor, textColor, borderColor) = when (status) {
        UserStatus.ACTIVE -> if (darkTheme) {
            Triple(GreenActiveBgDark, GreenActiveTextDark, GreenActiveBorderDark)
        } else {
            Triple(GreenActiveBgLight, GreenActiveTextLight, GreenActiveBorderLight)
        }

        UserStatus.INACTIVE -> Triple(
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        )
    }
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(bgColor)
            .border(1.dp, borderColor, CircleShape)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = status.name,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
        )
    }
}

@Composable
private fun ShimmerList() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_progress",
    )
    val shimmerAlpha = 0.3f + (0.5f * sin(progress * PI.toFloat()).coerceIn(0f, 1f))
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
    ) {
        items(6) { index -> ShimmerCard(index = index, shimmerAlpha = shimmerAlpha) }
    }
}

@Composable
private fun ShimmerCard(index: Int, shimmerAlpha: Float) {
    val shimmerColor = MaterialTheme.colorScheme.surfaceBright.copy(alpha = shimmerAlpha)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBackground(index))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(shimmerColor),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerColor),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerColor),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(20.dp)
                .clip(CircleShape)
                .background(shimmerColor),
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "No Internet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
