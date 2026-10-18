package com.carbroz.cbpartner.data.dynamic.diagnostics

import com.carbroz.cbpartner.domain.model.dynamic.DynamicScreen
import com.carbroz.cbpartner.domain.model.dynamic.component.DynamicComponent
import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent
import com.carbroz.cbpartner.domain.model.dynamic.content.DividerContent
import com.carbroz.cbpartner.domain.model.dynamic.content.ElementContent
import com.carbroz.cbpartner.domain.model.dynamic.content.IconContent
import com.carbroz.cbpartner.domain.model.dynamic.element.ButtonElement
import com.carbroz.cbpartner.domain.model.dynamic.element.DynamicElement
import com.carbroz.cbpartner.domain.model.dynamic.element.IconElement
import com.carbroz.cbpartner.domain.model.dynamic.element.ImageElement
import com.carbroz.cbpartner.domain.model.dynamic.element.InputElement
import com.carbroz.cbpartner.domain.model.dynamic.element.TextElement
import com.carbroz.cbpartner.domain.model.dynamic.group.DynamicGroup
import com.carbroz.cbpartner.domain.model.dynamic.group.StackGroup
import com.carbroz.cbpartner.domain.model.dynamic.section.DynamicSection
import com.carbroz.cbpartner.domain.model.dynamic.section.StackSection
import com.carbroz.cbpartner.domain.model.dynamic.template.FormTemplate

/**
 * Bounded, value-free domain SDUI hierarchy for developer diagnostics.
 *
 * Never writes free text, input/binding values, URLs, dynamic ids or action payloads.
 * It does not log or make changes to the mapped screen. Node paths use fixed labels
 * and numeric indexes only; unknown model classes are represented as 'other'.
 */
internal fun projectSduiStructure(screen: DynamicScreen, limit: Int = 100): List<String> {
    val lines = mutableListOf<String>()
    val budget = limit.coerceIn(1, 100)
    var truncated = false
    fun add(path: String, category: String, type: String, extra: String = "") {
        if (lines.size >= budget) { truncated = true; return }
        lines += "event=sdui.node path=$path category=$category type=$type$extra"
    }
    fun content(items: List<ElementContent>, path: String) {
        items.forEachIndexed { i, node ->
            val type = when (node) {
                is IconContent -> "icon"
                is DividerContent -> "divider"
            }
            add("$path[$i]", "content", type)
        }
    }
    fun element(items: List<DynamicElement>, path: String) {
        items.forEachIndexed { i, node ->
            val type = when (node) {
                is TextElement -> "text"
                is ButtonElement -> "button"
                is InputElement -> "input"
                is IconElement -> "icon"
                is ImageElement -> "image"
                else -> "other"
            }
            val leading = when (node) {
                is TextElement -> node.leadingContent
                is ButtonElement -> node.leadingContent
                is InputElement -> node.leadingContent
                is IconElement -> node.leadingContent
                is ImageElement -> node.leadingContent
                else -> emptyList()
            }
            val trailing = when (node) {
                is TextElement -> node.trailingContent
                is ButtonElement -> node.trailingContent
                is InputElement -> node.trailingContent
                is IconElement -> node.trailingContent
                is ImageElement -> node.trailingContent
                else -> emptyList()
            }
            add("$path[$i]", "element", type)
            content(leading, "$path[$i]/leading")
            content(trailing, "$path[$i]/trailing")
        }
    }
    fun groups(items: List<DynamicGroup>, path: String) {
        items.forEachIndexed { i, node ->
            add("$path[$i]", "group", if (node is StackGroup) "stack_group" else "other")
            element(node.elements, "$path[$i]/elements")
        }
    }
    fun sections(items: List<DynamicSection>, path: String) {
        items.forEachIndexed { i, node ->
            add("$path[$i]", "section", if (node is StackSection) "stack_section" else "other")
            element(node.elements, "$path[$i]/elements")
            groups(node.groups, "$path[$i]/groups")
        }
    }
    fun components(items: List<DynamicComponent>, path: String) {
        items.forEachIndexed { i, node ->
            add("$path[$i]", "component", if (node is StackComponent) "stack_component" else "other")
            element(node.elements, "$path[$i]/elements")
            sections(node.sections, "$path[$i]/sections")
        }
    }
    val template = screen.template
    add("template", "template", if (template is FormTemplate) "form_template" else "other")
    template.header?.let { components(it.components, "template/header/components") }
    components(template.components, "template/body/components")
    template.footer?.let { components(it.components, "template/footer/components") }
    if (truncated) lines += "event=sdui.truncated reason=node_limit"
    return lines
}
