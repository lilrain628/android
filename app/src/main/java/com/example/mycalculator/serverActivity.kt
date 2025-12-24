package com.example.mycalculator

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.zeromq.SocketType
import org.zeromq.ZMQ
import org.zeromq.ZContext
import java.io.*
import java.net.Socket

class serverActivity : AppCompatActivity() {
    private var log_tag: String = "MY_LOG_TAG"
    private lateinit var tvSockets: TextView
    private var textString: String = ""
    private lateinit var handler: Handler

    private var zmqContext: ZContext? = null
    private var zmqSocket: ZMQ.Socket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.serverclient_activity)

        tvSockets = findViewById(R.id.tvSockets)
        handler = Handler(Looper.getMainLooper())
    }

    fun connectToPythonServer() {
        Thread {
            try {
                zmqContext = ZContext()
                zmqSocket = zmqContext!!.createSocket(SocketType.REQ).apply {
                    setReceiveTimeOut(3000)
                    setSendTimeOut(3000)
                    connect("tcp://192.168.0.13:2222")
                }

                zmqSocket!!.send("Hello py server from Android".toByteArray(), 0)
                val response = zmqSocket!!.recv(0) ?: throw Exception("Таймаут")

                Log.d(log_tag, "Успешно: ${String(response)}")

            } catch (e: Exception) {
                Log.e(log_tag, "error: ${e.message}")
            }
        }.start()
    }

    fun startServer() {
        val context = ZMQ.context(1)
        val socket = context.socket(SocketType.REP)
        socket.bind("tcp://*:2222")
        var counter: Int = 0

        while (!Thread.currentThread().isInterrupted) {
            counter++
            val requestBytes = socket.recv(0)
            val request = String(requestBytes, ZMQ.CHARSET)
            println("[SERVER] Received request: [$request]")

            handler.post {
                tvSockets.text = "Received MSG from Client = $counter"
            }

            Thread.sleep(1000)

            val response = "Hello from Android ZMQ Server!"
            socket.send(response.toByteArray(ZMQ.CHARSET), 0)
            println("[SERVER] Sent reply: [$response]")
        }

        socket.close()
        context.close()
    }

    fun startClient() {
        val context = ZMQ.context(1)
        val socket = context.socket(SocketType.REQ)
        socket.connect("tcp://localhost:2222")
        val request = "Hello from Android client!"

        for (i in 0..10) {
            socket.send(request.toByteArray(ZMQ.CHARSET), 0)
            Log.d(log_tag, "[CLIENT] SendT: $request")

            val reply = socket.recv(0)
            Log.d(log_tag, "[CLIENT] Received: " + String(reply, ZMQ.CHARSET))
        }

        socket.close()
        context.close()
    }

    override fun onResume() {
        super.onResume()
        connectToPythonServer()

        val runnableServer = Runnable { startServer() }
        val threadServer = Thread(runnableServer)
        threadServer.start()

        Thread.sleep(1000)

        val runnableClient = Runnable { startClient() }
        val threadClient = Thread(runnableClient)
        threadClient.start()
    }
}