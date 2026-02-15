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

/** Privacy Policy Screen for VIREX Wallpapers */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Политика конфиденциальности") },
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
                    text = "Политика конфиденциальности VIREX Wallpapers",
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

            SectionTitle("1. Сбор информации")
            SectionContent(
                    """
Приложение VIREX Wallpapers собирает минимальный объём данных, необходимый для работы:

• Идентификатор устройства — для проверки покупок и PRO-статуса
• Настройки приложения — хранятся локально на устройстве
• Кэшированные изображения — для быстрой загрузки обоев

Мы НЕ собираем:
• Личную информацию (имя, email, телефон)
• Данные о местоположении
• Контакты или содержимое телефона
• Историю просмотров вне приложения
            """.trimIndent()
            )

            SectionTitle("2. Использование данных")
            SectionContent(
                    """
Собранные данные используются исключительно для:

• Обеспечения работы функций приложения
• Проверки статуса PRO-подписки через RuStore
• Загрузки обоев из Unsplash и Pexels API
• Показа рекламы (для бесплатной версии)

Данные не передаются третьим лицам, кроме:
• RuStore — для обработки покупок
• VK Ads (myTarget) — для показа рекламы
            """.trimIndent()
            )

            SectionTitle("3. Реклама")
            SectionContent(
                    """
Бесплатная версия приложения содержит рекламу от VK Ads (myTarget SDK).

Рекламная сеть может собирать:
• Рекламный идентификатор устройства
• Информацию о взаимодействии с рекламой
• Общую статистику использования

PRO-версия полностью без рекламы.
            """.trimIndent()
            )

            SectionTitle("4. Хранение данных")
            SectionContent(
                    """
• Все пользовательские данные хранятся локально на устройстве
• Избранные обои синхронизируются только локально
• Кэш изображений можно очистить в настройках устройства
• При удалении приложения все данные удаляются
            """.trimIndent()
            )

            SectionTitle("5. Безопасность")
            SectionContent(
                    """
Мы используем стандартные меры безопасности Android:

• Шифрование данных при хранении
• Безопасные HTTPS-соединения для загрузки изображений
• Проверка подлинности покупок через RuStore API
            """.trimIndent()
            )

            SectionTitle("6. Права пользователя")
            SectionContent(
                    """
Вы имеете право:

• Удалить приложение и все связанные данные
• Очистить кэш приложения
• Отказаться от персонализированной рекламы в настройках Android
• Связаться с нами по вопросам конфиденциальности
            """.trimIndent()
            )

            SectionTitle("7. Изменения политики")
            SectionContent(
                    """
Мы можем обновлять эту политику. Существенные изменения будут отражены в обновлениях приложения.
            """.trimIndent()
            )

            SectionTitle("8. Контакты")
            SectionContent(
                    """
По вопросам конфиденциальности:
Email: support@virex.app
            """.trimIndent()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
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
private fun SectionContent(text: String) {
    Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
    )
}
