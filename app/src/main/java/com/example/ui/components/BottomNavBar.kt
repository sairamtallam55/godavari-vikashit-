package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.model.AppLanguage
import com.example.ui.theme.HeritageGold
import com.example.ui.theme.NavyDeep

enum class AppTab(val route: String) {
    HOME("home"),
    AR_EXPERIENCE("ar_experience"),
    GALLERY_3D("gallery_3d"),
    TOURS_MAP("tours_map"),
    AI_GUIDE("ai_guide"),
    CULTURE("culture"),
    QUIZ("quiz"),
    EXHIBITION("exhibition"),
    PROFILE("profile"),
    ADMIN("admin")
}

data class BottomNavItem(
    val tab: AppTab,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
    val label: String
)

@Composable
fun BottomNavBar(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem(AppTab.HOME, Icons.Filled.Home, Icons.Outlined.Home, UiStrings.navHome(language)),
        BottomNavItem(AppTab.AR_EXPERIENCE, Icons.Filled.ViewInAr, Icons.Outlined.ViewInAr, UiStrings.navAr(language)),
        BottomNavItem(AppTab.GALLERY_3D, Icons.Filled.RotateRight, Icons.Outlined.RotateRight, UiStrings.nav3D(language)),
        BottomNavItem(AppTab.TOURS_MAP, Icons.Filled.Map, Icons.Outlined.Map, UiStrings.navTours(language)),
        BottomNavItem(AppTab.AI_GUIDE, Icons.Filled.Psychology, Icons.Outlined.Psychology, UiStrings.navAi(language)),
        BottomNavItem(AppTab.CULTURE, Icons.Filled.AutoStories, Icons.Outlined.AutoStories, UiStrings.navCulture(language))
    )

    NavigationBar(
        modifier = modifier,
        windowInsets = WindowInsets.navigationBars,
        containerColor = NavyDeep,
        contentColor = Color.White
    ) {
        items.forEach { item ->
            val isSelected = selectedTab == item.tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(item.tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.filledIcon else item.outlinedIcon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NavyDeep,
                    selectedTextColor = HeritageGold,
                    indicatorColor = HeritageGold,
                    unselectedIconColor = Color.White.copy(alpha = 0.7f),
                    unselectedTextColor = Color.White.copy(alpha = 0.7f)
                )
            )
        }
    }
}
