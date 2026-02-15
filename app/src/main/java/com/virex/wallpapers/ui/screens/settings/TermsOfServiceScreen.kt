package com.virex.wallpapers.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Terms of Service Screen for VIREX Wallpapers */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsOfServiceScreen(onBack: () -> Unit) {
    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Условия использования") },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                            }
                        },
                        colors =
                                TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                )
                )
            }
    ) { padding ->
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .padding(padding)
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
        ) {
            Text(
                    text = "Условия использования VIREX Wallpapers",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                    text = "Последнее обновление: 13 февраля 2026",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            TermsSectionTitle("1. Принятие условий")
            TermsSectionContent(
                    """
Используя приложение VIREX Wallpapers, вы соглашаетесь с настоящими Условиями использования. Если вы не согласны с условиями, пожалуйста, не используйте приложение.
            """.trimIndent()
            )

            TermsSectionTitle("2. Описание услуги")
            TermsSectionContent(
                    """
VIREX Wallpapers — это приложение для установки обоев на Android-устройства:

• Локальные обои из встроенных коллекций (Wallpacks)
• Синхронизированные обои из Unsplash и Pexels
• AI-генератор обоев (PRO)
• Установка на экран блокировки, рабочий стол или оба экрана
            """.trimIndent()
            )

            TermsSectionTitle("3. Лицензия на использование")
            TermsSectionContent(
                    """
Мы предоставляем вам ограниченную, неисключительную, непередаваемую лицензию на:

• Загрузку и установку приложения
• Использование обоев для личных целей
• Установку обоев на ваши устройства

Запрещено:
• Распространение обоев в коммерческих целях
• Модификация или декомпиляция приложения
• Использование контента для создания конкурирующих продуктов
            """.trimIndent()
            )

            TermsSectionTitle("4. Источники контента")
            TermsSectionContent(
                    """
Обои в приложении получены из легальных источников:

• Unsplash — бесплатные изображения по лицензии Unsplash
• Pexels — бесплатные изображения по лицензии Pexels
• Локальные коллекции — созданы или лицензированы нами

Авторские права на изображения принадлежат их создателям.
            """.trimIndent()
            )

            TermsSectionTitle("5. PRO-версия и покупки")
            TermsSectionContent(
                    """
PRO-версия приложения включает:
• Отключение рекламы
• AI-генератор обоев
• Доступ ко всем premium-функциям

Условия покупки:
• Покупка осуществляется через RuStore
• Возврат средств — согласно политике RuStore
• PRO-статус привязывается к аккаунту RuStore
            """.trimIndent()
            )

            TermsSectionTitle("6. Реклама")
            TermsSectionContent(
                    """
Бесплатная версия содержит рекламу:
• Баннерная реклама в нижней части экрана
• Межстраничная реклама при навигации

Реклама предоставляется VK Ads (myTarget).
PRO-версия полностью без рекламы.
            """.trimIndent()
            )

            TermsSectionTitle("7. Ограничение ответственности")
            TermsSectionContent(
                    """
Приложение предоставляется "как есть". Мы не несём ответственности за:

• Работоспособность на всех устройствах
• Качество интернет-соединения
• Проблемы с загрузкой изображений
• Совместимость с конкретными версиями Android

Максимальная ответственность ограничена суммой покупки PRO-версии.
            """.trimIndent()
            )

            TermsSectionTitle("8. Прекращение доступа")
            TermsSectionContent(
                    """
Мы можем ограничить доступ к приложению при:
• Нарушении данных условий
• Злоупотреблении сервисом
• Попытках обхода системы покупок

Вы можете прекратить использование приложения в любое время, удалив его.
            """.trimIndent()
            )

            TermsSectionTitle("9. Изменения условий")
            TermsSectionContent(
                    """
Мы можем изменять эти условия. Существенные изменения будут отражены в обновлениях приложения. Продолжение использования означает согласие с новыми условиями.
            """.trimIndent()
            )

            TermsSectionTitle("10. Применимое право")
            TermsSectionContent(
                    """
Настоящие условия регулируются законодательством Российской Федерации. Споры разрешаются в соответствии с действующим законодательством РФ.
            """.trimIndent()
            )

            TermsSectionTitle("11. Контакты")
            TermsSectionContent(
                    """
По вопросам условий использования:
Email: support@virex.app
            """.trimIndent()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TermsSectionTitle(text: String) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun TermsSectionContent(text: String) {
    Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
    )
}
