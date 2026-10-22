package com.carbroz.cbpartner.data.dynamic

internal object DynamicContractFixtures {
    private const val DOLLAR = "$"

    val GENERIC_SCREEN_JSON = """{"status":200,"code":"SUCCESS","message":"Dynamic screen loaded","data":{"screenId":"screen_generic","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"tpl_generic","type":"form_template","properties":{"orientation":"vertical","fillMaxSize":true,"padding":{"all":16}},"components":[{"id":"header","type":"stack_component","elements":[{"id":"title","type":"text","properties":{"text":"Welcome","fontSize":20,"spans":[{"text":{"${DOLLAR}context":"appVersion"},"color":"#111111","fontWeight":700,"underline":true}],"leading":[{"type":"icon","properties":{"url":"https://backend.example/assets/info.png","fallbackUrl":"https://backend.example/assets/fallback.png","size":16,"spacing":{"start":1,"top":2,"end":3,"bottom":4}}}],"trailing":[{"type":"divider","properties":{"orientation":"vertical","height":20,"thickness":1,"spacing":{"start":5,"top":6,"end":7,"bottom":8}}}]},"actions":{"onClick":{"type":"navigate","destination":{"screenId":"details","templateId":"tpl_details","templateType":"form_template","endpoint":"/details"}}}},{"id":"hero","type":"image","properties":{"url":"https://example.com/hero.png","width":320,"height":180,"contentScale":"crop","leading":[{"type":"icon","properties":{"url":"https://backend.example/assets/image.png","fallbackUrl":"https://backend.example/assets/fallback.png","size":18}}],"trailing":[{"type":"icon","properties":{"url":"https://backend.example/assets/open.png","fallbackUrl":"https://backend.example/assets/fallback.png","size":18}}]}},{"id":"action_icon","type":"icon","properties":{"url":"https://backend.example/assets/settings.png","fallbackUrl":"https://backend.example/assets/fallback.png","size":24,"color":"#333333","semanticRole":"toolbar"}}]},{"id":"form","type":"stack_component","sections":[{"id":"identity","type":"stack_section","elements":[{"id":"field_a","type":"input","properties":{"keyboardType":"text","maxLength":40,"placeholder":"Field A"},"validation":{"required":true},"binding":{"key":"fieldA"}},{"id":"field_b","type":"input","properties":{"keyboardType":"text","maxLength":80},"validation":{"pattern":"^[^@]+@[^@]+\\.[^@]+$"},"binding":{"key":"fieldB"}}]},{"id":"actions","type":"stack_section","groups":[{"id":"primary_group","type":"stack_group","properties":{"orientation":"horizontal","padding":{"horizontal":8},"fillMaxWidth":true},"elements":[{"id":"submit","type":"button","properties":{"text":"Submit","fillMaxWidth":true,"trailing":[{"type":"icon","properties":{"url":"https://backend.example/assets/arrow-forward.png","fallbackUrl":"https://backend.example/assets/fallback.png","size":20}}]},"actions":{"onClick":{"type":"request","payload":{"method":"POST","endpoint":"/submit","authentication":"NONE","validate":true,"body":{"fieldA":{"${DOLLAR}binding":"fieldA"},"fieldB":{"${DOLLAR}binding":"fieldB"},"environmentId":{"${DOLLAR}context":"deviceId"},"previous":{"${DOLLAR}state":"referenceId"}},"responseMode":"destination","retain":{"referenceId":"data.referenceId"}}}}}]}]}]}]},"theme":{"theme":"light","statusBar":"transparent","properties":{"density":"compact"}}},"traceId":"trace-generic"}"""

    val GENERIC_RESPONSE_WITH_DESTINATION_JSON = """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"screen_current","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"tpl_current","type":"form_template","components":[]},"destination":{"screenId":"screen_next","templateId":"tpl_next","templateType":"form_template","endpoint":"/next"}},"traceId":"trace-destination"}"""

    val GENERIC_RESPONSE_PAYLOAD_JSON = """{"status":200,"code":"DATA","message":"Payload","data":{"referenceId":"ref-123","count":3,"enabled":true,"items":["a","b"],"nested":{"value":"x"}},"traceId":"trace-payload"}"""

    val GENERIC_ERROR_JSON = """{"status":422,"code":"VALIDATION_ERROR","message":"Invalid input","data":{"field":"fieldB","reason":"invalid"},"traceId":"trace-error"}"""


