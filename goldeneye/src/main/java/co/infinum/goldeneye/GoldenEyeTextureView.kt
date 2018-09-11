package co.infinum.goldeneye

import android.content.Context
import android.util.AttributeSet
import android.view.TextureView
import android.view.View
import org.w3c.dom.Text

class GoldenEyeTextureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    style: Int = 0
) : TextureView(context, attrs, style) {

    private var aspectRatio = 0f

    fun setAspectRatio(aspectRatio: Float) {
        this.aspectRatio = aspectRatio
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(width, width * 4/3)
    }
}