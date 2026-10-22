# 127 — COPY / PASTE NEXT-CHAT MASTER PROMPT — CB-Partner Login / OTP (2026-10-23)

**IMPORTANT:** This file is a copy-ready chat handoff. It contains the FULL current Login parity JSON and the FULL frozen OTP UI v2 JSON in appendices. The authoritative copies remain the referenced repository JSON files; never silently rewrite them.

---

You are continuing our **CB-Partner Dynamic Runtime V2** project. Reply, explain and discuss in **Hindi** (Roman Hindi acceptable); code and repository documentation are **English only**. My latest attachments include **approved Login, Country Picker and OTP UI screenshots**; treat them as real visual references. We want *productive, careful source-based implementation with minimal back-and-forth, no redundant long discussion or tests*. Do **NOT** create new architecture, classes, renderers, patterns, layers, branches, designs or frozen product decisions without proving need and respecting current source/architecture.

## Repository and goal

Frontend `https://github.com/carbroz26/CB-Partner`, local `D:\CarBroz\CB-Partner`, **work ONLY** on branch `architecture/dynamic-runtime-v2-discussion-freeze`; no temporary branches, PR merges, destructive reset or unrelated Git operations. Verified source HEAD before the last handoff-only document commit: `791ebe6261151c6113c387814ffd0ca337db9abf`; **re-fetch current HEAD** at the beginning.

Backend `https://github.com/carbroz26/carwash-backend`, `feature/backend-migration`, READ ONLY unless I separately authorize backend writes.

**Current immediate objective:** Complete **Login + Country Picker ACTUAL DYNAMIC RENDERING PARITY** with the approved stand-alone Login visual preview. **OTP UI contract is frozen but OTP runtime implementation and Timer/Resend are PAUSED**. Do not switch back to OTP work without the Login acceptance gate.

## First task: full mandatory audit before touching code

1. Read **EVERY file in `docs/architecture-proposal/dynamic-runtime-v2/` completely**, not only the latest docs. For each relevant doc, determine purpose, frozen/proposed/implemented/tested/rolled-back/blocked status and chronological supersession. Check whether a once-pending item became frozen later, or an apparently completed item was rolled back. Use source truth before editing documentation or code. Docs 112–118, 120–122, 124–126 and `docs/architecture-proposal/dynamic-runtime-v2/partner-login-country-picker-parity-v3.json`/`docs/architecture-proposal/dynamic-runtime-v2/partner-otp-ui-contract-v2.json` are especially important; **historical Doc123 was rolled back and is not active source**.
2. Read **ALL current source classes line by line across every package/module and tests**, with no guessing from filenames or memory: `core`, `domain`, `data`, `network`, `navigation`, `feature/splash`, `feature/dynamic`, `app`, `webApp`, `androidApp`, DI, Gradle/build/config. Understand why each class exists, who calls it, typed mapper ownership, Compose renderer, Store/action/effect scope, navigation/list/presentation lifecycle. Summarize only *actionable* findings; don't paste 125 document summaries in chat.
3. Inspect Git history and branch HEAD. **Recovery commit `f8b76120` explicitly reverted the bad P0–P5 OTP renderer wave.** Its old commits are not current source. The newer actual Login parity work after recovery is in commits `47725b3f`, `2575c74d`, `f18da833`, `e208635f`, `9fa5e7ff`, `2ea5a325`, `791ebe62`. Reverify latest source, don't treat commits as passing tests.
4. Read `docs/architecture-proposal/dynamic-runtime-v2/126-LOGIN-OTP-DYNAMIC-PARITY-RECOVERY-AND-NEXT-CHAT-HANDOFF-2026-10-23.md` carefully. It contains the full chronology, all decisions, gaps, priorities and safe continuation guidance. Audit against actual source and correction docs; it is not a license to overwrite frozen files.
5. After audit, give one concise **source-proven** gap matrix and file-level smallest action plan. Then continue with existing authorized Login parity work and **targeted tests**. If an actual new product decision/asset/backend permission is required, identify precisely and work on independent unblocked tasks.

## The two previews are NOT equivalent — critical!

`?cbpartnerVisualPreview=1` routes into **hardcoded** `VisualLoginPreview.kt`, approved visually by me and must remain untouched. It draws temporary skyline, shield, flags, 8 sample countries and its own Compose controls. It does NOT consume Dynamic JSON and is NOT our final runtime.

`?cbpartnerLoginPreview=1` routes into **real JSON → DynamicResponseDto → DynamicResponseMapper → Domain → App → DynamicStore → registered typed Dynamic renderers → DynamicPresentationHost** using an offline fixture, no backend. This is what must MATCH the visual prototype in geometry, colors, keyboard, country list/search/selected state, errors, full-width panel and sheet.

`webApp/src/wasmJsMain/resources/preview/partner-login.json` currently matches the **versioned Login parity v3 JSON** attached below, not the old B5 JSON. Build against the exact current checked-out source. **Do not claim 100% match until we view the real Dynamic preview and test it.**

## Frozen architecture rules

