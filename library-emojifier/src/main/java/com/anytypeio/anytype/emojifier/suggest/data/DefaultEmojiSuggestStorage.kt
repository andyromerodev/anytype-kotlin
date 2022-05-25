package com.anytypeio.anytype.emojifier.suggest.data

import android.content.Context
import com.anytypeio.anytype.core_utils.ext.getJsonDataFromAsset
import com.anytypeio.anytype.emojifier.Emojifier.Config.EMOJI_FILE
import com.anytypeio.anytype.emojifier.suggest.model.EmojiModel
import com.anytypeio.anytype.emojifier.suggest.model.EmojiSuggest
import com.google.gson.Gson

class DefaultEmojiSuggestStorage(
    private val context: Context,
    private val gson: Gson
) : EmojiSuggestStorage {

    override suspend fun fetch(): List<EmojiSuggest> {
        val json = context.getJsonDataFromAsset(EMOJI_FILE)
        return if (json != null) {
            gson.fromJson(json, Array<EmojiModel>::class.java).toList()
        } else {
            emptyList()
        }
    }
}