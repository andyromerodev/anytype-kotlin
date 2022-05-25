package com.anytypeio.anytype.core_ui.features.editor

import com.anytypeio.anytype.presentation.editor.editor.model.UiBlock


interface TurnIntoActionReceiver {
    /**
     * @param target id of the target block
     * @param block new block's type
     */
    fun onTurnIntoBlockClicked(target: String, block: UiBlock)

    /**
     * @param block new block's type
     */
    fun onTurnIntoMultiSelectBlockClicked(block: UiBlock)
}