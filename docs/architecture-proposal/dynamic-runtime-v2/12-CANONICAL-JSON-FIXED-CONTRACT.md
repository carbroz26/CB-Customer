# 12 — Canonical Existing JSON Shape (Owner-Supplied Fixtures)

**State:** Source-of-truth clarification for V2 planning. **No backend JSON restructuring authorized.** Header/Footer is the only proposed new layout structure discussed by the owner, subject to a concrete additive wire-contract decision.
**Evidence:** Two attached complete API response JSONs from the owner, `partner_login` (`tpl_7K2M9Q`) and `partner_otp` (`tpl_P102O`).

## Canonical wire structure
```text
{ status, code, message, data: {
    screenId, schemaVersion, targetApp,
    template: {
      id, type, properties,
      components: [
        {id, type, properties, elements?: [...], sections?: [
          {id, type, properties, elements?: [...], groups?: [
            {id, type, properties, elements: [...]}
          ]}
        ]}
      ]
    },
    theme?: {theme, statusBar, properties}
  },
  traceId?
}
```

**Cardinality confirmed:** response data has one template object; template has one or more components. Component can have direct elements and/or sections; section can have direct elements and/or groups; group owns elements; element is leaf of the structural category tree, with optional **nested content** via `properties.leading[]` / `properties.trailing[]`. A nested content object has `type, properties`, optionally `id, actions`; it is not a standalone Element. Do not impose exactly one Component. Do not elevate Content to structural sibling node.

**Concrete counts verified**:
- Login: template `form_template`, 3 components: brand_content has 6 direct elements; login_content has 2 sections (mobile_field_section: 1 group -> 2 elements; action_section: 2 direct elements); hero_content: 1 direct element.
- OTP: template `form_template`, 2 components: otp_brand_content 7 direct elements; otp_content has 2 sections (otp_field_section: 1 group -> 1 element; otp_action_section: 3 direct elements).

Both fixed fixtures demonstrate same templateType with different unique template IDs. The current examples do not prove an ordering strategy for one Component with both non-empty elements and sections at once; that must be specified separately while preserving the fixed fields.

## Exact fields observed
- Template properties: orientation, verticalArrangement {type, spacing}, horizontalAlignment, padding {start,top,end,bottom}, fillMaxSize, semanticRole.
- Component/Section: orientation, verticalArrangement and where provided horizontalArrangement, horizontalAlignment/verticalAlignment, padding, fillMaxWidth.
- Group: orientation, verticalArrangement, horizontalArrangement, verticalAlignment, padding, height, fillMaxWidth, background {color}, border {width,color}, shape {type,cornerRadius}.
- Text Element: semanticRole, text or spans, fontSize/fontWeight/letterSpacing/lineHeight, color/textAlign/fillMaxWidth, leading/trailing Content, actions. Text spans can include text expression, color, fontWeight, underline, onClick external_uri.
- Image Element: url, width/height/maxWidth/fillMaxWidth/contentScale. Icon Element belongs to concrete supported types even if not present as a standalone element in these fixtures.
- Input Element: placeholder, keyboardType, maxLength, weight, textAlign, segmented presentation {type,count,spacing,segmentWidth,segmentHeight,background,border,shape}, binding {key}, validation {required,pattern,message}.
- Button Element: text, height, fillMaxWidth, fontSize/fontWeight/textColor/background {type=linearGradient,angle,colors[{color,stop}]}, shape, trailing Content, actions.onClick Request.
- Content in samples: divider orientation/width/height/thickness/color and icon name/size/color. Content spacing is part of separately decided contract, not visibly present in these two samples.
- Action events: onClick in samples; Request payload method,endpoint,authentication,validate,body ($binding/$state/$context),responseMode,retain. Navigate uses destination {screenId,templateId,templateType,endpoint}. External URI may use $context or $literal.
- Theme: theme, statusBar, properties.gradient {type=linear,angle,colors[{color,stop}]}.
- OTP Text resend example includes `enabled: false` and `disabledColor` in properties. These are fields visible in owner's fixture and must not be lost in a V2 field mapper just because existing TextElement model omits them.

## Planning decisions locked by this clarification
1. Keep these existing JSON names and structural positions unchanged; use typed DTO/domain transformations behind the same wire format.
2. Preserve one Template and multi-Component support, including direct and nested Element paths.
3. Header/Footer may be added as a backward-compatible template extension, with independent slot padding and body-only application of legacy template padding; **proposed header/body/footer redesign is NOT approved as a replacement for `template.components`**. Keep main components array in its established position.
4. The documented V2 renderer/mapping packages and strict SRP stay target-first; do not redesign them to copy old source.
5. The previously asserted template singular `component` is wrong. V2 `TemplateNode.components: List<ComponentNode>` is correct.
6. Distinguish the two evidence conflicts for explicit design review:
   - Owner-supplied fixtures use icon Content `properties.name` (arrow_forward, edit), while previous frozen visual policy says URL plus fallbackUrl and no glyph registry. Cannot claim both automatically render unchanged without an agreed contract/migration.
   - Theme gradient stop positions `stop`, Text enabled/disabledColor and text-span actions are actual fixture fields; V2 DTO mapping must retain these even if older domain classes did not.
7. Do not infer that method/auth are already provided on `destination`; the owner-supplied fixtures show four fields. New fields or defaults require deliberate additive contract approval.

## Regression acceptance
Decode both supplied fixtures without changing their wire structure; assert template/component/section/group/element tree counts; preserve text, content, actions and visual field values; correctly resolve dynamic text spans and request body references; ensure no silent dropped icon or enabled state. For header/footer, add **separate** new fixture only after the exact additive JSON extension is approved.

## Effect on previous docs
Any prior proposal requiring migration to `template.body.components`, exactly one component, or JSON-wide replacement is superseded here. A V2 class design can use a typed TemplateShell or BodySlot internally without moving the existing `template.components` wire array. Changes to this confirmed wire contract need an explicit owner decision, not inference from source.
