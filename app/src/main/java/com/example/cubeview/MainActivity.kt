package com.example.cubeview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    val cubeView :CubeView by lazy {
        findViewById<CubeView>(R.id.cube_view)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


    }

    override fun onResume() {
        super.onResume()
        cubeView.setShap(CubeView.Square(100,5))
    }
}
