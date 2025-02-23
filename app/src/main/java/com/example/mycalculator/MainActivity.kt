package com.example.mycalculator

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import net.objecthunter.exp4j.ExpressionBuilder

class MainActivity : AppCompatActivity() {
    private lateinit var mathOperation: TextView
    private lateinit var resultText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mathOperation = findViewById(R.id.math_operation)
        resultText = findViewById(R.id.result_text)

        setNumberListeners()
        setOperationListeners()
    }

    private fun setNumberListeners() {
        val numberButtons = listOf(
            R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
            R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9, R.id.dot_btn
        )

        numberButtons.forEach { id ->
            findViewById<TextView>(id).setOnClickListener {
                mathOperation.append((it as TextView).text)
            }
        }
    }

    private fun setOperationListeners() {
        val operationButtons = listOf(
            R.id.plus_btn, R.id.minus_btn, R.id.mult_btn, R.id.div_btn,
            R.id.lsk_btn, R.id.rsk_btn
        )

        operationButtons.forEach { id ->
            findViewById<TextView>(id).setOnClickListener {
                mathOperation.append((it as TextView).text)
            }
        }

        findViewById<TextView>(R.id.ac_btn).setOnClickListener {
            mathOperation.text = ""
            resultText.text = ""
        }

        findViewById<TextView>(R.id.back_btn).setOnClickListener {
            val text = mathOperation.text.toString()
            if (text.isNotEmpty()) {
                mathOperation.text = text.substring(0, text.length - 1)
            }
        }

        findViewById<TextView>(R.id.equal_btn).setOnClickListener {
            try {
                val expression = ExpressionBuilder(mathOperation.text.toString()).build()
                val result = expression.evaluate()
                resultText.text = result.toString()
            } catch (e: Exception) {
                resultText.text = "Error"
            }
        }
    }
}
