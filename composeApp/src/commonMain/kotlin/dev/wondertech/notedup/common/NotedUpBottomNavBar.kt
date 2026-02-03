package dev.wondertech.notedup.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.Tab
import dev.wondertech.notedup.navigation.BottomNavTab
import notedup.composeapp.generated.resources.Res
import notedup.composeapp.generated.resources.add_icon
import org.jetbrains.compose.resources.painterResource

@Composable
fun NotedUpBottomNavBar(
    currentTab: Tab,
    onTabSelected: (BottomNavTab) -> Unit,
    onAddTaskClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        BottomNavTab.HomeTab,
        BottomNavTab.CalendarTab
    )

    val selectedIndex = tabs.indexOfFirst { it.key == currentTab.key }.coerceAtLeast(0)
    val elevation by animateDpAsState(
        targetValue = 16.dp,
        animationSpec = tween(durationMillis = 300)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .height(60.dp)
                .fillMaxWidth(0.88f)
                .shadow(
                    elevation = elevation,
                    shape = RoundedCornerShape(34.dp),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                )
                .clip(CircleShape)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = CircleShape
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .matchParentSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    tab = tabs[0],
                    isSelected = selectedIndex == 0,
                    onClick = { onTabSelected(tabs[0]) },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(2.dp))

                AddTaskButton(
                    onClick = onAddTaskClick,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(2.dp))

                BottomNavItem(
                    tab = tabs[1],
                    isSelected = selectedIndex == 1,
                    onClick = { onTabSelected(tabs[1]) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    tab: BottomNavTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.0f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
        },
        animationSpec = tween(
            durationMillis = 350,
            easing = FastOutSlowInEasing
        )
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(
            durationMillis = 350,
            easing = FastOutSlowInEasing
        )
    )


    Column(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.scale(iconScale)
        ) {
            Icon(
                painter = painterResource(tab.icon),
                contentDescription = tab.title,
                modifier = Modifier.size(32.dp),
                tint = iconColor
            )
        }
    }
}

@Composable
private fun AddTaskButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val iconScale by animateFloatAsState(
        targetValue = 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val iconColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
        animationSpec = tween(
            durationMillis = 350,
            easing = FastOutSlowInEasing
        )
    )

    val backgroundColor by animateColorAsState(
        targetValue = Color.Transparent,
        animationSpec = tween(
            durationMillis = 350,
            easing = FastOutSlowInEasing
        )
    )

    Column(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.scale(iconScale)
        ) {
            Icon(
                painter = painterResource(Res.drawable.add_icon),
                contentDescription = "Add Task",
                modifier = Modifier.size(32.dp),
                tint = iconColor
            )
        }
    }
}
