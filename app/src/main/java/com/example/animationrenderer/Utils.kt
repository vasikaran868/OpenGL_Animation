package com.example.animationrenderer

import android.util.Log

fun String.rlog(){
    Log.v("Debug Tag", this)
}


fun FloatArray.printMatrix(name: String){
    var a = ""
    this.forEach { a += "$it , " }
    "$name matrix...${a}".rlog()
}

fun printMatrixJava(array:FloatArray, name: String){
    var a = ""
    array.forEach { a += "$it , " }
    "$name matrix...${a}".rlog()
}