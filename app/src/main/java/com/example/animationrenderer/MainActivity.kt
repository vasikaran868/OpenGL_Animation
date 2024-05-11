package com.example.animationrenderer

import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.AppCompatButton
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

class MainActivity : AppCompatActivity() {

    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val glView = findViewById<GLSurfaceView>(R.id.glView)
//        val btn = findViewById<AppCompatButton>(R.id.animateBtn)
        val renderer =  ImageRenderer(this, BitmapFactory.decodeResource(this.resources, R.drawable.peacock) ,glView, executor)
        glView.setEGLContextClientVersion(2)
        glView.setRenderer(renderer)
        glView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
//        btn.setOnClickListener {
//            val renderer =  GlRenderer(30,this, glView)
//            glView.setEGLContextClientVersion(2)
//            glView.setRenderer(renderer)
//            glView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
    }
}