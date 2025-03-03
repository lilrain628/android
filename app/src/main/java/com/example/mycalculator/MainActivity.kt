package com.example.mycalculator

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import net.objecthunter.exp4j.ExpressionBuilder

class MainActivity : AppCompatActivity() {
    var mathOperation: TextView? = null
    var resultText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mathOperation = findViewById<View>(R.id.math_operation) as TextView
        resultText = findViewById<View>(R.id.result_text) as TextView

        findViewById<View>(R.id.btn_0).setOnClickListener { v: View? -> // ищем viwe индентификаторм функции btn_0, setOnClickListener- задает действие которое будет выполняться при нажатии кнопки
            mathOperation!!.append(                                     // v - содержит ссылку на кнопку, mathOperation!! это TextView который содержит мат.выражение (!! проверка не на null)
                "0"                                                     //append добовляем 0 в текст
            )
        }
        findViewById<View>(R.id.btn_1).setOnClickListener { v: View? ->
            mathOperation!!.append(
                "1"
            )
        }
        findViewById<View>(R.id.btn_2).setOnClickListener { v: View? ->
            mathOperation!!.append(
                "2"
            )
        }
        findViewById<View>(R.id.btn_3).setOnClickListener { v: View? ->
            mathOperation!!.append(
                "3"
            )
        }
        findViewById<View>(R.id.btn_4).setOnClickListener { v: View? ->
            mathOperation!!.append(
                "4"
            )
        }
        findViewById<View>(R.id.btn_5).setOnClickListener { v: View? ->
            mathOperation!!.append(
                "5"
            )
        }
        findViewById<View>(R.id.btn_6).setOnClickListener { v: View? ->
            mathOperation!!.append(
                "6"
            )
        }
        findViewById<View>(R.id.btn_7).setOnClickListener { v: View? ->
            mathOperation!!.append(
                "7"
            )
        }
        findViewById<View>(R.id.btn_8).setOnClickListener { v: View? ->
            mathOperation!!.append(
                "8"
            )
        }
        findViewById<View>(R.id.btn_9).setOnClickListener { v: View? ->
            mathOperation!!.append(
                "9"
            )
        }
        findViewById<View>(R.id.dot_btn).setOnClickListener { v: View? ->
            mathOperation!!.append(
                "."
            )
        }

        findViewById<View>(R.id.plus_btn).setOnClickListener { v: View? ->
            mathOperation!!.append(
                "+"
            )
        }
        findViewById<View>(R.id.minus_btn).setOnClickListener { v: View? ->
            mathOperation!!.append(
                "-"
            )
        }
        findViewById<View>(R.id.mult_btn).setOnClickListener { v: View? ->
            mathOperation!!.append(
                "*"
            )
        }
        findViewById<View>(R.id.div_btn).setOnClickListener { v: View? ->
            mathOperation!!.append(
                "/"
            )
        }
        findViewById<View>(R.id.lsk_btn).setOnClickListener { v: View? ->
            mathOperation!!.append(
                "("
            )
        }
        findViewById<View>(R.id.rsk_btn).setOnClickListener { v: View? ->
            mathOperation!!.append(
                ")"
            )
        }

        findViewById<View>(R.id.ac_btn).setOnClickListener { v: View? ->
            mathOperation!!.text = "0"
            resultText!!.text = "0"
        }

        findViewById<View>(R.id.back_btn).setOnClickListener { v: View? ->  //Проверяем, есть ли хотя бы один символ (text.length > 0).
                                                                                         // Если да, обрезаем строку, удаляя последний символ
            val text = mathOperation!!.text.toString()
            if (text.length > 0) {
                mathOperation!!.text = text.substring(0, text.length - 1)
            }
        }

        findViewById<View>(R.id.equal_btn).setOnClickListener { v: View? ->
            try {
                val result = ExpressionBuilder(mathOperation!!.text.toString()).build().evaluate()
                resultText!!.text = result.toString() + ""
            } catch (e: Exception) {
                resultText!!.text = "Ошибка!"
            }
        }
    }
}