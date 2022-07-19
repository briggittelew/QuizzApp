package com.briggittelew.quizzapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.core.view.forEach
import com.briggittelew.quizzapp.databinding.ActivityMainBinding
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.*
import org.json.JSONArray
import java.io.IOException
import java.io.InputStream
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private var mutableSet = mutableSetOf<Int>()
    private var itemMutable = 0
    private var rightAnswer = 0
    private var score = 0
    private lateinit var serviceIntent: Intent
    private var time = 30.0
    private var rightAnswerText = ""
    private var counter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        //Evento analytics
        val analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message", "Integración de firebase completa")
        analytics.logEvent("InitScreen", bundle)

        //Configuracion de ActionBar
        supportActionBar?.title = HtmlCompat.fromHtml("<font color='#000'>"+
                "SimpsonQuiz", HtmlCompat.FROM_HTML_MODE_LEGACY)

        //Crear lista aleatoria de preguntas
        listQuestion()

        //Cargar Preguntas
        makeQuestion(itemMutable)

        //Iniciar temporizador
        serviceIntent = Intent(applicationContext, TimerService::class.java)
        registerReceiver(updateTime, IntentFilter(TimerService.TIMER_UPDATED))
        startTimer()

        //Acción botón responder
        b.btAnswer.setOnClickListener {
            if (counter < 10){
                if (getChecked() > 0 && getChecked() == rightAnswer) {
                    score += time.toInt()
                    message("CORRECTO!!")
                } else {
                    b.tvRightAnswer.text = rightAnswerText
                }
                itemMutable++
                resetTimer()
                counter++
                message(counter.toString())
            }else{
                endGame()
            }
            stopTimer()
        }
    }

    //Cargar Pregunta
    private fun makeQuestion(item: Int){
        var json : String? = null
        val inputStream: InputStream = assets.open("questions.json")
        json = inputStream.bufferedReader().use {it.readText()}
        try {
            val jsonArr = JSONArray(json)
            val jsonObj = jsonArr.getJSONObject(mutableSet.elementAt(item))
            b.tvQuestion.text = jsonObj.getString("text")
            val jsonAnswers = jsonObj.getJSONArray("answers")
            b.rbOption1.text = jsonAnswers.getJSONObject(0).getString("text")
            b.rbOption2.text = jsonAnswers.getJSONObject(1).getString("text")
            b.rbOption3.text = jsonAnswers.getJSONObject(2).getString("text")
            b.rbOption4.text = jsonAnswers.getJSONObject(3).getString("text")
            rightAnswer = jsonObj.getInt("correctAnswer")
            rightAnswerText = jsonAnswers.getJSONObject(rightAnswer-1).getString("text")
        }catch (e: IOException){}
    }

    //Iniciar el contador
    private fun startTimer(){
        serviceIntent.putExtra(TimerService.TIME_EXTRA, time)
        startService(serviceIntent)
    }

    //Reiniciar el contador
    private fun resetTimer(){
        CoroutineScope(Dispatchers.IO).launch {
            delay(2000L)
            withContext(Dispatchers.Main) {
                makeQuestion(itemMutable)
                time = 30.0
                serviceIntent.putExtra(TimerService.TIME_EXTRA, time)
                startService(serviceIntent)
                b.rgAnswers.clearCheck()
                b.tvRightAnswer.text = ""
            }
        }
    }

    //Detener el contador
    private fun stopTimer(){
        stopService(serviceIntent)
    }

    //Contador de tiempo
    private val updateTime: BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent) {
            time = intent.getDoubleExtra(TimerService.TIME_EXTRA, 0.0)
            b.tvTimer.text = getTimeStringFromDouble(time)
            //Cambiar de color al llegar a los 10 segundos
            if (time <= 10.0)
                b.tvTimer.setTextColor(Color.parseColor("#EF0909"))
            //Terminar juego cuando se acabe el tiempo
            if (time == 0.0) {
                message("TIME OVER")
                endGame()
            }
        }
    }

    //Calcular tiempo
    private fun getTimeStringFromDouble(time: Double): String {
        val resultInt = time.roundToInt()
        val hours = resultInt % 86400 / 3600
        val minutes = resultInt % 86400 % 3600 / 60
        val seconds = resultInt % 86400 % 3600 % 60
        return makeTimeString(hours, minutes, seconds)
    }

    //Aplicar formato a cronometro
    private fun makeTimeString(hour: Int, min: Int, sec: Int):String =
        String.format("%02d:%02d:%02d", hour, min, sec)

    //Detectar respuesta
    private fun getChecked(): Int{
        var checked = 0
        if (b.rbOption1.isChecked) checked = 1
        if (b.rbOption2.isChecked) checked = 2
        if (b.rbOption3.isChecked) checked = 3
        if (b.rbOption4.isChecked) checked = 4
        return checked
    }

    //Cerrar partida
    private fun endGame(){
        stopTimer()
        b.btAnswer.isEnabled = false
        val bundle = Bundle()
        bundle.putInt("mensaje", score)
        val enviarMensaje = Intent(this, PointsActivity::class.java)
        enviarMensaje.putExtras(bundle)
        startActivity(enviarMensaje)
    }

    //Crear Lista de preguntas en orden aleatorio
    private fun listQuestion(){
        var json : String? = null
        val inputStream: InputStream = assets.open("questions.json")
        json = inputStream.bufferedReader().use {it.readText()}
        try {
            val jsonArr = JSONArray(json)
            do {
                mutableSet.add((0..jsonArr.length()).random())
            }while (mutableSet.size <= jsonArr.length())
        }catch (e: IOException){}
    }

    //Imprimir mensajes flotantes
    private fun message(texto: String){
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show()
    }
}