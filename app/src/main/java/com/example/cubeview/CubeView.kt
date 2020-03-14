package com.example.cubeview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.ArraySet
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sqrt


/**
 * 立体 图形
 * 绘制位置 为 对于屏幕平面的投影
 *
 *
 *  坐标以屏幕为 X Y 平面  Z = 0   以view 中心作为坐标原点
 *  1.0 版本设定 Z轴不可变  中心不可变 永远为 0，0，0
 *
 *  当用手指进行屏幕滑动事件时  实际上是对  X-Z 平面 与  Y-Z平面进行角度旋转
 *
 *
 *
 *
 * */
class CubeView :View{
    val TAG ="CubeView"
    /**
    * 坐标 记录空间位置
    * */
    data class Coordinate(var x:Double=0.0,var y:Double=0.0,var z:Double=0.0){
        //平移位置
        fun transite(dx: Double,dy:Double,dz:Double=0.0):Coordinate{
            this.x = x.plus(dx)
            this.y = y.plus(dy)
            this.z = z.plus(dz)
            Log.i(javaClass.simpleName, "transite :[$x,$y,$z][$dx,$dy,$dz] ")
            return this
        }
        fun transiteTo(x: Double,y: Double,z: Double):Coordinate{
            this.x = x
            this.y = y
            this.z = z
            return this
        }
        //复制
        fun copy():Coordinate{
            return copyAndTransit()
        }
        fun copyAndTransit(dx: Double=0.0,dy: Double=0.0,dz: Double=0.0):Coordinate{
            Log.i(javaClass.simpleName, "copyAndTransit:[$dx,$dy,$dz] ");
            val result = Coordinate(x, y, z).transite(dx, dy, dz)
            return result
        }
        //获取该点到坐标中心的 距离
        fun getCenterDistance():Double{
            return distanceTo(0.0,0.0,0.0)
        }
        fun distanceTo(to:Coordinate):Double{
            return distanceTo(to.x,to.y,to.z)
        }
        fun distanceTo(toX:Double,toY:Double,toZ:Double):Double{
            val dx = (x-toX)
            val dy = (y-toY)
            val dz = (z-toZ)
            val d = dx.pow(dx)+dy.pow(dy)+dz.pow(dz)
            return sqrt(d)
        }

