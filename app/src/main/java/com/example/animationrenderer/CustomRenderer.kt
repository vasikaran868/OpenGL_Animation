package com.example.animationrenderer

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class CustomRenderer(val context: Context): GLSurfaceView.Renderer {
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    lateinit var image: Image
    private var mProjectionMatrix = FloatArray(16)
    var squareCoords = floatArrayOf(
        -0.5f, 0.5f, 0.0f,  // top left
        -0.5f, -0.5f, 0.0f,  // bottom left
        0.5f, -0.5f, 0.0f,  // bottom right
        0.5f, 0.5f, 0.0f
    ) // top right


    var scaleCoords = floatArrayOf(
        -0.2f, 1.5f,  // top left
        -0.2f, -0.5f,  // bottom left
        0.2f, 0.0f,  // bottom right
        0.2f, 1.5f
    ) // top right

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        image = Image(context, scaleCoords)
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        this.screenWidth = width
        this.screenHeight = height
        val ratio: Float = width.toFloat() / height.toFloat()

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }

    val mViewMatrix = FloatArray(16)
    val mMVPMatrix = FloatArray(16)
    val mRotationMatrix = FloatArray(16)
    val mRotationMatrix_ = FloatArray(16)

    override fun onDrawFrame(p0: GL10?) {
        val scratch = FloatArray(16)
        // Same rotation matrix except the angle is the opposite
        // Because of the way the PNG is loaded ?
        val scratch_ = FloatArray(16)

        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // Create a rotation transformation for the triangle
        val angle = 90f;
        Matrix.setRotateM(mRotationMatrix, 0, angle, 0f, 0f, 1.0f);
        Matrix.setRotateM(mRotationMatrix_, 0, -angle, 0f, 0f, 1.0f);

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);
        Matrix.multiplyMM(scratch_, 0, mMVPMatrix, 0, mRotationMatrix_, 0);

        //draw square and scale
        image.draw(scratch)
    }
}