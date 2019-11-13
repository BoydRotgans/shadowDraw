import com.google.gson.Gson
import org.openrndr.animatable.Animatable
import org.openrndr.animatable.Clock
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.ffmpeg.FFMPEGVideoPlayer
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.math.Vector2
import org.openrndr.math.Vector4
import org.openrndr.resourceUrl
import java.io.File

fun main() = application {
    configure {
        width = 1280
        height = 720
    }


    val gson = Gson()
    val jsonString = File("data/data/data-n.json").readText()
    val newList = gson.fromJson(jsonString, FrameRect::class.java)

    var cbContourList = mutableListOf<ColorBuffer>()
    var cbVideoList = mutableListOf<ColorBuffer>()
    var cbSourceList = mutableListOf<ColorBuffer>()




    class Contour : Filter(filterShaderFromUrl(resourceUrl("/shaders/contour.frag"))) {
        var resolution: Vector2 by parameters
        var colors: Array<Vector4> by parameters
        init {
            colors = (0..4).map {
                Vector4((0.8-it / 10.0), ( 0.8-it / 10.0), (0.8- it / 10.0), 1.0)
            }.toTypedArray()
        }
    }

    program {


//        extend(ScreenRecorder().apply {
//            contentScale = 1.0
//            frameRate = 60
//        })
//
//        Animatable.clock(object: Clock {
//            override val time: Long
//                get() {
//                    return (seconds*1000).toLong()
//                }
//        })

        var contour = Contour()

        var source = renderTarget(1280, 720) {
            colorBuffer()
        }

        var video = renderTarget(1280, 720) {
            colorBuffer()
        }

        var contourShapes = renderTarget(1280, 720) {
            colorBuffer()
        }

        var contourOut = colorBuffer(width, height, 1.0)

        val font = FontImageMap.fromUrl("file:data/fonts/IBMPlexMono-Regular.ttf", 64.0)

        drawer.isolatedWithTarget(source) {
            drawer.background(ColorRGBa.PINK)
        }

        val videoDemoPlayer: FFMPEGVideoPlayer = FFMPEGVideoPlayer.fromURL("file:data/videos/demo.mp4")
        videoDemoPlayer.start()

        lateinit var videoPlay: FFMPEGVideoPlayer
        videoPlay = FFMPEGVideoPlayer.fromURL("file:data/videos/air3.mov")
        videoPlay.start()

        var frame = 0
        var tic = 0
        var correct = 0
        var movingX = 0.0
        var movingY = 0.0
        var lastXPos = 0.0
        var deltaX = 0.0
        var movingDelta = 0.0

        var line = mutableListOf<Vector2>()
        var statDelta = mutableListOf<Vector2>()

        extend {

            // black white video
            source = renderTarget(1280, 720) {
                colorBuffer()
            }

            drawer.isolatedWithTarget(source) {
                videoDemoPlayer.next()
                videoDemoPlayer.draw(drawer)
            }


            // backdrop
            video = renderTarget(1280, 720) {
                colorBuffer()
            }

            drawer.isolatedWithTarget(video) {
                videoPlay.next()
                videoPlay.draw(drawer)
            }

            // get contours
            contourShapes = renderTarget(1280, 720) {
                colorBuffer()
            }

            contour.resolution = Vector2(width * 1.0, height * 1.0)
            contour.apply(source.colorBuffer(0), contourOut)

            drawer.isolatedWithTarget(contourShapes) {
                drawer.background(ColorRGBa.TRANSPARENT)
                drawer.shadeStyle = shadeStyle {
                    fragmentTransform = """
                        float colorFill = 0.0;
                        float alpha = 0.0;
                        if(x_fill.r > 0.9) {
                            alpha = 1.0;
                        }
                        x_fill.rgba = vec4( x_fill.r,  x_fill.g, x_fill.b, alpha);
                        """.trimIndent()
                }
                drawer.image(contourOut)
            }

            // clean
            if(cbContourList.size > 140) {
                cbContourList.removeAt(0)
                cbVideoList.removeAt(0)
                cbSourceList.removeAt(0)
                //cbBackList2.removeAt(0)
                correct++
            }

            // add
            cbContourList.add(contourShapes.colorBuffer(0))
            cbVideoList.add(video.colorBuffer(0))
            cbSourceList.add(source.colorBuffer(0))


            drawer.image(video.colorBuffer(0))

            // draw video
            drawer.isolated {
                cbSourceList.forEachIndexed { index, colorBuffer ->
                    drawer.shadeStyle = shadeStyle {
                        fragmentTransform = """
                        vec2 textSize = textureSize(p_texture, 0);
                        //vec2 uv = (c_screenPosition + vec2(p_count, 0.0) ) /  textSize;
                        vec2 uv = (c_screenPosition - vec2( (cos(  ((p_count*0.1)+p_seconds*0.5)))*40.0, 0.0)) /  textSize;
                        vec4 image = texture(p_texture,  1.0-vec2(1.0-uv.x, uv.y)).rgba;
                        float colorFill = 1.0; //sin((p_seconds*2.5)+(p_count*10.0));
                        float alpha = 1.0;
                        if(x_fill.r > 0.5) {
                            alpha = 0.0;
                        }
                        x_fill.rgba = vec4( image.r*colorFill,  image.g*colorFill, image.b*colorFill, alpha);
                        """.trimIndent()
                    }.parameter("texture", cbVideoList.get((frame+index)%cbVideoList.size))
                        .parameter("count", (index))
                        .parameter("seconds", seconds)
                        .parameter("fillIn", ColorRGBa.PINK)
                    drawer.image(colorBuffer)
                }
            }


            // draw contour
            drawer.isolated {
                cbContourList.forEachIndexed { index, colorBuffer ->

//                    drawer.shadeStyle = shadeStyle {
//                        fragmentTransform = """
//
//                         float alpha = abs(sin(p_count*0.025+p_seconds));
//                         x_fill.rgba = vec4( x_fill.r*p_fillIn.r,  x_fill.g*p_fillIn.g, x_fill.b*p_fillIn.b, (x_fill.a*alpha)*0.25);
//                        """.trimIndent()
//                    }
//                        .parameter("count", (index))
//                        .parameter("seconds", seconds)
//                        .parameter("fillIn", ColorRGBa.PINK)
                    drawer.drawStyle.colorMatrix = tint(ColorRGBa.WHITE.opacify(0.1))
                    drawer.image(colorBuffer)
                }
            }


            // shadow
            drawer.isolated {
                drawer.shadeStyle = shadeStyle {
                    fragmentTransform = """
                        float alpha = 1.0;
                        if(x_fill.r > 0.5) {
                            alpha = 0.0;
                        }
                        x_fill.rgba = vec4( x_fill.r,  x_fill.g,  x_fill.b, alpha);
                        """.trimIndent()
                }
                drawer.image(source.colorBuffer(0))
            }


            /*// extra
            drawer.isolated {

                drawer.fontMap = font
                drawer.fill = ColorRGBa.RED
                //drawer.text("$frame realtime", 50.0, 50.0)

                if(newList.frames.filter { it.tic == frame && it.height > (height / 1.5)  }.sortedBy { it.height }.isNotEmpty()) {
                    val frameS = newList.frames.filter { it.tic == frame && it.height > (height / 1.5) }.sortedBy { it.height }.last()
                    drawer.stroke = ColorRGBa.RED
                    drawer.strokeWeight = 1.0
                    drawer.fill = null

                    val rect = Rectangle(frameS.x, frameS.y, frameS.width, frameS.height)
                    drawer.rectangle(rect)
                    drawer.strokeWeight = 5.0
                    //drawer.circle(rect.center, 30.0)

                    movingX = rect.center.x*0.4 + movingX *0.6
                    movingY = rect.center.y*0.4 + movingY *0.6

                    drawer.stroke = ColorRGBa.GREEN
                    //drawer.circle(Vector2(movingX, movingY), 30.0)
                    line.add(Vector2(movingX, movingY))
                    deltaX = Math.abs(movingX - lastXPos)
                    movingDelta = deltaX*0.01 + movingDelta*0.99
                    statDelta.add(Vector2(50+statDelta.size.toDouble(), (200-movingDelta)))

                    if(statDelta.size > width-100) {
                        statDelta.clear()
                    }
                    drawer.fill = ColorRGBa(1.0, 0.0, 1.0)
                    //drawer.text("deltaX $movingDelta", 50+statDelta.size.toDouble(), (200-movingDelta))
                    if(movingDelta < 2.0) {
                        drawer.fill = ColorRGBa.GREEN
//                        drawer.text("person still", 50.0, 100.0)
                    } else {
                        drawer.fill = ColorRGBa.BLUE
                        //drawer.text("person moving", 50.0, 100.0)
                    }

                    lastXPos = movingX
                }

//                drawer.strokeWeight = 1.0
//                drawer.stroke = ColorRGBa.BLUE
//                drawer.lineStrip(line)
//
//                drawer.strokeWeight = 1.0
//                drawer.stroke = ColorRGBa(1.0, 0.0, 1.0)
//                drawer.lineStrip(statDelta)
            }*/

            //if(tic%2 == 0) {
                frame ++
           // }
            tic++
        }
    }
}