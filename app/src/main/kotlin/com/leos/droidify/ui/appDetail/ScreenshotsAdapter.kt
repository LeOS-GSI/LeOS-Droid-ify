package com.leos.droidify.ui.appDetail

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import com.google.android.material.R as MaterialR
import com.google.android.material.imageview.ShapeableImageView
import com.leos.core.common.R.dimen as dimenRes
import com.leos.core.common.extension.aspectRatio
import com.leos.core.common.extension.authentication
import com.leos.core.common.extension.camera
import com.leos.core.common.extension.dp
import com.leos.core.common.extension.getColorFromAttr
import com.leos.core.common.extension.selectableBackground
import com.leos.core.domain.Product
import com.leos.core.domain.Repository
import com.leos.droidify.graphics.PaddingDrawable
import com.leos.droidify.utility.extension.ImageUtils.url
import com.leos.droidify.widget.StableRecyclerAdapter

class ScreenshotsAdapter(private val onClick: (Product.Screenshot, ImageView) -> Unit) :
    StableRecyclerAdapter<ScreenshotsAdapter.ViewType, RecyclerView.ViewHolder>() {
    enum class ViewType { SCREENSHOT }

    private val items = mutableListOf<Item.ScreenshotItem>()

    private class ViewHolder(context: Context) :
        RecyclerView.ViewHolder(FrameLayout(context)) {
        val image: ShapeableImageView = object : ShapeableImageView(context) {
            override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                setMeasuredDimension(measuredWidth, measuredHeight)
            }
        }
        val placeholderColor = context.getColorFromAttr(MaterialR.attr.colorPrimaryContainer)
        val radius = context.resources.getDimension(dimenRes.shape_small_corner)

        val imageShapeModel = image.shapeAppearanceModel.toBuilder()
            .setAllCornerSizes(radius)
            .build()
        val cameraIcon = context.camera
            .apply { setTintList(placeholderColor) }
        val placeholder: Drawable = PaddingDrawable(cameraIcon, 3f, context.aspectRatio)

        init {
            with(image) {
                shapeAppearanceModel = imageShapeModel
                background = context.selectableBackground
                isFocusable = true
            }
            with(itemView as FrameLayout) {
                layoutParams = RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.WRAP_CONTENT,
                    150.dp
                ).apply {
                    marginStart = radius.toInt()
                    marginEnd = radius.toInt()
                }
                foregroundGravity = Gravity.CENTER
                addView(image)
            }
        }
    }

    fun setScreenshots(
        repository: Repository,
        packageName: String,
        screenshots: List<Product.Screenshot>
    ) {
        items.clear()
        items += screenshots.map { Item.ScreenshotItem(repository, packageName, it) }
        notifyItemRangeInserted(0, screenshots.size)
    }

    override val viewTypeClass: Class<ViewType>
        get() = ViewType::class.java

    override fun getItemEnumViewType(position: Int): ViewType {
        return ViewType.SCREENSHOT
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: ViewType
    ): RecyclerView.ViewHolder {
        return ViewHolder(parent.context).apply {
            image.setOnClickListener { onClick(items[absoluteAdapterPosition].screenshot, it as ImageView) }
        }
    }

    override fun getItemDescriptor(position: Int): String = items[position].descriptor
    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as ViewHolder
        val item = items[position]
        holder.image.load(
            item.screenshot.url(item.repository, item.packageName)
        ) {
            scale(Scale.FILL)
            placeholder(holder.placeholder)
            error(holder.placeholder)
            authentication(item.repository.authentication)
        }
    }

    private sealed class Item {
        abstract val descriptor: String

        class ScreenshotItem(
            val repository: Repository,
            val packageName: String,
            val screenshot: Product.Screenshot
        ) : Item() {
            override val descriptor: String
                get() = "screenshot.${repository.id}.${screenshot.identifier}"
        }
    }
}
