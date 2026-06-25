package com.eduplatform.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Теплі нейтральні поверхні — без фіолетового відтінку
// Material 3 генерує surfaceContainer* автоматично з primary через tonal palette,
// тому задаємо їх явно вручну.

private val SurfaceContainer         = Color(0xFFEEEAE1)  // картки, діалоги
private val SurfaceContainerHigh     = Color(0xFFE8E4DB)
private val SurfaceContainerHighest  = Color(0xFFE2DED5)  // Card() без кольору = цей
private val SurfaceContainerLow      = Color(0xFFF4F1EA)
private val SurfaceContainerLowest   = Color(0xFFFBF8F2)  // = Paper

private val LightColorScheme = lightColorScheme(
    primary                  = Gold,
    onPrimary                = Ink,
    primaryContainer         = Color(0xFFF5DEB3),
    onPrimaryContainer       = GoldDark,

    secondary                = Amber,
    onSecondary              = PaperRaised,
    secondaryContainer       = Color(0xFFFBDDC2),
    onSecondaryContainer     = AmberDark,

    tertiary                 = Tangerine,
    onTertiary               = PaperRaised,
    tertiaryContainer        = Color(0xFFFDDCC8),
    onTertiaryContainer      = TangerineDark,

    error                    = Coral,
    onError                  = PaperRaised,
    errorContainer           = Color(0xFFFAD4CC),
    onErrorContainer         = CoralDark,

    background               = Paper,
    onBackground             = Ink,

    surface                  = PaperRaised,
    onSurface                = Ink,
    surfaceVariant           = Color(0xFFF0EDE6),
    onSurfaceVariant         = Slate,

    // Явно задаємо всі surfaceContainer* щоб M3 не генерував фіолетові тони
    surfaceContainer         = SurfaceContainer,
    surfaceContainerHigh     = SurfaceContainerHigh,
    surfaceContainerHighest  = SurfaceContainerHighest,
    surfaceContainerLow      = SurfaceContainerLow,
    surfaceContainerLowest   = SurfaceContainerLowest,

    surfaceTint              = Gold,

    outline                  = Slate,           // вторинний текст (підписи, іконки)
    outlineVariant           = Line,            // лінії, роздільники, бордери

    inverseSurface           = Ink,
    inverseOnSurface         = Paper,
    inversePrimary           = Gold,

    scrim                    = Color(0xFF000000),
)

private val DarkColorScheme = darkColorScheme(
    primary                  = Gold,
    onPrimary                = Ink,
    primaryContainer         = GoldDark,
    onPrimaryContainer       = Paper,

    secondary                = Amber,
    onSecondary              = Ink,
    secondaryContainer       = AmberDark,
    onSecondaryContainer     = Paper,

    tertiary                 = Tangerine,
    onTertiary               = Ink,
    tertiaryContainer        = TangerineDark,
    onTertiaryContainer      = Paper,

    error                    = Color(0xFFEF8977),
    onError                  = Ink,
    errorContainer           = CoralDark,
    onErrorContainer         = Paper,

    background               = Color(0xFF0E1628),
    onBackground             = Paper,

    surface                  = Ink,
    onSurface                = Paper,
    surfaceVariant           = InkLight,
    onSurfaceVariant         = Color(0xFFB0BAC8),

    surfaceContainer         = Color(0xFF1E2C4A),
    surfaceContainerHigh     = Color(0xFF243355),
    surfaceContainerHighest  = Color(0xFF2A3A63),
    surfaceContainerLow      = Color(0xFF18243E),
    surfaceContainerLowest   = Color(0xFF0E1628),

    surfaceTint              = Gold,

    outline                  = Color(0xFF3A4A70),
    outlineVariant           = InkLight,

    inverseSurface           = Paper,
    inverseOnSurface         = Ink,
    inversePrimary           = GoldDark,

    scrim                    = Color(0xFF000000),
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content     = content,
    )
}