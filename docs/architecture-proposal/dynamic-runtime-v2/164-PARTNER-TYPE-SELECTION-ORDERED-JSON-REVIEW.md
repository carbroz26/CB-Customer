# 164 — Partner Type Selection — Ordered-Children Complete JSON V1 REVIEW CANDIDATE

**Status:** NOT FINAL / NOT IMPLEMENTATION-READY. It is a full structurally authored screen candidate using frozen decision [163](163-ORDERED-MIXED-CHILDREN-JSON-DOMAIN-MAPPER-CONTRACT-FREEZE.md) and existing Login v19/OTP v3 visual conventions. **No screen source implementation authorized by this file.**

**Full candidate file:** [partner-type-selection-ordered-v1-CANDIDATE.json](partner-type-selection-ordered-v1-CANDIDATE.json).

## Exact screen-owned hierarchy
One FormTemplate `tpl_P103T` (new unique proposed screen-instance ID) with two StackComponents: Branding (hero background from Login/OTP, Back/brand/title/subtitle), and bottom white vertical panel (24dp top corners, 24dp authored padding). Panel `children` exactly four `StackSection` nodes, in order: Individual, Organization, Under Organization, Help. Three cards each have **ordered** `children` exactly `[IconBadgeElement(64dp), StackGroup(vertical, exactly two TextElements, weight 1), IconBadgeElement(40dp)]`; the Sections own card background, border, shape, minimum height, full-card onClick, pressed state. The fourth Help section is a simple TextElement with leading help icon + existing `present`.

One `BottomSheetPresentation` named `partner_type_help`, using existing handle/rounding and a vertical component with Header, three informational avatar+text rows, Footer Got it `ButtonElement`; X/Got it use existing `dismiss`. No additional Help screen or different renderer. The image reference's six visual states are a reference, but error behavior is exclusively the **already implemented generic dialogs** (no red banner). **Only generic shimmer placeholders** are under separate discussion.

## Source-backed gaps deliberately exposed in the candidate
- The `children[]` JSON format is frozen by 163 but DTO/Domain/Mapper/Renderer changes are not yet implemented. **Do not load candidate as working source on Android/Web yet.**
- Generic `StackSection.actions`, `pressedStyle`, `minHeight`, and horizontal `StackGroup.weight` are small reusable requirements visible in candidate, but remain separate pending implementation freeze. Not screen-specific property hacks.
- The candidate uses resource IDs `images/partner_type_individual_01`, `images/partner_type_organization_01`, `images/partner_type_under_organization_01`, `icons/partner_type_arrow_right_01`, `icons/partner_type_back_01`, and `icons/partner_type_help_01`. These **do not exist in the current packaged resource registry**, and must be authored/cropped/registered before preview use.
- The deliberately nonproduction action endpoint `/__contract_pending__/partner-type-selection` is a **BLOCKER PLACEHOLDER**, never approved Backend. Current `CB-Backend/development` enum has `INDIVIDUAL` and `ORGANIZATION` only. `UNDER_ORGANIZATION` is a proposed stable identifier requiring Backend/business approval, and existing registration endpoints are not a matching category selection endpoint. No invented response destination or KYC route.
- Current startup screen-loading source is unavailable before its own network response, so shimmer cannot simply be declared in this screen JSON to cover initial load. Exact generic pending-state source and skeleton descriptor must be reconciled before final JSON freeze. **No red error banner**; use existing error Dialogs.
- Main cards use `minHeight:96`, not hardcoded `height:96`, to avoid clipping descriptions and accessibility large-font text.

## Decisions before converting candidate to frozen implementation contract
1. Approve generic StackSection actions/pressedStyle/minHeight and StackGroup.weight **only if source audit says genuinely necessary**, together with ordered-children traversal and request-pending guards.
2. Resolve full-screen-first-load vs active-screen request-pending shimmer state trigger, reusable skeleton geometry, and Backend ownership.
3. Confirm three business partner IDs, selection endpoint/auth/body, and successful response `data.destination` shape with actual Backend.
4. Approve final asset sources and compare exact typography/background/spacing on Samsung A35. Preserve existing Login, OTP, country picker, splash and Dialogs.
5. Then finalize exact JSON, approve one targeted frontend implementation manifest, implement/tests/device review. Backend remains untouched until separate migration authorization.

## 2026-10-26 current discussion boundary (handoff clarification)

Owner has NOT frozen this full Partner Type V1 JSON as implementation-ready. The ordered-child **wire design** in Doc 163 is frozen; the screen JSON in this file remains a candidate. The user explicitly rejected adding an error banner: **all error states reuse the already accepted generic dialogs**. Only generic loading shimmer/skeleton design remains to discuss. Next chat must audit all source and V2 documents using 165/166/167, verify renderer/DTO/Domain gaps and read-only Backend enum+route constraints; do not implement candidate JSON or assume resource IDs/endpoints are real. Existing Login/OTP and Toast must stay frozen.
