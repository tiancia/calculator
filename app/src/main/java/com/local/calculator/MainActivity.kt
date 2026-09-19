package com.local.calculator

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var input: CalculatorInput
    private lateinit var expressionView: TextView
    private lateinit var resultView: TextView
    private lateinit var expressionScroll: HorizontalScrollView
    private lateinit var resultScroll: HorizontalScrollView
    private val ink = Color.rgb(38, 39, 43)
    private val accent = Color.rgb(244, 119, 40)
    private val muted = Color.rgb(130, 132, 137)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        input = CalculatorInput(
            savedInstanceState?.getString("expression").orEmpty(),
            savedInstanceState?.getBoolean("confirmed") ?: false
        )
        buildScreen()
        render()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("expression", input.expression)
        outState.putBoolean("confirmed", input.confirmed)
        super.onSaveInstanceState(outState)
    }

    private fun buildScreen() {
        val page = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(Color.rgb(250, 250, 248))
            clipToPadding = false
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(16), dp(20), dp(12))
            layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
        page.addView(content, ScrollView.LayoutParams(-1, -1))
        page.setOnApplyWindowInsetsListener { view, insets ->
            if (Build.VERSION.SDK_INT >= 30) {
                val bars = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            } else {
                @Suppress("DEPRECATION")
                view.setPadding(insets.systemWindowInsetLeft, insets.systemWindowInsetTop,
                    insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
            }
            insets
        }
        content.addView(label(getString(R.string.app_name), 22f, ink).apply {
            setTypeface(typeface, Typeface.BOLD)
        })
        content.addView(label(getString(R.string.live_hint), 13f, muted).apply {
            setPadding(0, dp(8), 0, 0)
        })

        val display = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.BOTTOM
            minimumHeight = dp(210)
            setPadding(0, dp(28), 0, dp(28))
        }
        content.addView(display, LinearLayout.LayoutParams(-1, 0, 1f))
        expressionView = label("0", 34f, ink).apply {
            gravity = Gravity.END
            setSingleLine(true)
            contentDescription = getString(R.string.expression_description)
        }
        expressionScroll = horizontalLine(expressionView)
        display.addView(expressionScroll, LinearLayout.LayoutParams(-1, dp(64)))
        resultView = label("", 40f, accent).apply {
            gravity = Gravity.END
            setSingleLine(true)
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        }
        resultScroll = horizontalLine(resultView)
        display.addView(resultScroll, LinearLayout.LayoutParams(-1, dp(64)))

        content.addView(View(this).apply { setBackgroundColor(Color.rgb(232, 232, 229)) },
            LinearLayout.LayoutParams(-1, dp(1)).apply { bottomMargin = dp(16) })
        val rows = listOf(
            listOf("AC", "⌫", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("±", "0", ".", "=")
        )
        rows.forEach { keys ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            content.addView(row, LinearLayout.LayoutParams(-1, dp(72)))
            keys.forEach { key ->
                val operation = key in listOf("AC", "⌫", "%", "÷", "×", "-", "+", "±")
                val fill = when {
                    key == "=" -> accent
                    operation -> Color.rgb(255, 241, 230)
                    else -> Color.rgb(241, 242, 240)
                }
                val button = Button(this).apply {
                    text = if (key == "-") "−" else key
                    textSize = if (key == "AC") 22f else 28f
                    isAllCaps = false
                    setTextColor(if (key == "=") Color.WHITE else if (operation) accent else ink)
                    setPadding(0, 0, 0, 0)
                    minWidth = 0
                    minimumWidth = 0
                    minHeight = dp(48)
                    background = RippleDrawable(ColorStateList.valueOf(0x18000000),
                        GradientDrawable().apply { setColor(fill); cornerRadius = dp(24).toFloat() }, null)
                    contentDescription = when (key) {
                        "AC" -> getString(R.string.clear_description)
                        "⌫" -> getString(R.string.delete_description)
                        "%" -> getString(R.string.percent_description)
                        "±" -> getString(R.string.sign_description)
                        "=" -> getString(R.string.equals_description)
                        else -> text
                    }
                    setOnClickListener {
                        if (!input.press(key)) Toast.makeText(this@MainActivity, R.string.too_long, Toast.LENGTH_SHORT).show()
                        render(key == "=")
                    }
                    if (key == "⌫") setOnLongClickListener {
                        input.press("AC")
                        render()
                        true
                    }
                }
                row.addView(button, LinearLayout.LayoutParams(0, -1, 1f).apply { setMargins(dp(4), dp(4), dp(4), dp(4)) })
            }
        }
        setContentView(page)
        page.requestApplyInsets()
    }

    private fun horizontalLine(textView: TextView) = HorizontalScrollView(this).apply {
        isFillViewport = true
        isHorizontalScrollBarEnabled = false
        addView(textView, HorizontalScrollView.LayoutParams(-2, -1))
    }

    private fun render(showIncomplete: Boolean = false) {
        expressionView.text = input.expression.ifEmpty { "0" }.replace('-', '−')
        val result = Calculator.evaluate(input.expression)
        resultView.setTextColor(if (result is Calculator.Result.Value) accent else muted)
        resultView.textSize = if (result is Calculator.Result.Value) 40f else 18f
        resultView.text = when (result) {
            is Calculator.Result.Value -> "= ${result.text.replace('-', '−')}"
            Calculator.Result.Incomplete -> if (showIncomplete) getString(R.string.incomplete) else ""
            Calculator.Result.DivisionByZero -> getString(R.string.divide_by_zero)
            Calculator.Result.Invalid -> getString(R.string.invalid_expression)
        }
        resultView.contentDescription = getString(R.string.result_description, resultView.text)
        expressionScroll.post { expressionScroll.fullScroll(View.FOCUS_RIGHT) }
        // Keep the beginning/sign of a long result visible; swipe to read the rest.
        resultScroll.post { resultScroll.scrollTo(0, 0) }
    }

    private fun label(value: String, size: Float, color: Int) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        gravity = Gravity.CENTER_VERTICAL
        includeFontPadding = false
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density + 0.5f).toInt()
}
