package com.eduplatform.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Іконка "відкрита книга" (filled), що відповідає логотипу застосунку
 * (ic_launcher_foreground.xml). Використовується замість Icons.Default.School.
 *
 * Форма намальована як суцільний (filled) силует — це потрібно, щоб параметр
 * `tint` у androidx.compose.material3.Icon коректно фарбував іконку, як це
 * відбувається зі стандартними Material Icons.
 *
 * Приклад використання:
 *   Icon(AppIcons.OpenBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
 */
object AppIcons {
    val OpenBook: ImageVector by lazy {
        ImageVector.Builder(
            name = "OpenBook",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.NonZero
        ) {
            // Ліва сторінка
            moveTo(11f, 19.5f)
            curveTo(10.4f, 18.65f, 9.95f, 18.1f, 9.55f, 17.78f)
            curveTo(9.1f, 17.42f, 8.6f, 17.2f, 7.43f, 17.2f)
            horizontalLineTo(4.6f)
            curveTo(4.1f, 17.2f, 3.85f, 17.2f, 3.7f, 17.1f)
            curveTo(3.58f, 17.02f, 3.5f, 16.9f, 3.5f, 16.75f)
            verticalLineTo(6.1f)
            curveTo(3.5f, 5.95f, 3.58f, 5.83f, 3.7f, 5.75f)
            curveTo(3.85f, 5.65f, 4.1f, 5.65f, 4.6f, 5.65f)
            horizontalLineTo(7.2f)
            curveTo(8.7f, 5.65f, 9.4f, 5.65f, 9.93f, 5.92f)
            curveTo(10.4f, 6.16f, 10.78f, 6.55f, 11.02f, 7.02f)
            curveTo(11.29f, 7.55f, 11f, 8f, 11f, 9.45f)
            close()
            // Права сторінка
            moveTo(13f, 19.5f)
            verticalLineTo(9.45f)
            curveTo(13f, 8f, 13f, 7.55f, 13.27f, 7.02f)
            curveTo(13.51f, 6.55f, 13.89f, 6.16f, 14.36f, 5.92f)
            curveTo(14.9f, 5.65f, 15.6f, 5.65f, 17.1f, 5.65f)
            horizontalLineTo(19.4f)
            curveTo(19.9f, 5.65f, 20.15f, 5.65f, 20.3f, 5.75f)
            curveTo(20.42f, 5.83f, 20.5f, 5.95f, 20.5f, 6.1f)
            verticalLineTo(16.75f)
            curveTo(20.5f, 16.9f, 20.42f, 17.02f, 20.3f, 17.1f)
            curveTo(20.15f, 17.2f, 19.9f, 17.2f, 19.4f, 17.2f)
            horizontalLineTo(16.57f)
            curveTo(15.4f, 17.2f, 14.9f, 17.42f, 14.45f, 17.78f)
            curveTo(14.05f, 18.1f, 13.6f, 18.65f, 13f, 19.5f)
            close()
            // Корінець книги (вузька смужка по центру, з'єднує сторінки)
            moveTo(11.3f, 9.7f)
            horizontalLineTo(12.7f)
            verticalLineTo(19.7f)
            horizontalLineTo(11.3f)
            close()
        }.build()
    }
}
