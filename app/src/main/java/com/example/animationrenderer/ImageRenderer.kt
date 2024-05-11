package com.example.animationrenderer

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ImageRenderer(private val context: Context, private val imageBitmap: Bitmap, val glView: GLSurfaceView, private val executor: ScheduledExecutorService) : GLSurfaceView.Renderer {

    private var textureId: Int = 0
    private var program: Int = 0
//    private var uMVPMatrixLocation: Int = 0
//    private val mvpMatrix = FloatArray(16)
//    private val projectionMatrix = FloatArray(16)
//    private val viewMatrix = FloatArray(16)


    //Handles
    private var aPositionHandle = -1
    private var uTextureHandle = -1
    private var aTextureCoordinateHandle = -1
    private var alphaHandle = -1


    private val vertexShaderCode = """
            attribute vec4 a_Position;
            attribute vec2 a_TexCoordinate;
            varying vec2 v_TexCoordinate;
            uniform mat4 u_MVPMatrix;
            void main() {
                gl_Position = a_Position;
                v_TexCoordinate = a_TexCoordinate;
            }
        """.trimIndent()

    private val fragmentShaderCode = """
            precision mediump float;
            varying vec2 v_TexCoordinate;
            uniform sampler2D u_Texture;
            uniform float alpha;
            void main() {
                vec4 color = texture2D(u_Texture, v_TexCoordinate);
                gl_FragColor = color * alpha;
            }
        """.trimIndent()

    private val vertexData = floatArrayOf(
        -1.0f, 1.0f,  // Top left
        -1.0f, -1.0f,  // Bottom left
        1.0f, 1.0f,  // Top right
        1.0f, -1.0f // Bottom right
    )

    private val textureData = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f)

    private val vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(8 * 4).run {
        order(ByteOrder.nativeOrder())
        asFloatBuffer().apply {
            put(vertexData)
            position(0)
        }
    }

    private val texCoordBuffer: FloatBuffer = ByteBuffer.allocateDirect(8 * 4).run {
        order(ByteOrder.nativeOrder())
        asFloatBuffer().apply {
            put(textureData)
            position(0)
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }

        val textureHandle = IntArray(1)
        GLES20.glGenTextures(1, textureHandle, 0)
        textureId = textureHandle[0]

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, imageBitmap, 0)

        imageBitmap.recycle()

        //Handles
        aPositionHandle = GLES20.glGetAttribLocation(program, "a_Position")
        uTextureHandle = GLES20.glGetUniformLocation(program, "u_Texture")
        aTextureCoordinateHandle = GLES20.glGetAttribLocation(program, "a_TexCoordinate")
//        uMVPMatrixLocation = GLES20.glGetUniformLocation(program, "u_MVPMatrix")
        alphaHandle = GLES20.glGetUniformLocation(program, "alpha")


        GLES20.glVertexAttribPointer(aPositionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glEnableVertexAttribArray(aPositionHandle)

        GLES20.glVertexAttribPointer(aTextureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer)
        GLES20.glEnableVertexAttribArray(aTextureCoordinateHandle)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        executor.scheduleAtFixedRate(::mRequestRender, 0, 33, TimeUnit.MILLISECONDS)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        "surface changed called".rlog()
        GLES20.glViewport(0, 0, width, height)
//        val ratio = width.toFloat() / height.toFloat()
//        Matrix.orthoM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
//        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -5f, 0f, 0f, 0f, 0f, 1f, 0f)
//        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }
    fun mRequestRender(){
        glView.requestRender()
    }

    var alpha = 1.0f
    var isIncreasing = false

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        GLES20.glUseProgram(program)
//        GLES20.glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0)
        GLES20.glUniform1f(alphaHandle, alpha)
        if (alpha <= 0.5f){
            alpha += 0.02f
            isIncreasing = true
        } else if (alpha >= 0.9f){
            alpha -= 0.02f
            isIncreasing = false
        } else {
            if (isIncreasing){
                alpha += 0.02f
            } else {
                alpha -= 0.02f
            }
        }

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(uTextureHandle, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }
}