- Kotlin Multiplatform, Compose Multiplatform, Clean Architecture, MVI/UDF with one **Pure Store**, no ViewModel. No duplicate networking/navigation/cache/runtime/effect processing.
- Exactly **one typed Template per Screen** with unique `templateId`; Template can contain multiple instances of the one allowed concrete Component category. Existing concrete typed categories Template, Component, Section, Group, Element, ElementContent with DTO → Domain → Mapper → explicit Registry → Renderer. No reflection, generic UI property bag, screen-specific production rendering or silently ignored JSON.
- Current `FormTemplate`, `StackComponent`, `StackSection`, `ListSection`, `StackGroup`, `TextElement`, `InputElement`, `ImageElement`, `ButtonElement`, `IconElement`, `DividerElement`, and typed accessory `DividerContent`, `IconContent`, `ImageContent`. Verify actual registered types in current source before reusing, deleting or adding.
- `$binding`, `$context`, `$state`, `$literal` are frozen namespaces; `$item` only in permitted list row, `$config` only after allowlisted trusted config. **NO `$response`.**
- Existing frozen action handlers: request / navigate / present / dismiss / external_uri / state / sequence. Sequence keeps inert terminal rules; one `DynamicActionExecutor`. Never duplicate.
- Enforce **SRP, no duplication, no overengineering, no unrelated refactor**. First inspect/trace exact source line; make the smallest justified generic typed extension, add minimal targeted regression, record truth. No hardcoded production visuals or default colors/dimensions/flags/timers, and no fake real countries/assets.
- Follow **DISCUSS → RESEARCH → DECIDE → PLAN → FREEZE → IMPLEMENT → TEST → REVIEW → FREEZE**. Respect already-authorized Login parity, but never invent or silently re-freeze new product semantics.
- Branch: only `architecture/dynamic-runtime-v2-discussion-freeze`, non-destructive history. Backend read-only.

## Exact Login visual contract, Theme, and red error

The *approved* Web visual Login+Picker is the reference. Light 135-degree four-stop gradient belongs to `theme.properties.gradient`; **Template-only** URL hero `template.properties.backgroundImage`. White full-width lower Login card top rounded 24dp, internal 24dp padding, bottom anchored when content fits. Country Picker white edge-to-edge bottom sheet top rounded 24dp, current Dynamic v3 `heightFraction=0.70`, handle 36×4, search, list, selection, empty fallback.

Phone input: entire 56dp row (flag + dial code + grey divider + editable number) has **ONE red #DC2626 outer rounded border** if invalid. **Only numeric editable area** has **#FFF1F2 light-red fill**; rest stays white. NEVER red border only numeric area and never a second red inner outline. Inline phone validation is below row; reserve message space in later approved generic extension to avoid jumping. Network/server/rate-limit errors are distinct from phone validation and **must NOT be placed under the input**. Login's separate server error alert design is frozen in references; OTP explicitly forbids an alert card inside the white form.

Frozen JSON hierarchy: Screen `partner_login`, Template `tpl_7K2M9Q`, two Components `login_branding` and `login_form`, `country_picker` BottomSheet Presentation, `ListSection` catalog `countries`, `countrySearch` filter, selection result maps `countryId/countryCode/countryFlagAsset` atomically and dismisses. `ListSection.emptySectionId` references a **sibling** StackSection for empty fallback, which renders ONLY when empty (not as a normal sibling). No hardcoded ListElement or duplicate visibility condition. Country support/validation must not exceed approved backend rules.

The approved preview's **8 sample countries are UI-only**; backend CountryCatalog currently intentionally has no approved production dataset. Icon/flags/logo are mock canvases in the visual preview and DynamicPackagedAssets has NO corresponding approved registered assets; real hero URL `/images/partner_login_hero.jpg` is not verified, do not invent asset access. Legal `$config` URLs and sending OTP API body require actual trusted agreed values/contracts.

## OTP contract — preserve, don't implement now

Frozen OTP UI-only `docs/architecture-proposal/dynamic-runtime-v2/partner-otp-ui-contract-v2.json` (exact JSON below). Same Theme/Template upper/bottom composition; `Verify your number`, phone text and Change phone number. One segmented InputElement/one binding `otp` with six boxes 56×56dp and 5dp gap, `—` in all empty boxes. Normal border #D1D5DB, focused #3B82F6. Invalid => **ALL SIX red #DC2626 borders and ALL SIX #FFF1F2 backgrounds**; no red parent group border. Only code-related inline error below with reserved space. No network/server error card, no "Unable to verify OTP" error box in white sheet or under input. Actual network feedback OUTSIDE the sheet not yet decided; failure must not be silently swallowed.

Positional digit editing approved: sequential type, replace selected box only, filled backspace deletes current only, empty backspace moves focus backwards without shifting digits, paste at selected box overwrites forward ignoring excess. Single input/Store/binding, no new architecture.

Timer is CURRENTLY **static TextElement** `Resend OTP in 00:30`. `OR` is TextElement with leading/trailing DividerContent. Resend is CURRENTLY **non-clickable disabled TextElement**. Future action (only after separate freeze) is one direct existing request; **NO resend-confirmation dialog**. Backend resend cooldown 60sec conflicts with visual 00:30; require authoritative server duration/deadline. Verify button `request` payload/role/challenge/response is illustrative and backend-UNVERIFIED. OTP v2 declares future unsupported props (all-slot invalid validation border, placeholder, button.enabledWhen, reservedSpace, nullable errorBorder), not source-implemented after rollback. DON'T implement OTP as part of current Login parity.

## Current Login parity source increments (UNVERIFIED tests)

Implemented after recovery: 8 Web-only offline preview countries; generic typed BottomSheet heightFraction/shape/background + bounded white top-rounded sheet; typed `StackComponent.fillMaxHeight`; versioned parity-v3 JSON; `BottomSheetLayoutContractTest`; generic rounded-group clipping so number-only error tint does not leak; generic `FormTemplate` full viewport minHeight under scroll for authored `spaceBetween` bottom card positioning; Doc125 updated. Source head `791ebe62` **before** handoff-doc commit. DO NOT claim JVM/Web/Android tests passed or pixel parity. Check test source and run only essential tasks.

