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

    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var texCoordBuffer: FloatBuffer
    private var textureId: Int = 0
    private var program: Int = 0
    private var uMVPMatrixLocation: Int = 0
    private var alphaHandle: Int = 0
    private val mvpMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        val vertexShaderCode = """
            attribute vec4 a_Position;
            attribute vec2 a_TexCoordinate;
            varying vec2 v_TexCoordinate;
            void main() {
                gl_Position = a_Position;
                v_TexCoordinate = a_TexCoordinate;
            }
        """.trimIndent()

        val fragmentShaderCode = """
            precision mediump float;
            varying vec2 v_TexCoordinate;
            uniform sampler2D u_Texture;
            void main() {
                vec4 color = texture2D(u_Texture, v_TexCoordinate);
                gl_FragColor = color ;
            }
        """.trimIndent()

        val vertexData = floatArrayOf(
            -1.0f, 1.0f,  // Top left
            -1.0f, -1.0f,  // Bottom left
            1.0f, 1.0f,  // Top right
            1.0f, -1.0f // Bottom right
        )

        val vertexBuffer = ByteBuffer.allocateDirect(8 * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(vertexData)
                position(0)
            }
        }

        val texCoordBuffer = ByteBuffer.allocateDirect(8 * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f))
                position(0)
            }
        }

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }

        GLES20.glUseProgram(program)

//        uMVPMatrixLocation = GLES20.glGetUniformLocation(program, "u_MVPMatrix")
//        alphaHandle = GLES20.glGetUniformLocation(program, "alpha")

        val textureHandle = IntArray(1)
        GLES20.glGenTextures(1, textureHandle, 0)
        textureId = textureHandle[0]

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, imageBitmap, 0)

        imageBitmap.recycle()

        vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(
            GLES20.glGetAttribLocation(program, "a_Position"),
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            vertexBuffer
        )
        GLES20.glEnableVertexAttribArray(GLES20.glGetAttribLocation(program, "a_Position"))

        texCoordBuffer.position(0)
        GLES20.glVertexAttribPointer(
            GLES20.glGetAttribLocation(program, "a_TexCoordinate"),
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            texCoordBuffer
        )
        GLES20.glEnableVertexAttribArray(GLES20.glGetAttribLocation(program, "a_TexCoordinate"))
//        GLES20.glEnable(GLES20.GL_BLEND)
//        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
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

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        GLES20.glUseProgram(program)
//        GLES20.glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0)
//        GLES20.glUniform1f(alphaHandle, 0.9f)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "u_Texture"), 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }
}

