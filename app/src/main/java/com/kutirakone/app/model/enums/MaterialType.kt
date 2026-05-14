package com.kutirakone.app.model.enums

import androidx.compose.ui.graphics.Color
import com.kutirakone.app.ui.theme.BlendColor
import com.kutirakone.app.ui.theme.CottonColor
import com.kutirakone.app.ui.theme.JuteColor
import com.kutirakone.app.ui.theme.SilkColor
import com.kutirakone.app.ui.theme.SyntheticColor
import com.kutirakone.app.ui.theme.WoolColor

enum class MaterialType(val displayName: String, val color: Color) {
    SILK("Silk", SilkColor),
    COTTON("Cotton", CottonColor),
    WOOL("Wool", WoolColor),
    SYNTHETIC("Synthetic", SyntheticColor),
    BLEND("Blend", BlendColor),
    JUTE("Jute", JuteColor)
}
