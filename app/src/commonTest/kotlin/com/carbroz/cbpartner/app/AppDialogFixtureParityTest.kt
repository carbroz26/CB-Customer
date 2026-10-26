package com.carbroz.cbpartner.app

import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent
import com.carbroz.cbpartner.domain.model.dynamic.content.IconContent
import com.carbroz.cbpartner.domain.model.dynamic.element.ButtonElement
import com.carbroz.cbpartner.domain.model.dynamic.element.IconBadgeElement
import com.carbroz.cbpartner.domain.model.dynamic.element.TextElement
import com.carbroz.cbpartner.domain.model.dynamic.section.StackSection
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Checks the actual packaged JSON and shared production mapper, not a separately drawn preview.
 * The startup variants intentionally omit unsafe Cancel/Close actions when Bootstrap is blocked.
 */
class AppDialogFixtureParityTest {
    @Test
    fun everyPackagedDialogMapsAndReferenceLayoutsUseExistingHierarchy() = runTest {
        val dialogs = loadPackagedDialogTemplates()
        assertEquals(
            setOf(
                "maintenance", "required_update", "optional_update", "connection",
                "server_error", "secure_connection", "server_error_startup",
                "secure_connection_startup",
            ),
            dialogs.keys,
        )
        dialogs.forEach { (_, presentation) ->
            val surface = assertIs<StackComponent>(presentation.components.single())
            assertTrue(surface.sections.isNotEmpty())
            assertTrue(surface.sections.all { it is StackSection })
        }

        listOf("required_update", "optional_update").forEach { name ->
            val surface = assertIs<StackComponent>(dialogs.getValue(name).components.single())
            val supportive = assertIs<StackSection>(
                surface.sections.single { it.id == "supportive_info_section" },
            )
            assertEquals("horizontal", supportive.orientation)
            assertEquals(2, supportive.groups.single().elements.size)
        }
        val maintenance = assertIs<StackComponent>(dialogs.getValue("maintenance").components.single())
        assertTrue(maintenance.sections.none { it.id == "supportive_info_section" })
    }

    @Test
    fun horizontalServerAndSecurityDialogsKeepRetryDistinctFromSafeCancel() = runTest {
        val dialogs = loadPackagedDialogTemplates()
        listOf("server_error", "secure_connection").forEach { kind ->
            val surface = assertIs<StackComponent>(dialogs.getValue(kind).components.single())
            val header = assertIs<StackSection>(
                surface.sections.single { it.id == "dialog_summary_section" },
            )
            assertEquals("horizontal", header.orientation)
            assertIs<IconBadgeElement>(header.elements.single())
            val title = assertIs<TextElement>(header.groups.single().elements.first())
            val close = assertIs<IconContent>(title.trailingContent.single())
            assertEquals(
                DynamicValue.StringValue("cancel"),
                close.actions["onClick"]?.payload?.get("dialogIntent"),
            )

            val actions = assertIs<StackSection>(
                surface.sections.single { it.id == "actions_section" },
            )
            val retry = assertNotNull(
                actions.elements.filterIsInstance<ButtonElement>()
                    .firstOrNull { it.id == "action_retry" },
            )
            assertEquals(
                DynamicValue.StringValue("retry"),
                retry.actions["onClick"]?.payload?.get("dialogIntent"),
            )
            val startup = assertIs<StackComponent>(
                dialogs.getValue(kind + "_startup").components.single(),
            )
            val startupTitle = assertIs<TextElement>(
                startup.sections.single { it.id == "dialog_summary_section" }
                    .groups.single().elements.first(),
            )
            assertTrue(startupTitle.trailingContent.isEmpty())
            assertEquals(
                listOf("action_retry"),
                startup.sections.single { it.id == "actions_section" }
                    .elements.filterIsInstance<ButtonElement>().map { it.id },
            )
        }
        val serverActions = assertIs<StackComponent>(
            dialogs.getValue("server_error").components.single(),
        ).sections.single { it.id == "actions_section" }.elements.filterIsInstance<ButtonElement>()
        assertEquals(listOf("action_cancel", "action_retry"), serverActions.map { it.id })
        assertEquals(
            DynamicValue.StringValue("cancel"),
            serverActions.first().actions["onClick"]?.payload?.get("dialogIntent"),
        )
    }
}
