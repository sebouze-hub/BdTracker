package com.mediatheque.bdtracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Couleur d'accent verte utilisée pour signaler un tome "lu" (demande du cahier des charges)
val VertLu = Color(0xFF2E7D32)
val RougeNonLu = Color(0xFFB0BEC5)

private val ColorsClaires = lightColorScheme(
    primary = Color(0xFF3949AB),
    secondary = VertLu
)

private val ColorsSombres = darkColorScheme(
    primary = Color(0xFF7986CB),
    secondary = VertLu
)

@Composable
fun BdTrackerTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (useDarkTheme) ColorsSombres else ColorsClaires
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
