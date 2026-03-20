package litun.uxinnovator.ui.feed

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import litun.uxinnovator.domain.model.Gender
import litun.uxinnovator.domain.model.User
import litun.uxinnovator.domain.model.UserStatus
import litun.uxinnovator.ui.theme.AppTheme

private val previewUsers = listOf(
    User(1, "John Doe", "john.doe@example.com", Gender.MALE, UserStatus.ACTIVE),
    User(2, "Sarah Wilson", "sarah.w@techpulse.io", Gender.FEMALE, UserStatus.ACTIVE),
    User(3, "Alex Chen", "a.chen@global.net", Gender.MALE, UserStatus.INACTIVE),
    User(4, "Elena Rodriguez", "e.rodriguez@legal.com", Gender.FEMALE, UserStatus.ACTIVE),
    User(5, "Marcus Thorne", "marcus.t@design.co", Gender.MALE, UserStatus.INACTIVE),
)

@Preview
@Composable
private fun PreviewUserFeedLoaded() {
    AppTheme(darkTheme = true) {
        UserFeedContent(
            state = UserFeedState(users = previewUsers, isLoading = false),
            onRetry = {},
        )
    }
}

@Preview
@Composable
private fun PreviewUserFeedLoading() {
    AppTheme(darkTheme = true) {
        UserFeedContent(
            state = UserFeedState(isLoading = true),
            onRetry = {},
        )
    }
}

@Preview
@Composable
private fun PreviewUserFeedError() {
    AppTheme(darkTheme = true) {
        UserFeedContent(
            state = UserFeedState(error = "Unable to reach server"),
            onRetry = {},
        )
    }
}
