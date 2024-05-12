package com.example.animationrenderer

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer


class Image(activityContext: Context, coords: FloatArray) {
    //Reference to Activity Context
    private val mActivityContext: Context

    //Added for Textures
    private val mCubeTextureCoordinates: FloatBuffer
    private var mTextureUniformHandle = 0
    private var mTextureCoordinateHandle = 0
    private val mTextureCoordinateDataSize = 2
    private val mTextureDataHandle: Int
    private val vertexShaderCode = "attribute vec2 a_TexCoordinate;" +
            "varying vec2 v_TexCoordinate;" +
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition ;" +  //Test
            "v_TexCoordinate = a_TexCoordinate;" +  //End Test
            "}"
    private val fragmentShaderCode = "precision mediump float;" +
            "uniform sampler2D u_Texture;" +
            "varying vec2 v_TexCoordinate;" +
            "void main() {" +
            "gl_FragColor = texture2D(u_Texture, v_TexCoordinate);" +
            "}"
    private val shaderProgram: Int
    private val vertexBuffer: FloatBuffer
    private val drawListBuffer: ShortBuffer
    private var mPositionHandle = 0
    private var mMVPMatrixHandle = 0
    private val drawOrder = shortArrayOf(0, 1, 2, 0, 2, 3) //Order to draw vertices
    private val vertexStride = COORDS_PER_VERTEX * 4 //Bytes per vertex

    init {
        mActivityContext = activityContext

        //Initialize Vertex Byte Buffer for Shape Coordinates / # of coordinate values * 4 bytes per float
        val bb: ByteBuffer = ByteBuffer.allocateDirect(coords.size * 4)
        //Use the Device's Native Byte Order
        bb.order(ByteOrder.nativeOrder())
        //Create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer()
        //Add the coordinates to the FloatBuffer
        vertexBuffer.put(coords)
        //Set the Buffer to Read the first coordinate
        vertexBuffer.position(0)

        // U, V coordinates
        val cubeTextureCoordinateData = floatArrayOf(
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
        )
        mCubeTextureCoordinates = ByteBuffer.allocateDirect(cubeTextureCoordinateData.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mCubeTextureCoordinates.put(cubeTextureCoordinateData).position(0)

        //Initialize byte buffer for the draw list
        val dlb: ByteBuffer = ByteBuffer.allocateDirect(coords.size * 2)
        dlb.order(ByteOrder.nativeOrder())
        drawListBuffer = dlb.asShortBuffer()
        drawListBuffer.put(drawOrder)
        drawListBuffer.position(0)
        val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        shaderProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(shaderProgram, vertexShader)
        GLES20.glAttachShader(shaderProgram, fragmentShader)

        //Texture Code
        GLES20.glBindAttribLocation(shaderProgram, 0, "a_TexCoordinate")
        GLES20.glLinkProgram(shaderProgram)

        //Load the texture
        // Retrieve our image from resources.
        val id: Int = mActivityContext.resources.getIdentifier(
            "drawable/peacock", "drawable",
            mActivityContext.packageName
        )
        mTextureDataHandle = loadTexture(mActivityContext, id)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }

    fun draw(mvpMatrix: FloatArray?) {
        //Add program to OpenGL ES Environment
        GLES20.glUseProgram(shaderProgram)

        //Get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition")

        //Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle)

        //Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
            mPositionHandle,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )

        //Set Texture Handles and bind Texture
        mTextureUniformHandle = GLES20.glGetAttribLocation(shaderProgram, "u_Texture")
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(shaderProgram, "a_TexCoordinate")

        //Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)

        //Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle)

        //Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0)

        //Pass in the texture coordinate information
        mCubeTextureCoordinates.position(0)
        GLES20.glVertexAttribPointer(
            mTextureCoordinateHandle, mTextureCoordinateDataSize,
            GLES20.GL_FLOAT, false, 0, mCubeTextureCoordinates
        )
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle)

        //Get Handle to Shape's Transformation Matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix")

        //Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)

        //Draw the triangle
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            drawOrder.size,
            GLES20.GL_UNSIGNED_SHORT,
            drawListBuffer
        )

        //Disable Vertex Array
        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }

    companion object {
        // number of coordinates per vertex in this array
        const val COORDS_PER_VERTEX = 2
        fun loadTexture(context: Context, resourceId: Int): Int {
            val textureHandle = IntArray(1)
            GLES20.glGenTextures(1, textureHandle, 0)
            if (textureHandle[0] != 0) {
                val options = BitmapFactory.Options()
                options.inScaled = false // No pre-scaling

                // Read in the resource
                val bitmap =
                    BitmapFactory.decodeResource(context.getResources(), resourceId, options)

                // Bind to the texture in OpenGL
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])

                // Set filtering
                GLES20.glTexParameteri(
                    GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_NEAREST
                )
                GLES20.glTexParameteri(
                    GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_NEAREST
                )

                // Load the bitmap into the bound texture.
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

                // Recycle the bitmap, since its data has been loaded into OpenGL.
                bitmap.recycle()
            }
            if (textureHandle[0] == 0) {
                throw RuntimeException("Error loading texture.")
            }
            return textureHandle[0]
        }
    }
}