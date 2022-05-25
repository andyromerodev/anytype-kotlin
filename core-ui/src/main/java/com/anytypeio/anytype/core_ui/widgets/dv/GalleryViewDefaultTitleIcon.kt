package com.anytypeio.anytype.core_ui.widgets.dv

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.WidgetGalleryViewDefaultTitleIconBinding
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import timber.log.Timber

class GalleryViewDefaultTitleIcon @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    val binding = WidgetGalleryViewDefaultTitleIconBinding.inflate(
        LayoutInflater.from(context), this
    )

    fun bind(icon: ObjectIcon) = with(binding) {
        when (icon) {
            is ObjectIcon.Basic.Emoji -> {
                ivIconImage.setImageDrawable(null)
                ivIconImage.gone()
                tvAvatar.text = null
                tvAvatar.gone()
                setEmoji(icon.unicode)
            }
            is ObjectIcon.Basic.Image -> {
                ivIconEmoji.setImageDrawable(null)
                ivIconEmoji.gone()
                tvAvatar.text = null
                tvAvatar.gone()
                setImage(icon.hash)
            }
            is ObjectIcon.Profile.Image -> {
                ivIconEmoji.setImageDrawable(null)
                ivIconEmoji.gone()
                tvAvatar.text = null
                tvAvatar.gone()
                setCircularImage(icon.hash)
            }
            is ObjectIcon.Profile.Avatar -> {
                ivIconEmoji.setImageDrawable(null)
                ivIconEmoji.gone()
                ivIconImage.setImageDrawable(null)
                ivIconImage.gone()
                tvAvatar.text = if (icon.name.isNotEmpty())
                    icon.name.first().toString()
                else
                    resources.getString(R.string.u)
                tvAvatar.visible()
            }
            is ObjectIcon.Task -> {
                ivIconEmoji.setImageDrawable(null)
                ivIconEmoji.gone()
                tvAvatar.text = null
                tvAvatar.gone()
                ivIconImage.visible()
                if (icon.isChecked) {
                    ivIconImage.setImageResource(R.drawable.ic_gallery_view_task_checked)
                } else {
                    ivIconImage.setImageResource(R.drawable.ic_gallery_view_task_unchecked)
                }
            }
            else -> {
                Timber.e("Ignoring icon: $icon")
            }
        }
    }

    private fun setEmoji(emoji: String) = with(binding) {
        if (emoji.isNotBlank()) {
            try {
                Glide
                    .with(ivIconEmoji)
                    .load(Emojifier.uri(emoji))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(ivIconEmoji)
            } catch (e: Throwable) {
                Timber.e(e, "Error while setting emoji icon for: $emoji")
            }
        } else {
            ivIconEmoji.setImageDrawable(null)
        }
        ivIconEmoji.visible()
    }

    private fun setImage(image: Url) = with(binding) {
        if (image.isNotBlank()) {
            Glide
                .with(ivIconImage)
                .load(image)
                .centerCrop()
                .into(ivIconImage)
        } else {
            ivIconImage.setImageDrawable(null)
        }
        ivIconImage.visible()
    }

    private fun setCircularImage(image: Url) = with(binding) {
        if (image.isNotBlank()) {
            Glide
                .with(ivIconEmoji)
                .load(image)
                .circleCrop()
                .into(ivIconImage)
        } else {
            ivIconImage.setImageDrawable(null)
        }
        ivIconImage.visible()
    }
}