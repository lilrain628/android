package com.example.mycalculator

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import net.objecthunter.exp4j.ExpressionBuilder

class MainActivity : AppCompatActivity() {     //управляет интерфейсом и логикой калькулятора.
    private lateinit var mathOperation: TextView  //отображает текущее матем. выраж.
    private lateinit var resultText: TextView   //результат вычисления.

    override fun onCreate(savedInstanceState: Bundle?) {  //инициализация интерфейса
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mathOperation = findViewById(R.id.math_operation)  //элементы интрефейса по идентификаторам
        resultText = findViewById(R.id.result_text)

        setNumberListeners()  //методы для обработки собфтий
        setOperationListeners()
    }

    private fun setNumberListeners() {  //обраьотчик событи для цифр и точек.  созд. идентификатор кнопок
        val numberButtons = listOf(
            R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
            R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9, R.id.dot_btn
        )

        numberButtons.forEach { id ->
            findViewById<TextView>(id).setOnClickListener {  //для каждой конпки созд. обработчик событий
                mathOperation.append((it as TextView).text)    //добав. в MATCHOPERATION
            }
        }
    }

    private fun setOperationListeners() {   //обработчик чобфтий для спец. кнопок
        val operationButtons = listOf(
            R.id.plus_btn, R.id.minus_btn, R.id.mult_btn, R.id.div_btn,
            R.id.lsk_btn, R.id.rsk_btn
        )

        operationButtons.forEach { id ->
            findViewById<TextView>(id).setOnClickListener {
                mathOperation.append((it as TextView).text)
            }
        }

        findViewById<TextView>(R.id.ac_btn).setOnClickListener {  //отчистка строки
            mathOperation.text = ""
            resultText.text = ""
        }

        findViewById<TextView>(R.id.back_btn).setOnClickListener {  //кнопка удаления  удаления
            val text = mathOperation.text.toString()
            if (text.isNotEmpty()) {
                mathOperation.text = text.substring(0, text.length - 1)
            }
        }

        findViewById<TextView>(R.id.equal_btn).setOnClickListener {  //кнопка вычислений
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