## Next practical work — NO scope creep

1. Verify current HEAD, read all docs/source and inspect true unverified blockers. First run/check minimal compile or relevant focused JVM tests for recent source.
2. Compare approved `?cbpartnerVisualPreview=1` against JSON-backed `?cbpartnerLoginPreview=1` at Galaxy A35 411×891 and responsive Web. Enumerate only real differences and fix one minimal *generic* root cause at a time. Preserve working hardcoded visual preview.
3. Priority: correct FormTemplate bottom anchoring, whole red phone row with number-only tinted fill, keyboard/caret, Country Picker full width/top radius/70% height/scroll/search/selected result/empty fallback, validation reserved-space layout. Real legal/config, approved assets, real country policy and backend Send OTP are independent BLOCKED gates; report honestly.
4. Verify compiler/tests after each affected small batch, not repetitive broad test plans. Check change list, regressions and visual screenshots. Do not touch OTP or backend.
5. If compile/runtime reveals missing contract/asset/backend requirement, report exact file + blocked condition and obtain relevant product input only when necessary.

Minimal Windows commands:
```powershell
git pull --ff-only origin architecture/dynamic-runtime-v2-discussion-freeze
.\gradlew.bat :domain:jvmTest :data:jvmTest :feature:dynamic:jvmTest :app:jvmTest :feature:dynamic:compileAndroidMain :webApp:wasmJsBrowserDevelopmentWebpack
.\gradlew.bat :webApp:wasmJsBrowserDevelopmentRun
```
Use the **actual server port**, not assumed localhost:8080. Explain progress in Hindi with concise audit findings; repository notes English.

## Full, exact current Login Dynamic parity v3 JSON (DO NOT overwrite without reviewed versioned diff)

