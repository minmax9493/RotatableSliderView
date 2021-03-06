package com.example.rotatableslider


import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import kotlin.math.max


/**
 * Created by bo on 7/2/18.
 * Modified by Murodjon
 */

class RotatableSlider : View {

    private var mThumbRadius: Int = 0
    private var mTrackBgThickness: Int = 0
    private var mTrackFgThickness: Int = 0
    private var mThumbFgPaint: Paint? = null
    private var mTrackBgPaint: Paint? = null
    private var mTrackFgPaint: Paint? = null
    private var mTrackRect: RectF? = null
    private var mListener: OnProgressChangeListener? = null
    private var mProgress = 0.0f
    private var mHorizontal:Boolean = false

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs, defStyleAttr)
    }

    fun setThumbColor(color: Int) {
        mThumbFgPaint!!.color = color
        invalidate()
    }

    fun setTrackFgColor(color: Int) {
        mTrackFgPaint!!.color = color
        invalidate()
    }

    fun setTrackBgColor(color: Int) {
        mTrackBgPaint!!.color = color
        invalidate()
    }

    fun setThumbRadiusPx(radiusPx: Int) {
        mThumbRadius = radiusPx
        invalidate()
    }

    fun setTrackFgThicknessPx(heightPx: Int) {
        mTrackFgThickness = heightPx
        invalidate()
    }

    fun setTrackBgThicknessPx(heightPx: Int) {
        mTrackBgThickness = heightPx
        invalidate()
    }

    @JvmOverloads
    fun setProgress(progress: Float, notifyListener: Boolean = false) {
        onProgressChanged(progress, notifyListener)
    }

    fun setOnSliderProgressChangeListener(listener: OnProgressChangeListener) {
        mListener = listener
    }

    private fun init(attrs: AttributeSet?, defStyleAttr: Int) {
        // to support non-touchable environment
        isFocusable = true

        val colorDefaultBg = resolveAttrColor("colorControlNormal", COLOR_BG)
        val colorDefaultFg = resolveAttrColor("colorControlActivated", COLOR_FG)

        mThumbFgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mThumbFgPaint!!.style = Paint.Style.FILL
        mThumbFgPaint!!.color = colorDefaultFg

        mTrackBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mTrackBgPaint!!.style = Paint.Style.FILL
        mTrackBgPaint!!.color = colorDefaultBg

        mTrackFgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mTrackFgPaint!!.style = Paint.Style.FILL
        mTrackFgPaint!!.color = colorDefaultFg

        mTrackRect = RectF()

        val dm = resources.displayMetrics
        mThumbRadius =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, THUMB_RADIUS_FG.toFloat(), dm)
                .toInt()
        mTrackBgThickness =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TRACK_HEIGHT_BG.toFloat(), dm)
                .toInt()
        mTrackFgThickness =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TRACK_HEIGHT_FG.toFloat(), dm)
                .toInt()

        if (attrs != null) {
            val arr =
                context.obtainStyledAttributes(attrs, R.styleable.RotatableSlider, defStyleAttr, 0)
            val thumbColor =
                arr.getColor(R.styleable.RotatableSlider_rs_thumb_color, mThumbFgPaint!!.color)
            mThumbFgPaint!!.color = thumbColor

            val trackColor =
                arr.getColor(R.styleable.RotatableSlider_rs_track_fg_color, mTrackFgPaint!!.color)
            mTrackFgPaint!!.color = trackColor

            val trackBgColor =
                arr.getColor(R.styleable.RotatableSlider_rs_track_bg_color, mTrackBgPaint!!.color)
            mTrackBgPaint!!.color = trackBgColor

            mThumbRadius = arr.getDimensionPixelSize(
                R.styleable.RotatableSlider_rs_thumb_radius,
                mThumbRadius
            )
            mTrackFgThickness = arr.getDimensionPixelSize(
                R.styleable.RotatableSlider_rs_track_fg_thickness,
                mTrackFgThickness
            )
            mTrackBgThickness = arr.getDimensionPixelSize(
                R.styleable.RotatableSlider_rs_track_bg_thickness,
                mTrackBgThickness
            )
            mHorizontal = arr.getBoolean(R.styleable.RotatableSlider_rs_track_horizontal, false)

            arr.recycle()
        }
    }

    private fun resolveAttrColor(attrName: String, defaultColor: Int): Int {
        val packageName = context.packageName
        val attrRes = resources.getIdentifier(attrName, "attr", packageName)
        if (attrRes <= 0) {
            return defaultColor
        }
        val value = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(attrRes, value, true)
        return resources.getColor(value.resourceId)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            if (mProgress < 1f) {
                onProgressChanged(mProgress + 0.02f, true)
                return true
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            if (mProgress > 0f) {
                onProgressChanged(mProgress - 0.02f, true)
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun onProgressChanged(progress: Float, notifyChange: Boolean) {
        mProgress = progress
        if (mProgress < 0) {
            mProgress = 0f
        } else if (mProgress > 1f) {
            mProgress = 1f
        }
        invalidate()
        if (notifyChange && mListener != null) {
            mListener!!.onProgress(mProgress)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }

        if (mHorizontal){
            val x = event.x
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    val width = width - paddingRight - paddingLeft - 2 * mThumbRadius
                    onProgressChanged(1 - x / width, true)
                }
            }
        }else{
            val y = event.y
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    val height = height - paddingTop - paddingBottom - 2 * mThumbRadius
                    onProgressChanged(1 - y / height, true)
                }
            }
        }

        return true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (mHorizontal){
            val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)

            val contentHeight = paddingBottom+paddingTop+2*mThumbRadius

            val heightMode = MeasureSpec.getMode(heightMeasureSpec)
            var height = MeasureSpec.getSize(heightMeasureSpec)
            if (heightMode != MeasureSpec.EXACTLY) {
                height = max(contentHeight, suggestedMinimumHeight)
            }
            setMeasuredDimension(width, height)
        }else{
            val height = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)

            val contentWidth = paddingLeft + mThumbRadius * 2 + paddingRight
            val widthMode = MeasureSpec.getMode(widthMeasureSpec)
            var width = MeasureSpec.getSize(widthMeasureSpec)
            if (widthMode != MeasureSpec.EXACTLY) {
                width = max(contentWidth, suggestedMinimumWidth)
            }
            setMeasuredDimension(width, height)
        }
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //this function draws background slider line
        drawBgTrackLine(canvas, mThumbRadius, mTrackBgThickness, 0, mTrackBgPaint, 1f)

        val trackPadding =
            if (mTrackBgThickness > mTrackFgThickness) mTrackBgThickness - mTrackFgThickness shr 1 else 0

        //this function draws slider line
        drawTrack(canvas, mThumbRadius, mTrackFgThickness, trackPadding, mTrackFgPaint, mProgress)

        if (mHorizontal){
            // draw bg thumb
            val width = width - paddingLeft - paddingRight - 2 * mThumbRadius - 2 * trackPadding
            val height = height - paddingTop - paddingBottom
            val leftOffset = width - mThumbRadius * 2 shr 1
            val topOffset = height - mThumbRadius * 2 shr 1

            //cX, xY, R
            canvas.drawCircle(
                paddingLeft.toFloat()+ mThumbRadius.toFloat() + (1-mProgress)*width + trackPadding.toFloat(),
                (paddingTop + topOffset + mThumbRadius).toFloat(),
                mThumbRadius.toFloat(),
                mThumbFgPaint!!
            )

            // startX, startY, stopX, stopY;
            mThumbFgPaint!!.strokeWidth = 10f
            canvas.drawLine(
                (paddingLeft + width/2 +mThumbRadius).toFloat(),
                (paddingTop + topOffset).toFloat(),
                (paddingLeft + width/2 + mThumbRadius).toFloat(),
                (paddingTop + topOffset + 2 * mThumbRadius).toFloat(),
                mThumbFgPaint!!
            )
//            canvas.drawLine(
//                (paddingLeft + leftOffset).toFloat(),
//                (paddingTop + height / 2 + mThumbRadius).toFloat(),
//                (paddingLeft + leftOffset + 2 * mThumbRadius).toFloat(),
//                (paddingTop + height / 2 + mThumbRadius).toFloat(),
//                mThumbFgPaint!!
//            )
        }else{
            // draw bg thumb
            val width = width - paddingLeft - paddingRight
            val height = height - paddingTop - paddingBottom - 2 * mThumbRadius - 2 * trackPadding
            val leftOffset = width - mThumbRadius * 2 shr 1

            //cX, xY, R
            canvas.drawCircle(
                (paddingLeft + leftOffset + mThumbRadius).toFloat(),
                paddingTop.toFloat() + mThumbRadius.toFloat() + (1 - mProgress) * height + trackPadding.toFloat(),
                mThumbRadius.toFloat(),
                mThumbFgPaint!!
            )

            // startX, startY, stopX, stopY;
            mThumbFgPaint!!.strokeWidth = 10f
            canvas.drawLine(
                (paddingLeft + leftOffset).toFloat(),
                (paddingTop + height / 2 + mThumbRadius).toFloat(),
                (paddingLeft + leftOffset + 2 * mThumbRadius).toFloat(),
                (paddingTop + height / 2 + mThumbRadius).toFloat(),
                mThumbFgPaint!!
            )
        }

    }

    private fun drawTrack(
        canvas: Canvas,
        thumbRadius: Int,
        trackThickness: Int,
        trackPadding: Int,
        trackPaint: Paint?,
        progress: Float
    ) {
        val trackRadius = trackThickness * 0.5f

        if (mHorizontal){
            val width = width - paddingLeft - paddingRight - 2 * thumbRadius
            val height = height - paddingTop - paddingBottom

            val trackTop = paddingTop + (height - trackThickness shr 1)
            val trackBottom = trackTop + trackThickness

            val trackLeft: Int
            if (progress > 0.5f) {
                trackLeft =
                    (paddingLeft.toFloat() + thumbRadius.toFloat() + (1 - progress) * width).toInt() + trackPadding

                mTrackRect!!.set(
                    trackLeft.toFloat(),
                    trackTop.toFloat(),
                    (paddingLeft + width / 2 + mThumbRadius).toFloat(),
                    trackBottom.toFloat()
                )
            } else {
                trackLeft =
                    (paddingLeft.toFloat() + thumbRadius.toFloat() + (1 - progress) * width).toInt() + trackPadding

                mTrackRect!!.set(
                    trackLeft.toFloat(),
                    trackTop.toFloat(),
                    (paddingLeft + width / 2 + mThumbRadius).toFloat(),
                    trackBottom.toFloat()
                )
            }
        }else{
            val width = width - paddingLeft - paddingRight
            val height = height - paddingTop - paddingBottom - 2 * thumbRadius

            val trackLeft = paddingLeft + (width - trackThickness shr 1)
            val trackRight = trackLeft + trackThickness

            val trackTop: Int
            if (progress > 0.5f) {
                trackTop =
                    (paddingTop.toFloat() + thumbRadius.toFloat() + (1 - progress) * height).toInt() + trackPadding

                mTrackRect!!.set(
                    trackLeft.toFloat(),
                    trackTop.toFloat(),
                    trackRight.toFloat(),
                    (paddingTop + height / 2 + mThumbRadius).toFloat()
                )
            } else {
                trackTop =
                    (paddingTop.toFloat() + thumbRadius.toFloat() + (1 - progress) * height).toInt() + trackPadding

                mTrackRect!!.set(
                    trackLeft.toFloat(),
                    (paddingTop + height / 2 + mThumbRadius).toFloat(),
                    trackRight.toFloat(),
                    trackTop.toFloat()
                )
            }
        }

        canvas.drawRoundRect(mTrackRect!!, trackRadius, trackRadius, trackPaint!!)
    }

    private fun drawBgTrackLine(
        canvas: Canvas,
        thumbRadius: Int,
        trackThickness: Int,
        trackPadding: Int,
        trackPaint: Paint?,
        progress: Float
    ) {
        val trackRadius = trackThickness * 0.5f

        if (mHorizontal){
            val height = height - paddingTop - paddingBottom
            val width = width - paddingLeft - paddingRight - 2 * thumbRadius

            val trackLeft = (paddingLeft + thumbRadius.toFloat() + (1-progress)*width).toInt() + trackPadding

            val trackTop = paddingTop.toFloat() + (height - trackThickness shr 1)

            val trackRight = getWidth() - paddingRight -thumbRadius -trackPadding

            val trackBottom = trackTop + trackThickness

            mTrackRect!!.set(
                trackLeft.toFloat(),
                trackTop,
                trackRight.toFloat(),
                trackBottom
            )
        }else{
            val width = width - paddingLeft - paddingRight
            val height = height - paddingTop - paddingBottom - 2 * thumbRadius

            val trackLeft = paddingLeft + (width - trackThickness shr 1)
            val trackTop =
                (paddingTop.toFloat() + thumbRadius.toFloat() + (1 - progress) * height).toInt() + trackPadding
            val trackRight = trackLeft + trackThickness
            val trackBottom = getHeight() - paddingBottom - thumbRadius - trackPadding
            mTrackRect!!.set(
                trackLeft.toFloat(),
                trackTop.toFloat(),
                trackRight.toFloat(),
                trackBottom.toFloat()
            )
        }
        canvas.drawRoundRect(mTrackRect!!, trackRadius, trackRadius, trackPaint!!)
    }

    interface OnProgressChangeListener {
        fun onProgress(progress: Float)
    }

    companion object {

        private val THUMB_RADIUS_FG = 6

        private val TRACK_HEIGHT_BG = 4
        private val TRACK_HEIGHT_FG = 2

        private val COLOR_BG = Color.parseColor("#dddfeb")
        private val COLOR_FG = Color.parseColor("#7da1ae")
    }
}
