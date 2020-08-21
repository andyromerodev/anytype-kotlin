package com.agileburo.anytype.ui.page.modals.actions

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.bumptech.glide.Glide

class PictureBlockActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.Media.Picture

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun blockLayout() = R.layout.item_block_picture_preview
    override fun getBlock(): BlockView = block

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) {
        val item = block
        view.findViewById<ImageView>(R.id.image).apply {
            Glide.with(this).load(item.url).into(this)
        }
        setConstraints()
    }
}