```json
{
  "status": 200,
  "code": "SUCCESS",
  "message": "Partner login screen fetched successfully",
  "data": {
    "screenId": "partner_login",
    "schemaVersion": "3.0.0",
    "targetApp": "PARTNER",
    "initialBindings": {
      "mobileNumber": "",
      "countryId": "IN",
      "countryCode": "+91",
      "countryFlagAsset": "flags/in"
    },
    "template": {
      "id": "tpl_7K2M9Q",
      "type": "form_template",
      "properties": {
        "orientation": "vertical",
        "fillMaxSize": true,
        "scrollable": true,
        "verticalArrangement": {
          "type": "spaceBetween"
        },
        "padding": {
          "start": 0,
          "top": 0,
          "end": 0,
          "bottom": 0
        },
        "backgroundImage": {
          "source": {
            "type": "url",
            "value": "/images/partner_login_hero.jpg"
          },
          "contentScale": "crop",
          "alignment": "topCenter"
        }
      },
      "components": [
        {
          "id": "login_branding",
          "type": "stack_component",
          "properties": {
            "orientation": "vertical",
            "fillMaxWidth": true,
            "horizontalAlignment": "center",
            "padding": {
              "start": 24,
              "top": 48,
              "end": 24,
              "bottom": 32
            }
          },
          "elements": [
            {
              "id": "brand_name",
              "type": "text",
              "properties": {
                "text": "CB-Partner",
                "fontSize": 20,
                "fontWeight": 700,
                "color": "#0A3A8D",
                "textAlign": "center",
                "leading": [
                  {
                    "type": "image",
                    "properties": {
                      "source": {
                        "type": "resource",
                        "value": "images/cb_partner_logo"
                      },
                      "width": 28,
                      "height": 28,
                      "contentScale": "fit",
                      "spacing": {
                        "end": 8
                      }
                    }
                  }
                ],
                "spacing": {
                  "bottom": 32
                }
              }
            },
            {
              "id": "welcome_title",
              "type": "text",
              "properties": {
                "text": "Welcome back",
                "fontSize": 32,
                "fontWeight": 700,
                "color": "#0A3A8D",
                "textAlign": "center",
                "spacing": {
                  "bottom": 8
                }
              }
            },
            {
              "id": "welcome_subtitle",
              "type": "text",
              "properties": {
                "text": "Access your partner account",
                "fontSize": 16,
                "fontWeight": 400,
                "color": "#6B7280",
                "textAlign": "center"
              }
            }
          ]
        },
        {
          "id": "login_form",
          "type": "stack_component",
          "properties": {
            "orientation": "vertical",
            "fillMaxWidth": true,
            "background": {
              "color": "#FFFFFF"
            },
            "shape": {
              "type": "roundedCorner",
              "topStart": 24,
              "topEnd": 24,
              "bottomStart": 0,
              "bottomEnd": 0
            },
            "padding": {
              "start": 24,
              "top": 24,
              "end": 24,
              "bottom": 24
            }
          },
          "sections": [
            {
              "id": "phone_label_section",
              "type": "stack_section",
              "properties": {
                "orientation": "vertical",
                "fillMaxWidth": true,
                "padding": {
                  "bottom": 8
                }
              },
              "elements": [
                {
                  "id": "phone_label",
                  "type": "text",
                  "properties": {
                    "text": "Mobile number",
                    "fontSize": 14,
                    "fontWeight": 600,
                    "color": "#18263B"
                  }
                }
              ]
            },
            {
              "id": "phone_field_section",
              "type": "stack_section",
              "properties": {
                "orientation": "vertical",
                "fillMaxWidth": true,
                "padding": {
                  "bottom": 24
                }
              },
              "groups": [
                {
                  "id": "phone_input_row",
                  "type": "stack_group",
                  "properties": {
                    "orientation": "horizontal",
                    "fillMaxWidth": true,
                    "height": 56,
                    "verticalAlignment": "center",
                    "background": {
                      "color": "#FFFFFF"
                    },
                    "border": {
                      "width": 1,
                      "color": "#CBD5E1"
                    },
                    "shape": {
                      "type": "roundedCorner",
                      "cornerRadius": 12
                    },
                    "focusPresentation": {
                      "binding": "mobileNumber",
                      "border": {
                        "width": 1,
                        "color": "#0A3A8D"
                      }
                    },
                    "validationPresentation": {
                      "binding": "mobileNumber",
                      "errorBorder": {
                        "width": 1,
                        "color": "#DC2626"
                      },
                      "messageStyle": {
                        "fontSize": 12,
                        "fontWeight": 400,
                        "color": "#DC2626",
                        "spacing": {
                          "top": 6
                        }
                      }
                    }
                  },
                  "elements": [
                    {
                      "id": "country_selector",
                      "type": "text",
                      "properties": {
                        "text": {
                          "$binding": "countryCode"
                        },
                        "fontSize": 15,
                        "fontWeight": 600,
                        "color": "#18263B",
                        "minHeight": 44,
                        "padding": {
                          "start": 12,
                          "end": 8
                        },
                        "leading": [
                          {
                            "type": "image",
                            "properties": {
                              "source": {
                                "type": "resource",
                                "value": {
                                  "$binding": "countryFlagAsset"
                                }
                              },
                              "width": 24,
                              "height": 18,
                              "contentScale": "fit",
                              "spacing": {
                                "end": 8
                              }
                            }
                          }
                        ],
                        "trailing": [
                          {
                            "type": "icon",
                            "properties": {
                              "source": {
                                "type": "resource",
                                "value": "icons/chevron_down"
                              },
                              "size": 16,
                              "spacing": {
                                "start": 4
                              }
                            }
                          }
                        ]
                      },
                      "actions": {
                        "onClick": {
                          "type": "present",
                          "payload": {
                            "presentationId": "country_picker"
                          }
                        }
                      }
                    },
                    {
                      "id": "phone_divider",
                      "type": "divider",
                      "properties": {
                        "orientation": "vertical",
                        "height": 28,
                        "thickness": 1,
                        "color": "#D1D5DB",
                        "spacing": {
                          "start": 8,
                          "end": 12
                        }
                      }
                    },
                    {
                      "id": "mobile_number",
                      "type": "input",
                      "properties": {
                        "placeholder": "Enter mobile number",
                        "singleLine": true,
                        "keyboardType": "phone",
                        "inputFilter": "digits",
                        "maxLength": 15,
                        "fontSize": 16,
                        "fontWeight": 400,
                        "textColor": "#111827",
                        "placeholderColor": "#94A3B8",
                        "weight": 1,
                        "padding": {
                          "start": 0,
                          "top": 0,
                          "end": 12,
                          "bottom": 0
                        },
                        "validationStyle": {
                          "background": {
                            "color": "#FFF1F2"
                          }
                        },
                        "height": 56
                      },
                      "validation": {
                        "required": true,
                        "validator": {
                          "key": "phone_number",
                          "arguments": {
                            "region": {
                              "$binding": "countryId"
                            }
                          }
                        },
                        "message": "Enter a valid mobile number"
                      },
                      "binding": {
                        "key": "mobileNumber"
                      }
                    }
                  ]
                }
              ]
            },
            {
              "id": "continue_action_section",
              "type": "stack_section",
              "properties": {
                "orientation": "vertical",
                "fillMaxWidth": true,
                "padding": {
                  "bottom": 12
                }
              },
              "elements": [
                {
                  "id": "continue_button",
                  "type": "button",
                  "properties": {
                    "text": "Continue",
                    "height": 56,
                    "fillMaxWidth": true,
                    "fontSize": 18,
                    "fontWeight": 600,
                    "textColor": "#FFFFFF",
                    "background": {
                      "color": "#0A3A8D"
                    },
                    "shape": {
                      "type": "roundedCorner",
                      "cornerRadius": 16
                    },
                    "loading": {
                      "whileActionPending": true,
                      "disableWhilePending": true,
                      "replaceContent": true,
                      "indicator": {
                        "type": "circular",
                        "size": 20,
                        "color": "#FFFFFF"
                      }
                    }
                  },
                  "actions": {
                    "onClick": {
                      "type": "request",
                      "payload": {
                        "method": "POST",
                        "endpoint": "/api/v1/auth/send_otp",
                        "authentication": "NONE",
                        "validate": true,
                        "body": {
                          "countryCode": {
                            "$binding": "countryCode"
                          },
                          "phoneNumber": {
                            "$binding": "mobileNumber"
                          },
                          "deviceId": {
                            "$context": "deviceId"
                          }
                        },
                        "responseMode": "destination",
                        "mode": "push",
                        "retain": {
                          "challengeId": "data.challengeId",
                          "phoneNumber": "data.phoneNumber"
                        }
                      }
                    }
                  }
                }
              ]
            },
            {
              "id": "legal_section",
              "type": "stack_section",
              "properties": {
                "orientation": "vertical",
                "fillMaxWidth": true
              },
              "elements": [
                {
                  "id": "legal_text",
                  "type": "text",
                  "properties": {
                    "fontSize": 13,
                    "fontWeight": 400,
                    "lineHeight": 19,
                    "color": "#6B7280",
                    "textAlign": "center",
                    "fillMaxWidth": true,
                    "spans": [
                      {
                        "text": "By continuing, you agree to our "
                      },
                      {
                        "text": "Terms & Conditions",
                        "color": "#0A3A8D",
                        "underline": true,
                        "onClick": {
                          "type": "external_uri",
                          "payload": {
                            "uri": {
                              "$config": "legal.termsUrl"
                            }
                          }
                        }
                      },
                      {
                        "text": " and "
                      },
                      {
                        "text": "Privacy Policy",
                        "color": "#0A3A8D",
                        "underline": true,
                        "onClick": {
                          "type": "external_uri",
                          "payload": {
                            "uri": {
                              "$config": "legal.privacyUrl"
                            }
                          }
                        }
                      }
                    ]
                  }
                }
              ]
            }
          ]
        }
      ]
    },
    "presentations": [
      {
        "id": "country_picker",
        "type": "bottom_sheet",
        "properties": {
          "dismissOnOutsideClick": true,
          "dismissOnBack": true,
          "showHandle": true,
          "handle": {
            "width": 36,
            "height": 4,
            "color": "#D1D5DB",
            "cornerRadius": 2,
            "alignment": "center",
            "spacing": {
              "top": 8,
              "bottom": 12
            }
          },
          "heightFraction": 0.7,
          "background": {
            "color": "#FFFFFF"
          },
          "shape": {
            "type": "roundedCorner",
            "topStart": 24,
            "topEnd": 24,
            "bottomStart": 0,
            "bottomEnd": 0
          }
        },
        "initialBindings": {
          "countrySearch": ""
        },
        "components": [
          {
            "id": "picker_content",
            "type": "stack_component",
            "properties": {
              "orientation": "vertical",
              "fillMaxWidth": true,
              "background": {
                "color": "#FFFFFF"
              },
              "shape": {
                "type": "roundedCorner",
                "topStart": 24,
                "topEnd": 24,
                "bottomStart": 0,
                "bottomEnd": 0
              },
              "fillMaxHeight": true
            },
            "sections": [
              {
                "id": "picker_header",
                "type": "stack_section",
                "properties": {
                  "orientation": "horizontal",
                  "fillMaxWidth": true,
                  "verticalAlignment": "center",
                  "horizontalArrangement": {
                    "type": "spaceBetween"
                  },
                  "padding": {
                    "start": 24,
                    "top": 12,
                    "end": 16,
                    "bottom": 16
                  }
                },
                "elements": [
                  {
                    "id": "picker_title",
                    "type": "text",
                    "properties": {
                      "text": "Select country code",
                      "fontSize": 20,
                      "fontWeight": 700,
                      "color": "#0A3A8D",
                      "maxLines": 1,
                      "overflow": "ellipsis"
                    }
                  },
                  {
                    "id": "picker_close",
                    "type": "icon",
                    "properties": {
                      "source": {
                        "type": "resource",
                        "value": "icons/close"
                      },
                      "size": 20,
                      "minHeight": 44,
                      "minWidth": 44
                    },
                    "actions": {
                      "onClick": {
                        "type": "dismiss"
                      }
                    }
                  }
                ]
              },
              {
                "id": "picker_search",
                "type": "stack_section",
                "properties": {
                  "orientation": "vertical",
                  "fillMaxWidth": true,
                  "padding": {
                    "start": 16,
                    "top": 8,
                    "end": 16,
                    "bottom": 8
                  }
                },
                "elements": [
                  {
                    "id": "country_search",
                    "type": "input",
                    "properties": {
                      "placeholder": "Search country or code...",
                      "singleLine": true,
                      "keyboardType": "text",
                      "fontSize": 14,
                      "fontWeight": 400,
                      "textColor": "#111827",
                      "placeholderColor": "#94A3B8",
                      "height": 44,
                      "fillMaxWidth": true,
                      "border": {
                        "width": 1,
                        "color": "#DCE6F3"
                      },
                      "focusBorder": {
                        "width": 1,
                        "color": "#0A3A8D"
                      },
                      "shape": {
                        "type": "roundedCorner",
                        "cornerRadius": 10
                      },
                      "padding": {
                        "start": 12,
                        "top": 0,
                        "end": 12,
                        "bottom": 0
                      },
                      "leading": [
                        {
                          "type": "icon",
                          "properties": {
                            "source": {
                              "type": "resource",
                              "value": "icons/search"
                            },
                            "size": 18,
                            "spacing": {
                              "end": 8
                            }
                          }
                        }
                      ]
                    },
                    "binding": {
                      "key": "countrySearch"
                    }
                  }
                ]
              },
              {
                "id": "country_list",
                "type": "list_section",
                "properties": {
                  "fillMaxWidth": true,
                  "source": {
                    "type": "catalog",
                    "key": "countries"
                  },
                  "filter": {
                    "binding": "countrySearch",
                    "fields": [
                      "name",
                      "dialCode"
                    ],
                    "caseInsensitive": true
                  },
                  "selection": {
                    "mode": "single",
                    "itemKey": "id",
                    "selectedBinding": "countryId",
                    "result": {
                      "countryId": "id",
                      "countryCode": "dialCode",
                      "countryFlagAsset": "flagAsset"
                    },
                    "dismissOnSelect": true
                  },
                  "itemSeparator": {
                    "type": "divider",
                    "orientation": "horizontal",
                    "thickness": 1,
                    "color": "#EDF1F5",
                    "spacing": {
                      "start": 16,
                      "end": 16
                    }
                  },
                  "weight": 1,
                  "emptySectionId": "country_list_empty_state"
                },
                "groups": [
                  {
                    "id": "country_row",
                    "type": "stack_group",
                    "properties": {
                      "orientation": "horizontal",
                      "fillMaxWidth": true,
                      "verticalAlignment": "center",
                      "minHeight": 44,
                      "padding": {
                        "start": 16,
                        "top": 8,
                        "end": 16,
                        "bottom": 8
                      },
                      "background": {
                        "color": "#FFFFFF"
                      },
                      "selectedStyle": {
                        "background": {
                          "color": "#DCEBFC"
                        }
                      },
                      "horizontalArrangement": {
                        "type": "spacedBy",
                        "spacing": 12
                      }
                    },
                    "elements": [
                      {
                        "id": "country_flag",
                        "type": "image",
                        "properties": {
                          "source": {
                            "type": "resource",
                            "value": {
                              "$item": "flagAsset"
                            }
                          },
                          "width": 24,
                          "height": 18,
                          "contentScale": "fit"
                        }
                      },
                      {
                        "id": "country_name",
                        "type": "text",
                        "properties": {
                          "text": {
                            "$item": "name"
                          },
                          "fontSize": 14,
                          "fontWeight": 500,
                          "color": "#18263B",
                          "weight": 1,
                          "maxLines": 1,
                          "overflow": "ellipsis"
                        }
                      },
                      {
                        "id": "country_dial_code",
                        "type": "text",
                        "properties": {
                          "text": {
                            "$item": "dialCode"
                          },
                          "fontSize": 13,
                          "fontWeight": 400,
                          "color": "#64748B",
                          "maxLines": 1,
                          "overflow": "ellipsis"
                        }
                      },
                      {
                        "id": "selected_indicator",
                        "type": "icon",
                        "properties": {
                          "source": {
                            "type": "resource",
                            "value": "icons/check_circle_blue"
                          },
                          "size": 18,
                          "visibleWhen": {
                            "selected": true
                          },
                          "reserveSpaceWhenHidden": true
                        }
                      }
                    ]
                  }
                ]
              },
              {
                "id": "country_list_empty_state",
                "type": "stack_section",
                "groups": [
                  {
                    "id": "empty_content",
                    "type": "stack_group",
                    "properties": {
                      "orientation": "vertical",
                      "fillMaxWidth": true,
                      "horizontalAlignment": "center",
                      "padding": {
                        "start": 20,
                        "top": 32,
                        "end": 20,
                        "bottom": 32
                      },
                      "verticalArrangement": {
                        "type": "spacedBy",
                        "spacing": 8
                      }
                    },
                    "elements": [
                      {
                        "id": "empty_icon",
                        "type": "icon",
                        "properties": {
                          "source": {
                            "type": "resource",
                            "value": "icons/search_empty"
                          },
                          "size": 40
                        }
                      },
                      {
                        "id": "empty_title",
                        "type": "text",
                        "properties": {
                          "text": "No countries found",
                          "fontSize": 16,
                          "fontWeight": 600,
                          "color": "#111827",
                          "textAlign": "center"
                        }
                      },
                      {
                        "id": "empty_description",
                        "type": "text",
                        "properties": {
                          "text": "Try searching with a different name or dial code.",
                          "fontSize": 13,
                          "fontWeight": 400,
                          "color": "#64748B",
                          "textAlign": "center",
                          "maxLines": 3,
                          "overflow": "ellipsis"
                        }
                      }
                    ]
                  }
                ]
              }
            ]
          }
        ]
      }
    ],
    "theme": {
      "theme": "light",
      "statusBar": "transparent",
      "properties": {
        "gradient": {
          "type": "linear",
          "angle": 135,
          "colors": [
            {
              "color": "#DDF8F6",
              "stop": 0
            },
            {
              "color": "#F7FEFD",
              "stop": 0.28
            },
            {
              "color": "#FFFFFF",
              "stop": 0.55
            },
            {
              "color": "#D9F7F4",
              "stop": 1
            }
          ]
        }
      }
    }
  }
}
```

