# 169 — Partner Type Selection — Original Arrays JSON V3 REVIEW CANDIDATE

**Decision date:** 2026-10-27
**Status:** REVIEW CANDIDATE — owner review/approval required; NOT an implementation freeze.
**Frontend:** `carbroz26/CB-Partner`, branch `architecture/dynamic-runtime-v2-discussion-freeze`.
**Review JSON:** [partner-type-selection-v3-original-arrays-REVIEW-CANDIDATE.json](partner-type-selection-v3-original-arrays-REVIEW-CANDIDATE.json) when uploaded; local review artifact is `partner-type-selection-v3-existing-arrays-REVIEW.json`.
**Baseline:** historical `partner-type-selection-ordered-v1-CANDIDATE.json` plus owner-selected navigation actions, with original visual properties preserved.

## 1. Decision boundaries and supersession

The owner has **explicitly superseded document 168** (`properties.order`) and does **not** want new `children[]` or `childOrder` authoring for this screen. Document 163's mixed-`children[]` design was previously superseded and remains historical. Do not implement either deprecated encoding.

For the present screen, use the existing JSON collections and their **current rendering order**. This meets the Partner Type visual hierarchy *without promising arbitrary interleaving of Elements and Sections/Groups in future screens*. A future use case requiring genuine arbitrary interleaving must be reviewed separately; no speculative generic ordering extension is authorized now.

**Unchanged hierarchy:** Screen -> one Template -> multiple Components -> Sections / directly owned Elements -> Groups / directly owned Elements -> Elements -> ElementContent. No new Dynamic category.

## 2. Source-supported rendering rule; no new format

Current `StackComponent` renderer displays `elements[]` first, then `sections[]`. Current `StackSection` renderer displays `elements[]` first, then `groups[]`. Current `StackGroup` renderer displays only `elements[]` in their authored array order.

The V3 authoring **uses this behavior deliberately**, retaining the exact key names `elements[]`, `sections[]`, `groups[]`. There is no `children[]`, no `properties.order`, no `properties.childOrder`, and no duplicate JSON object keys.

## 3. Main screen — exact structural mapping

One `FormTemplate` with proposed ID `tpl_P103T` and screenId `partner_type_selection`; existing Login hero background source `images/partner_login_hero_07`; two StackComponents.

**Component 1 — `partner_type_branding`:** `elements: []`, `sections: [partner_type_brand_row, partner_type_heading]` in authored order.
- Brand row is a horizontal StackSection: `elements: [back IconElement, logo ImageElement]`; `groups: [brand_copy StackGroup]`. Thus Back -> Logo -> Brand Copy without any ordering extension.
- Heading is a zero-surface vertical StackSection with `elements: [screen_title TextElement, screen_subtitle TextElement]`; `groups: []`. Preserve original authored typography, spacing and backgrounds.
- Back IconElement retains `navigate` with `mode: pop`.

**Component 2 — `partner_type_bottom_card`:** `elements: []`; `sections: [Individual Card, Organization Card, Under Organization Card, Help Link Section]`. Preserve white background, 24dp top corners/padding and the existing authored section spacing.

Every category card is **one whole-surface horizontal StackSection**:
- `elements: [avatar IconBadgeElement]`.
- `groups: [copy StackGroup, arrow StackGroup]`.
- Copy StackGroup vertical with exactly two TextElements (title, description), and proposed `weight: 1` for remaining horizontal width.
- Arrow StackGroup horizontal with exactly one IconBadgeElement; the old arrow Element's visual properties are unchanged.
- Three new arrow-group wrappers and one new heading Section are the **only new structural nodes**. All old IDs and their `properties` and `actions` are retained.
- Existing approved card visual design remains: `minHeight: 96`, border, rounded corners, background and proposed `pressedStyle`.

**Help link Section:** one TextElement with leading Help icon, whose existing `onClick` action is `present` with `presentationId: partner_type_help`.

## 4. Actions and provisional destinations

Category cards use **existing `navigate`** actions, each `payload.mode: push`, with four-field `destination`: `screenId`, `templateId`, `templateType: form_template`, `endpoint`. No `request` selection action, fake POST, body, or category-persistence assertion.

- Individual: `partner_individual_ekyc`, `tpl_P104I`, `/api/v1/partner/onboarding/select_partner_type/individual`
- Organization: `partner_organization_documents`, `tpl_P104G`, `/api/v1/partner/onboarding/select_partner_type/organization`
- Partner Under Organization: `partner_under_organization_ekyc`, `tpl_P104U`, `/api/v1/partner/onboarding/select_partner_type/partner_under_organization`

**These next-screen identities and endpoints are provisional** until respective Backend GET screens exist. The owner explicitly authorized temporary URLs; they must not be described as existing Backend APIs. Navigation to the new screens cannot be end-to-end tested yet. Selection persistence, KYC and company-code flows are deferred.

## 5. One existing BottomSheetPresentation

Presentation ID `partner_type_help`, type `bottom_sheet`, one StackComponent whose `elements: []`, `sections[]` hold five ordered StackSections:
1. Header: `elements: [help title TextElement, close IconElement]`.
2. Individual explanatory row: `elements: [avatar]`, `groups: [copy vertical StackGroup]`.
3. Organization explanatory row: same pattern.
4. Partner Under Organization explanatory row: same pattern.
5. Footer: `elements: [Got it ButtonElement]`.

Both Close and Got it retain `onClick: {type: "dismiss"}`. Preserve existing sheet properties, all text, imagery resources, spacing, sizing, borders and accessibility review requirements. Do not add a separate Help screen or duplicate presentation host.

## 6. JSON review checklist and source gaps, not implementation approval

The V3 artifact is syntactically valid JSON and structurally audited against V2: no `children[]` or `order` anywhere; all pre-existing node IDs, `properties` and `actions` retained. Exactly four new wrapper IDs. Three category destinations match the owner-provided provisional URL suffixes.

This **does not mean today's Kotlin mapper can render the entire UI**:
- Generic `StackSection.actions` / whole-card gestures and `pressedStyle` remain missing.
- Generic `StackSection.minHeight` remains missing in the current mapper/domain/layout.
- Generic `StackGroup.weight` remains missing in the mapper/domain/parent horizontal layout.
- New category avatar/back/arrow/help resource IDs must be supplied and explicitly registered.
- Real preview, Gradle tests, Android A35/Web visuals, responsive layout and BottomSheet interaction must follow separate source implementation authorization.
- Legacy Login/OTP/Country Picker/Dialog/Toast behaviors must remain unchanged.
- Shimmer/skeleton loading is **deferred**. Do not implement the rejected red banner; reuse existing generic error Dialogs.

## 7. Approval gate

**Current action:** present the complete JSON for owner review. Only after explicit approval may this V3 candidate become a frozen JSON baseline and proceed to focused generic DTO/Domain/Mapper/Renderer implementation. Do not modify Kotlin, Backend, asset registry or accepted old fixtures as part of this documentation step.