    val GENERIC_PHASE1_SCREEN_JSON = """{
  "status": 200,
  "code": "SUCCESS",
  "message": "OK",
  "data": {
    "screenId": "phase1_generic",
    "schemaVersion": "3.0.0",
    "targetApp": "PARTNER",
    "initialBindings": {
      "query": "",
      "selectedId": "IN"
    },
    "template": {
      "id": "tpl_phase1",
      "type": "form_template",
      "properties": {
        "scrollable": true
      },
      "components": [
        {
          "id": "surface",
          "type": "stack_component",
          "properties": {
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
          "elements": [
            {
              "id": "input",
              "type": "input",
              "properties": {
                "singleLine": true,
                "inputFilter": "digits",
                "placeholderColor": "#94A3B8",
                "height": 44,
                "validationStyle": {
                  "background": {
                    "color": "#FFF1F2"
                  }
                }
              },
              "binding": {
                "key": "phone"
              },
              "validation": {
                "required": true,
                "validator": {
                  "key": "phone_number",
                  "arguments": {
                    "region": {
                      "${DOLLAR}binding": "selectedId"
                    }
                  }
                },
                "message": "Invalid"
              }
            },
            {
              "id": "button",
              "type": "button",
              "properties": {
                "text": "Continue",
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
                    "endpoint": "/submit"
                  }
                }
              }
            },
            {
              "id": "divider",
              "type": "divider",
              "properties": {
                "orientation": "vertical",
                "height": 28,
                "thickness": 1,
                "color": "#D1D5DB",
                "spacing": {
                  "start": 8
                }
              }
            },
            {
              "id": "label",
              "type": "text",
              "properties": {
                "text": "Label",
                "weight": 1,
                "minHeight": 44,
                "maxLines": 1,
                "overflow": "ellipsis",
                "leading": [
                  {
                    "type": "image",
                    "properties": {
                      "source": {
                        "type": "resource",
                        "value": "images/logo"
                      },
                      "width": 24,
                      "height": 18,
                      "contentScale": "fit",
                      "spacing": {
                        "end": 8
                      }
                    }
                  }
                ]
              }
            }
          ],
          "sections": [
            {
              "id": "list",
              "type": "list_section",
              "properties": {
                "fillMaxWidth": true,
                "weight": 1,
                "source": {
                  "type": "catalog",
                  "key": "countries"
                },
                "filter": {
                  "binding": "query",
                  "fields": [
                    "name",
                    "dialCode"
                  ],
                  "caseInsensitive": true
                },
                "selection": {
                  "mode": "single",
                  "itemKey": "id",
                  "selectedBinding": "selectedId",
                  "result": {
                    "selectedId": "id"
                  },
                  "dismissOnSelect": true
                },
                "itemSeparator": {
                  "type": "divider",
                  "orientation": "horizontal",
                  "thickness": 1,
                  "color": "#EDF1F5"
                },
                "emptySectionId": "list_empty"
              },
              "groups": [
                {
                  "id": "row",
                  "type": "stack_group",
                  "properties": {
                    "minHeight": 44,
                    "selectedStyle": {
                      "background": {
                        "color": "#DCEBFC"
                      }
                    }
                  },
                  "elements": [
                    {
                      "id": "asset",
                      "type": "image",
                      "properties": {
                        "source": {
                          "type": "resource",
                          "value": {
                            "${DOLLAR}item": "asset"
                          }
                        }
                      }
                    },
                    {
                      "id": "selected",
                      "type": "icon",
                      "properties": {
                        "source": {
                          "type": "resource",
                          "value": "icons/check"
                        },
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
              "id": "list_empty",
              "type": "stack_section",
              "groups": [
                {
                  "id": "empty",
                  "type": "stack_group",
                  "elements": [
                    {
                      "id": "empty_text",
                      "type": "text",
                      "properties": {
                        "text": "Empty"
                      }
                    }
                  ]
                }
              ]
            }
          ]
        }
      ]
    },
    "presentations": [
      {
        "id": "picker",
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
              "top": 8
            }
          }
        },
        "initialBindings": {
          "search": ""
        },
        "components": [
          {
            "id": "picker_content",
            "type": "stack_component"
          }
        ]
      }
    ]
  }
}"""

}
