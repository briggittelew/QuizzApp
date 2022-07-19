package com.briggittelew.quizzapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.briggittelew.quizzapp.databinding.ActivityPointsBinding
import com.squareup.picasso.Picasso
import org.json.JSONArray
import java.io.IOException
import java.io.InputStream

class PointsActivity : AppCompatActivity() {

    private lateinit var b: ActivityPointsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityPointsBinding.inflate(layoutInflater)
        setContentView(b.root)

        settingActionBar()

        val bundle = intent.extras
        val dato = bundle?.getInt("mensaje")
        if (dato != null) {
            showResults(dato)
        }
    }

    private fun settingActionBar(){
        supportActionBar?.hide()
        title = " "
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.75).toInt()
        window.setLayout(width, height)
    }

    //Cargar Resultado
    private fun showResults(result: Int){
        var json : String? = null
        val inputStream: InputStream = assets.open("profile.json")
        json = inputStream.bufferedReader().use {it.readText()}

        try {
            val rango = when{
                result in 50..100 -> 1
                result in 110..500 -> 2
                else -> 0
            }
            val jsonArr = JSONArray(json)
            val jsonObj = jsonArr.getJSONObject(rango)
            val imagenUrl = jsonObj.getString("image")
            Picasso.get().load(imagenUrl).into(b.ivProfile)
            b.tvPoints.text = result.toString()
            b.tvProfile.text = jsonObj.getString("description")
        }catch (e: IOException){}
        //TODO: Grabar puntuacion en base de datos,
        // si la puntuacion es mayor a la existente en la base de datos o
        // el usuario no ha sido registrado
    }
}