        //计算到X-Y平面的投影
        fun shadowCoordinateXY():ShadowPoint{
            return ShadowPoint(x, y)
        }
        fun shadowCoordinateXZ():ShadowPoint{
            return ShadowPoint(x,z)
        }
        fun shadowCoordinateYZ():ShadowPoint{
            return ShadowPoint(y,z)
        }
        override fun toString(): String {
            return "[$x,$y,$z]"
        }
    }
    /**
     * 工具类 辅助计算坐标相关
     * */
    object CorrdinateHelper{
        fun rotateCoordinateByX(coordinate: Coordinate,dagreeX:Double){
            val targetX = coordinate.x.times(cos(dagreeX) *coordinate.getCenterDistance())
            coordinate.transiteTo(targetX,0.0,0.0)
        }
        fun rotateCoordinateByY(coordinate: Coordinate,dagreeY:Double){
            val targetY = coordinate.y.times(cos(dagreeY) *coordinate.getCenterDistance())
        }
    }
    /**
     * 在屏幕平面投影坐标
     * */
    data class ShadowPoint(var x:Double,var y: Double)
    /**
     * 基本类型
     * 1、正方形 square
    2、三角形 triangle
    3、圆形 circle
    4、矩形 rectangle
    5、椭圆形 oval,ellipse
    6、梯形 trapezium
    7、多边形 polygon
    8、圆柱体 cylinder
    9、菱形 lozenge / dimond
    10、斜方形 Rhombus
    11、角柱体 prism
    12、心形 heart
    13、星形 star
    14、五边形 pentagon
    15、平行四边形 parallelogram
    16、扇形 sector
    17、月形 ( 弦月 ) -- Crescent
    18、正方体 cube
    19、圆锥体 cone
    20、三角锥体 pyramid
    21、球形 sphere
     * */
    enum class Type{
        Square,Circle,Cube,Sphere
    }
    abstract class Shape(
        protected val side: Int,
        val type:Type,//类型
        var center:Coordinate,//中心
        var showLine:Boolean=false,//是否显示 点间连线 默认不显示,
        var dagreeCoordinate: Coordinate=Coordinate(),//初始状态的旋转角度向量 Z轴偏移向量  两个角度组成
        var showContent:Boolean=true//是否显示 内部点 默认显示
    ){
        val coordinates=ArrayList<Coordinate>()//所有坐标点
        val shadowCoordinates = ArrayList<ShadowPoint>()//投影坐标点集合
        protected var viewDistance =0.0//图形边界到中心的最大距离
        /**
         * 对所有坐标点进行赋值
         * */
        abstract fun createAllCoordinate()
        abstract fun transite(dx: Double,dy: Double,dz: Double)


        abstract fun currentTouchZ():Float
        /**
         * 进行旋转操作
         * */
        fun rotate(dx:Double, dy:Double){
           /**首先 分别计算  cZ 相对XY 平面在ZX 的角度变化  正切？ 正切 存在无穷值  换为正弦计算
            * R = viewDistance  旋转半径
            * 位移起点坐标   coordinate(x=0,y=0,z=R)
            * 位移终点坐标   coordinate(x+dx,y+dy,z=R)
            *
           * */
            val endCoordinate = Coordinate(dx,dy,viewDistance)
            val startCoordinate = Coordinate(0.0,0.0,viewDistance)

            val moveDis = endCoordinate.distanceTo(startCoordinate)
            //
            val dagreeOnX = Math.atan2(dx,moveDis)
            val dagreeOnY = Math.atan2(dy,moveDis)
           //  cZ 相对XY 平面在ZX 的角度变化
            Log.i(javaClass.simpleName, "rotate : $dagreeOnX  $dagreeOnY ");
            //每个左边的 相对于坐标原点 进行两次角度变化  X,Y
        }

    }
    /**
     * 正方形
     * */
    class Square(side:Int,private val length: Int ,center: Coordinate= Coordinate(), showLine: Boolean=false) :
        Shape(side,Type.Square, center,showLine) {

        init {
            createAllCoordinate()
        }
        override fun createAllCoordinate() {
            //side 步长  length 边长度 分为奇数 偶数
            //比较容易理解的是 按照每个点从0，0作为 一个角开始铺
            Log.i(javaClass.simpleName, "createAllCoordinate: $side  $length");

            if (showContent) {
                for (x in 0 until length){//x坐标循环
                    for (y in 0 until length){//y坐标循环
                        Log.i(javaClass.simpleName, "loop : $x $y");
                        coordinates.add(center.copyAndTransit(side*x.toDouble(),side*y.toDouble()))
                    }
                }
            }else{
                TODO()
            }
            val offsetX = -side*(length-1).div(2.0)
            val offsetY = -side*(length-1).div(2.0)

            for (coordinate in coordinates) {
                coordinate.transite(offsetX,offsetY)
            }
        }

        override fun transite(dx: Double, dy: Double, dz: Double) {
            for (coordinate in coordinates) {
                coordinate.transite(dx, dy, dz)
            }
        }

        override fun currentTouchZ(): Float {
            return 0f
        }

        override fun toString(): String {
            return "center: [${center.x},${center.y}] side :${side} l:$length capsize = ${coordinates.size}"
        }
    }

    private val center=Coordinate(0.0,0.0,0.0)
    private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        strokeWidth = 10f
    }
    private val shapCollection = ArrayList<Shape>()
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        ensureCenter(measuredWidth,measuredHeight)
    }

    private fun ensureCenter(w:Int,h:Int){

        center.z = 0.0
        center.x = w.div(2.0)
        center.y = h.div(2.0)
        Log.i(TAG, "ensureCenter: $center");
    }

    fun setShap(shap:Shape){
        shapCollection.clear()
        shapCollection.add(shap)
        invalidate()
    }

    private val gestureListener =object : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            return super.onFling(e1, e2, velocityX, velocityY)
        }

        override fun onDown(e: MotionEvent?): Boolean {
            return true
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            rotateShapes(distanceX.toDouble(),distanceY.toDouble())
            return true
        }
    }
    private val gestureDetector = GestureDetector(context,gestureListener)

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        Log.i(TAG, "onTouchEvent: ");
        return gestureDetector.onTouchEvent(event)
    }

    private fun rotateShapes(dx:Double,dy: Double){

        for (shape in shapCollection) {
            shape.rotate(dx,dy)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.translate(center.x.toFloat(),center.y.toFloat())
        for (shape in shapCollection) {
            Log.i(TAG, "shape: $shape");
            for (coordinate in shape.coordinates) {
                Log.i(TAG, "coordinate: $coordinate");
                canvas?.drawPoint(coordinate.x.toFloat(),coordinate.y.toFloat(),paint)
            }
        }
        canvas?.translate(-center.x.toFloat(),-center.y.toFloat())

    }
}