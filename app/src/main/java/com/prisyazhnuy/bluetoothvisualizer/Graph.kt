package com.prisyazhnuy.bluetoothvisualizer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import kotlin.math.sqrt

class Graph : View {

    private var _exampleString: String? = "Test" // TODO: use a default from R.string...
    private var _exampleColor: Int = Color.RED // TODO: use a default from R.color...
    private var _exampleDimension: Float = 0f // TODO: use a default from R.dimen...

    private var textPaint: TextPaint? = null
    private var textWidth: Float = 0f
    private var textHeight: Float = 0f
    private var circlePaint: Paint? = null
    private val linePaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 5f
        textSize = 25f
    }

    private var _data: List<String>? = null//listOf("Circle1", "Circle2", "Circle3", "Circle4", "Circle5")

    var data: List<String>?
        get() = _data
        set(value) {
            _data = value
            invalidate()
        }
    /**
     * The text to draw
     */
    var exampleString: String?
        get() = _exampleString
        set(value) {
            _exampleString = value
//            invalidateTextPaintAndMeasurements()
        }

    /**
     * The font color
     */
    var exampleColor: Int
        get() = _exampleColor
        set(value) {
            _exampleColor = value
//            invalidateTextPaintAndMeasurements()
        }

    /**
     * In the example view, this dimension is the font size.
     */
    var exampleDimension: Float
        get() = _exampleDimension
        set(value) {
            _exampleDimension = value
//            invalidateTextPaintAndMeasurements()
        }

    /**
     * In the example view, this drawable is drawn above the text.
     */
    var exampleDrawable: Drawable? = null

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
                attrs, R.styleable.Graph, defStyle, 0)

        _exampleString = a.getString(
                R.styleable.Graph_exampleString)
        _exampleColor = a.getColor(
                R.styleable.Graph_exampleColor,
                exampleColor)
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        _exampleDimension = a.getDimension(
                R.styleable.Graph_exampleDimension,
                exampleDimension)

        if (a.hasValue(R.styleable.Graph_exampleDrawable)) {
            exampleDrawable = a.getDrawable(
                    R.styleable.Graph_exampleDrawable)
            exampleDrawable?.callback = this
        }

        a.recycle()

        // Set up a default TextPaint object
        textPaint = TextPaint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            textAlign = Paint.Align.LEFT
        }

        circlePaint = Paint().apply {
            color = _exampleColor
        }

        // Update TextPaint and text measurements from attributes
//        invalidateTextPaintAndMeasurements()
    }

//    private fun invalidateTextPaintAndMeasurements() {
//        textPaint?.let {
//            it.textSize = exampleDimension
//            it.color = exampleColor
//            textWidth = it.measureText(exampleString)
//            textHeight = it.fontMetrics.bottom
//        }
//    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        val paddingLeft = paddingLeft
        val paddingTop = paddingTop
        val paddingRight = paddingRight
        val paddingBottom = paddingBottom

        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom


        val xC = contentWidth / 2f
        val yC = contentHeight / 2f

        data?.let {
            val distance = getDistanceBTWCircles(contentHeight / 4, it.size).toInt()


            it.forEach {
                val xD = Math.random().toFloat() * contentWidth
                val yD = Math.random().toFloat() * contentHeight
                canvas.drawText(it, xD, yD - 50f, linePaint)
                canvas.drawLine(xD, yD, xC, yC, linePaint)
                canvas.drawCircle(xD, yD, 40f, circlePaint)

//                with(getCircleCenter(contentWidth / 2f, contentHeight / 2f, contentHeight / 4, xD, yD, distance)) {
//                    xD = first
//                    yD = second
//                    canvas.drawCircle(xD, xD, 40f, circlePaint)
//                    canvas.drawLine(xD, yD, xC, yC, linePaint)
//                }
            }
        }

        canvas.drawCircle(contentWidth / 2f, contentHeight / 2f, 50f, circlePaint)



        exampleString?.let {
            // Draw the text.
            canvas.drawText(it,
                    paddingLeft + (contentWidth - textWidth) / 2,
                    paddingTop + (contentHeight + textHeight) / 2,
                    textPaint)
        }

        // Draw the example drawable on top of the text.
        exampleDrawable?.let {
            it.setBounds(paddingLeft, paddingTop,
                    paddingLeft + contentWidth, paddingTop + contentHeight)
            it.draw(canvas)
        }
    }

    private fun getDistanceBTWCircles(r: Int, count: Int): Float {
        val circleLength = 2 * r * Math.PI
        return (circleLength / count).toFloat()
    }

    private fun getCircleCenter(xC: Float, yC: Float, rC: Int, xD: Float, yD: Float, rD: Int): Pair<Float, Float> {
        val c = (rD * rD - Math.pow(((xD - xC).toDouble()), 2.0) - Math.pow(((yD - yC).toDouble()), 2.0) - rC * rC) / -2
        val a = Math.pow(((yD - yC).toDouble()), 2.0) + Math.pow(((xD - xC).toDouble()), 2.0)
        val b = -2 * (yD - yC) * c
        val e = c * c - 4 * rC * rC * Math.pow(((xD - xC).toDouble()), 2.0)
        val D = b * b - 4 * a * e

        val y = (-b + sqrt(D)) / (2 * a)
        val x = (c - y * (yD - yC)) / (xD - xC)
        return Pair(x.toFloat(), y.toFloat())
    }
}
