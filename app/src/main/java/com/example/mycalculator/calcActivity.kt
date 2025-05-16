package com.example.mycalculator

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity
import net.objecthunter.exp4j.ExpressionBuilder

class calcActivity : AppCompatActivity() {
    lateinit var mathOperation: TextView     //contains link to textview
    lateinit var resultText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calc_activity)

        mathOperation = findViewById<View>(R.id.math_operation) as TextView //find elements in the xml
        resultText = findViewById<View>(R.id.result_text) as TextView

        findViewById<View>(R.id.btn_0).setOnClickListener { v: View? ->
            mathOperation.append("0")
        }
        findViewById<View>(R.id.btn_1).setOnClickListener { v: View? ->
            mathOperation.append("1")
        }
        findViewById<View>(R.id.btn_2).setOnClickListener { v: View? ->
            mathOperation.append("2")
        }
        findViewById<View>(R.id.btn_3).setOnClickListener { v: View? ->
            mathOperation.append("3")
        }
        findViewById<View>(R.id.btn_4).setOnClickListener { v: View? ->
            mathOperation.append("4")
        }
        findViewById<View>(R.id.btn_5).setOnClickListener { v: View? ->
            mathOperation.append("5")
        }
        findViewById<View>(R.id.btn_6).setOnClickListener { v: View? ->
            mathOperation.append("6")
        }
        findViewById<View>(R.id.btn_7).setOnClickListener { v: View? ->
            mathOperation.append("7")
        }
        findViewById<View>(R.id.btn_8).setOnClickListener { v: View? ->
            mathOperation.append("8")
        }
        findViewById<View>(R.id.btn_9).setOnClickListener { v: View? ->
            mathOperation.append("9")
        }

        findViewById<View>(R.id.dot_btn).setOnClickListener { v: View? ->
            mathOperation.append(".")
        }

        findViewById<View>(R.id.plus_btn).setOnClickListener { v: View? ->
            mathOperation.append("+")
        }
        findViewById<View>(R.id.minus_btn).setOnClickListener { v: View? ->
            mathOperation.append("-")
        }
        findViewById<View>(R.id.mult_btn).setOnClickListener { v: View? ->
            mathOperation.append("*")
        }
        findViewById<View>(R.id.div_btn).setOnClickListener { v: View? ->
            mathOperation.append("/")
        }


        findViewById<View>(R.id.ac_btn).setOnClickListener { v: View? ->
            mathOperation.text = ""
            resultText.text = ""
        }

        findViewById<View>(R.id.back_btn).setOnClickListener { v: View? ->  //Проверяем, есть ли хотя бы один символ (text.length > 0).
            // Если да, обрезаем строку, удаляя последний символ
            val text = mathOperation.text.toString()
            if (text.length > 0) {
                mathOperation.text = text.substring(0, text.length - 1)
            }
        }

        findViewById<View>(R.id.equal_btn).setOnClickListener { v: View? ->
            try {
//                val result = ExpressionBuilder(mathOperation.text.toString()).build().evaluate()
//                val lenght = mathOperation.text.toString().length
//                resultText!!.text = result.toString()
//                Log.d ("melentev", resultText.toString())
//                println(result)
//                //println(lenght)
//                Log.d ("длина выражения", mathOperation.text.toString().length.toString())
                val expression = mathOperation.text.toString()
                val numbers = mutableListOf<Double>()
                val operators = mutableListOf<Char>()
                var currentNumber = ""
                var i = 0
                while (i < expression.length) {
                    val ch = expression[i]  // Берём символ из строки

                    if (ch.isDigit() || ch == '.') {
                        currentNumber += ch  // Если цифра или '.', добавляем к текущему числу
                    } else {
                        if (currentNumber.isNotEmpty()) {
                            numbers.add(currentNumber.toDouble())  // Если число накопилось, добавляем его в список
                            currentNumber = ""  // Обнуляем
                        }
                        operators.add(ch)  // Добавляем оператор (+, -, *, /)
                    }
                    i++
                }
                if (currentNumber.isNotEmpty()) numbers.add(currentNumber.toDouble())


                var j = 0                                                    // Шаг 1: Обрабатываем *, /
                while (j < operators.size) {
                    if (operators[j] == '*' || operators[j] == '/') {
                        val left = numbers[j]
                        val right = numbers[j + 1]
                        val result = if (operators[j] == '*') left * right else left / right

                        numbers[j] = result
                        numbers.removeAt(j + 1)
                        operators.removeAt(j)
                        j--
                    }
                    j++
                }

                // Шаг 2: Обрабатываем +, -
                var result = numbers[0]
                for (k in operators.indices) {
                    when (operators[k]) {
                        '+' -> result += numbers[k + 1]
                        '-' -> result -= numbers[k + 1]
                    }
                }
                resultText.text = result.toString()



            } catch (e: Exception) {
                resultText!!.text = "Ошибка"
                Log.d ("oshibka!!!", resultText.toString())
            }
        }
    }
}