## Full, exact current OTP UI-only contract v2 JSON (DO NOT mistake declarations for implemented behavior)

```json
{
  "status": 200,
  "code": "SUCCESS",
  "message": "Partner OTP screen fetched successfully",
  "data": {
    "screenId": "partner_otp",
    "schemaVersion": "3.0.0",
    "targetApp": "PARTNER",
    "initialBindings": {
      "otp": ""
    },
    "template": {
      "id": "tpl_P102O",
      "type": "form_template",
      "properties": {
        "orientation": "vertical",
        "fillMaxSize": true,
        "scrollable": true,
        "verticalArrangement": {
          "type": "spaceBetween"
        },
        "padding": {
          "start": 0,
          "top": 0,
          "end": 0,
          "bottom": 0
        },
        "backgroundImage": {
          "source": {
            "type": "url",
            "value": "/images/partner_login_hero.jpg"
          },
          "contentScale": "crop",
          "alignment": "topCenter"
        }
      },
      "components": [
        {
          "id": "otp_branding",
          "type": "stack_component",
          "properties": {
            "orientation": "vertical",
            "fillMaxWidth": true,
            "horizontalAlignment": "start",
            "padding": {
              "start": 24,
              "top": 48,
              "end": 24,
              "bottom": 32
            }
          },
          "elements": [
            {
              "id": "otp_brand_name",
              "type": "text",
              "properties": {
                "text": "CB-Partner",
                "fontSize": 20,
                "fontWeight": 700,
                "color": "#0A3A8D",
                "textAlign": "center",
                "leading": [
                  {
                    "type": "image",
                    "properties": {
                      "source": {
                        "type": "resource",
                        "value": "images/cb_partner_logo"
                      },
                      "width": 28,
                      "height": 28,
                      "contentScale": "fit",
                      "spacing": {
                        "end": 8
                      }
                    }
                  }
                ],
                "spacing": {
                  "bottom": 32
                },
                "fillMaxWidth": true
              }
            },
            {
              "id": "otp_screen_title",
              "type": "text",
              "properties": {
                "text": "Verify your number",
                "fontSize": 32,
                "fontWeight": 700,
                "color": "#0A3A8D",
                "textAlign": "start",
                "spacing": {
                  "bottom": 12
                },
                "fillMaxWidth": true
              }
            },
            {
              "id": "otp_screen_subtitle",
              "type": "text",
              "properties": {
                "text": "We've sent a 6-digit OTP to",
                "fontSize": 16,
                "fontWeight": 400,
                "color": "#6B7280",
                "textAlign": "start",
                "fillMaxWidth": true,
                "spacing": {
                  "bottom": 4
                }
              }
            },
            {
              "id": "otp_target_phone",
              "type": "text",
              "properties": {
                "spans": [
                  {
                    "text": {
                      "$state": "phoneNumber"
                    },
                    "fontWeight": 700
                  }
                ],
                "fontSize": 16,
                "fontWeight": 700,
                "color": "#0A3A8D",
                "textAlign": "start",
                "fillMaxWidth": true,
                "spacing": {
                  "bottom": 12
                }
              }
            },
            {
              "id": "otp_change_number",
              "type": "text",
              "properties": {
                "text": "Change phone number",
                "fontSize": 14,
                "fontWeight": 600,
                "color": "#007AFF",
                "textAlign": "start",
                "fillMaxWidth": true
              },
              "actions": {
                "onClick": {
                  "type": "navigate",
                  "payload": {
                    "mode": "pop"
                  }
                }
              }
            }
          ]
        },
        {
          "id": "otp_form",
          "type": "stack_component",
          "properties": {
            "orientation": "vertical",
            "fillMaxWidth": true,
            "background": {
              "color": "#FFFFFF"
            },
            "shape": {
              "type": "roundedCorner",
              "topStart": 24,
              "topEnd": 24,
              "bottomStart": 0,
              "bottomEnd": 0
            },
            "padding": {
              "start": 24,
              "top": 24,
              "end": 24,
              "bottom": 24
            }
          },
          "sections": [
            {
              "id": "otp_field_section",
              "type": "stack_section",
              "properties": {
                "orientation": "vertical",
                "fillMaxWidth": true,
                "padding": {
                  "bottom": 0
                }
              },
              "groups": [
                {
                  "id": "otp_fields_group",
                  "type": "stack_group",
                  "properties": {
                    "orientation": "horizontal",
                    "fillMaxWidth": true,
                    "verticalAlignment": "center",
                    "validationPresentation": {
                      "binding": "otp",
                      "messageStyle": {
                        "fontSize": 12,
                        "fontWeight": 400,
                        "color": "#DC2626",
                        "spacing": {
                          "top": 8
                        },
                        "reserveSpaceWhenHidden": true,
                        "minHeight": 24
                      }
                    }
                  },
                  "elements": [
                    {
                      "id": "otp_code_input",
                      "type": "input",
                      "properties": {
                        "semanticRole": "input",
                        "keyboardType": "number",
                        "inputFilter": "digits",
                        "maxLength": 6,
                        "fontSize": 24,
                        "fontWeight": 600,
                        "textColor": "#0A3A8D",
                        "textAlign": "center",
                        "fillMaxWidth": true,
                        "presentation": {
                          "type": "segmented",
                          "count": 6,
                          "spacing": 5,
                          "segmentWidth": 56,
                          "segmentHeight": 56,
                          "placeholder": "—",
                          "background": {
                            "color": "#FFFFFF"
                          },
                          "border": {
                            "width": 1.5,
                            "color": "#D1D5DB"
                          },
                          "focusBorder": {
                            "width": 1.5,
                            "color": "#3B82F6"
                          },
                          "shape": {
                            "type": "roundedCorner",
                            "cornerRadius": 16
                          }
                        },
                        "validationStyle": {
                          "background": {
                            "color": "#FFF1F2"
                          },
                          "border": {
                            "width": 1.5,
                            "color": "#DC2626"
                          }
                        }
                      },
                      "binding": {
                        "key": "otp"
                      },
                      "validation": {
                        "required": true,
                        "pattern": "^[0-9]{6}$",
                        "message": "Please enter the 6-digit OTP."
                      }
                    }
                  ]
                }
              ]
            },
            {
              "id": "otp_timer_section",
              "type": "stack_section",
              "properties": {
                "orientation": "vertical",
                "fillMaxWidth": true,
                "padding": {
                  "top": 16,
                  "bottom": 14
                }
              },
              "elements": [
                {
                  "id": "otp_timer_text",
                  "type": "text",
                  "properties": {
                    "text": "Resend OTP in 00:30",
                    "fontSize": 14,
                    "fontWeight": 500,
                    "color": "#6B82AD",
                    "textAlign": "center",
                    "fillMaxWidth": true,
                    "leading": [
                      {
                        "type": "icon",
                        "properties": {
                          "source": {
                            "type": "resource",
                            "value": "icons/clock"
                          },
                          "size": 18,
                          "color": "#007AFF",
                          "spacing": {
                            "end": 8
                          }
                        }
                      }
                    ]
                  }
                }
              ]
            },
            {
              "id": "otp_separator_section",
              "type": "stack_section",
              "properties": {
                "orientation": "vertical",
                "fillMaxWidth": true,
                "padding": {
                  "bottom": 12
                }
              },
              "elements": [
                {
                  "id": "otp_or_separator",
                  "type": "text",
                  "properties": {
                    "text": "OR",
                    "fontSize": 12,
                    "fontWeight": 400,
                    "color": "#6B7280",
                    "textAlign": "center",
                    "fillMaxWidth": true,
                    "leading": [
                      {
                        "type": "divider",
                        "properties": {
                          "orientation": "horizontal",
                          "width": 132,
                          "thickness": 1,
                          "color": "#E2E8F0",
                          "spacing": {
                            "end": 12
                          }
                        }
                      }
                    ],
                    "trailing": [
                      {
                        "type": "divider",
                        "properties": {
                          "orientation": "horizontal",
                          "width": 132,
                          "thickness": 1,
                          "color": "#E2E8F0",
                          "spacing": {
                            "start": 12
                          }
                        }
                      }
                    ]
                  }
                }
              ]
            },
            {
              "id": "otp_resend_section",
              "type": "stack_section",
              "properties": {
                "orientation": "vertical",
                "fillMaxWidth": true,
                "padding": {
                  "bottom": 20
                }
              },
              "elements": [
                {
                  "id": "otp_resend_control",
                  "type": "text",
                  "properties": {
                    "text": "Resend OTP",
                    "fontSize": 14,
                    "fontWeight": 600,
                    "color": "#93B4E8",
                    "textAlign": "center",
                    "fillMaxWidth": true,
                    "minHeight": 32
                  }
                }
              ]
            },
            {
              "id": "otp_verify_section",
              "type": "stack_section",
              "properties": {
                "orientation": "vertical",
                "fillMaxWidth": true,
                "padding": {
                  "bottom": 12
                }
              },
              "elements": [
                {
                  "id": "otp_verify_button",
                  "type": "button",
                  "properties": {
                    "text": "Verify & Continue",
                    "height": 56,
                    "fillMaxWidth": true,
                    "fontSize": 18,
                    "fontWeight": 600,
                    "textColor": "#FFFFFF",
                    "background": {
                      "color": "#007AFF"
                    },
                    "shape": {
                      "type": "roundedCorner",
                      "cornerRadius": 16
                    },
                    "enabledWhen": {
                      "binding": "otp",
                      "length": 6
                    },
                    "disabledBackground": {
                      "color": "#BFDBFE"
                    },
                    "disabledTextColor": "#FFFFFF",
                    "loading": {
                      "whileActionPending": true,
                      "disableWhilePending": true,
                      "replaceContent": true,
                      "indicator": {
                        "type": "circular",
                        "size": 20,
                        "color": "#FFFFFF"
                      }
                    }
                  },
                  "actions": {
                    "onClick": {
                      "type": "request",
                      "payload": {
                        "method": "POST",
                        "endpoint": "/api/v1/auth/verify",
                        "authentication": "NONE",
                        "validate": true,
                        "responseMode": "destination",
                        "body": {
                          "challengeId": {
                            "$state": "challengeId"
                          },
                          "phoneNumber": {
                            "$state": "phoneNumber"
                          },
                          "deviceId": {
                            "$context": "deviceId"
                          },
                          "code": {
                            "$binding": "otp"
                          }
                        }
                      }
                    }
                  }
                }
              ]
            },
            {
              "id": "otp_help_section",
              "type": "stack_section",
              "properties": {
                "orientation": "vertical",
                "fillMaxWidth": true,
                "padding": {}
              },
              "elements": [
                {
                  "id": "otp_help_text",
                  "type": "text",
                  "properties": {
                    "text": "Didn't receive the code? Check your SMS or try again in a few minutes.",
                    "fontSize": 12,
                    "fontWeight": 400,
                    "lineHeight": 18,
                    "color": "#6B82AD",
                    "textAlign": "center",
                    "fillMaxWidth": true
                  }
                }
              ]
            }
          ]
        }
      ]
    },
    "theme": {
      "theme": "light",
      "statusBar": "transparent",
      "properties": {
        "gradient": {
          "type": "linear",
          "angle": 135,
          "colors": [
            {
              "color": "#DDF8F6",
              "stop": 0
            },
            {
              "color": "#F7FEFD",
              "stop": 0.28
            },
            {
              "color": "#FFFFFF",
              "stop": 0.55
            },
            {
              "color": "#D9F7F4",
              "stop": 1
            }
          ]
        }
      }
    }
  }
}
```

**STOP RULE:** The user must never again lose the previously accepted Login + Country Picker visual due to speculative OTP/cross-screen renderer changes. No new design, hidden source assumption, duplicate code, unverified test-success claim or backend write. Start productive work with a reconciled actionable gap table and smallest safe source-level continuation on the Login parity target.
