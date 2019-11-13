import com.google.gson.Gson
import org.openrndr.animatable.Animatable
import org.openrndr.animatable.Clock
import org.openrndr.animatable.easing.Easing
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.ffmpeg.FFMPEGVideoPlayer
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.math.Vector2
import org.openrndr.math.Vector4
import org.openrndr.resourceUrl
import org.openrndr.shape.Rectangle
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

    var movingDeltaStats = mutableListOf<Double>()

    class Contour : Filter(filterShaderFromUrl(resourceUrl("/shaders/contour.frag"))) {
        var resolution: Vector2 by parameters
        var colors: Array<Vector4> by parameters
        init {
            colors = (0..4).map {
                Vector4((0.8-it / 10.0), ( 0.8-it / 10.0), (0.8- it / 10.0), 1.0)
            }.toTypedArray()
        }
    }

    class motion : Animatable() {
        var fade = 0.0
        fun animateIn() {
            animate("fade", 1.0, 1000, Easing.SineInOut)
        }
        fun animateOut() {
            animate("fade", 0.0, 1000, Easing.SineInOut)
        }
    }

    program {

        var m = motion()

        extend(ScreenRecorder().apply {
            contentScale = 1.0
            frameRate = 30
        })

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

            m.updateAnimation()

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
//                videoPlay.next()
//                videoPlay.draw(drawer)
                drawer.background(ColorRGBa.PINK)
            }

            // get contours
            contourShapes = renderTarget(1280, 720) {
                colorBuffer()
            }

            contour.resolution = Vector2(width * 1.0, height * 1.0)
            contour.apply(source.colorBuffer(0), contourOut)

            drawer.isolatedWithTarget(contourShapes) {
                drawer.background(ColorRGBa.BLACK)
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
            if(movingDeltaStats.size > 140) {
                movingDeltaStats.removeAt(0)
            }




            // add
            cbContourList.add(contourShapes.colorBuffer(0))
            cbVideoList.add(video.colorBuffer(0))
            cbSourceList.add(source.colorBuffer(0))
            movingDeltaStats.add(movingDelta)

            drawer.background(ColorRGBa.WHITE)
            //drawer.image(video.colorBuffer(0))

            // draw video
            drawer.isolated {

                cbSourceList.forEachIndexed { index, colorBuffer ->

                    drawer.isolated {
                        drawer.shadeStyle = shadeStyle {
                            fragmentTransform = """
                        vec2 textSize = textureSize(p_texture, 0);
                        //vec2 uv = (c_screenPosition ) /  textSize;
                        vec2 uv = (c_screenPosition - vec2( sin(p_offset)*25.0, 0.0)) /  textSize;
                        vec4 image = texture(p_texture,  1.0-vec2(1.0-uv.x, uv.y)).rgba;
                        float colorFill = p_fade; //sin((p_seconds*2.5)+(p_count*10.0));
                        float alpha = 1.0;
                        if(x_fill.r > 0.5) {
                            alpha = 0.0;
                        }
                        x_fill.rgba = vec4( image.r*colorFill,  image.g*colorFill, image.b*colorFill, alpha);
                        """.trimIndent()
                        }.parameter("texture", cbVideoList.get((frame + index) % cbVideoList.size))
                            .parameter("count", (index))
                            .parameter("offset", movingDeltaStats.get((frame + index) % movingDeltaStats.size))
                            .parameter("seconds", seconds)
                            .parameter("fillIn", ColorRGBa.PINK)
                            .parameter("fade", m.fade)
                        //drawer.drawStyle.colorMatrix = tint(ColorRGBa.WHITE.opacify(1.0-m.fade))
                        drawer.image(colorBuffer)
                    }
                }

                cbContourList.forEachIndexed { index, colorBuffer ->
                    drawer.isolated {
                        drawer.shadeStyle = shadeStyle {
                            fragmentTransform = """
                            vec2 textSize = textureSize(p_texture, 0);
                            //vec2 uv = (c_screenPosition ) /  textSize; // + vec2(p_count, 0.0)
                            vec2 uv = (c_screenPosition) /  textSize;
                            vec4 image = texture(p_texture,  1.0-vec2(1.0-uv.x, uv.y)).rgba;
                            float colorFill = 1.0; //sin((p_seconds*2.5)+(p_count*10.0));
                            float alpha = 0.0;
                            if(x_fill.r > 0.5) {
                                alpha = 0.01;
                            }
                            x_fill.rgba = vec4(1.0, 1.0, 1.0, alpha);
                            """.trimIndent()
                        }.parameter("texture", cbContourList.get((frame + index) % cbContourList.size))
                            .parameter("count", (index))
                            .parameter("seconds", seconds)
                            .parameter("fillIn", ColorRGBa.PINK)
                        drawer.drawStyle.colorMatrix = tint(ColorRGBa.WHITE.opacify(0.03))
                        drawer.image(colorBuffer)
                    }
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


           // extra
            drawer.isolated {

                drawer.fontMap = font
                drawer.fill = ColorRGBa.RED
                drawer.text("$frame realtime ${movingDelta}", 50.0, 50.0)

                if(newList.frames.filter { it.tic == frame && it.height > (height / 1.5)  }.sortedBy { it.height }.isNotEmpty()) {
                    val frameS = newList.frames.filter { it.tic == frame && it.height > (height / 1.5) }.sortedBy { it.height }.last()
                    drawer.stroke = ColorRGBa.RED
                    drawer.strokeWeight = 1.0
                    drawer.fill = null

                    val rect = Rectangle(frameS.x, frameS.y, frameS.width, frameS.height)
                    drawer.rectangle(rect)
                    drawer.strokeWeight = 5.0
                    drawer.circle(rect.center, 30.0)

                    movingX = rect.center.x*0.4 + movingX *0.6
                    movingY = rect.center.y*0.4 + movingY *0.6

                    drawer.stroke = ColorRGBa.GREEN
                    drawer.circle(Vector2(movingX, movingY), 30.0)
                    line.add(Vector2(movingX, movingY))
                    deltaX = Math.abs(movingX - lastXPos)
                    movingDelta = deltaX*0.1 + movingDelta*0.9
                    statDelta.add(Vector2(50+statDelta.size.toDouble(), (200-movingDelta)))

                    drawer.fill = ColorRGBa(1.0, 0.0, 1.0)
                    drawer.text("deltaX $movingDelta", 50+statDelta.size.toDouble(), (200-movingDelta))
                    if(movingDelta < 2.5) {
                        drawer.fill = ColorRGBa.GREEN
                        drawer.text("person still", 50.0, 100.0)
                        m.animateIn()
                    } else {
                        drawer.fill = ColorRGBa.BLUE
                        drawer.text("person moving", 50.0, 100.0)
                        m.animateOut()
                    }

                    lastXPos = movingX
                }

                drawer.strokeWeight = 1.0
                drawer.stroke = ColorRGBa.BLUE
                drawer.lineStrip(line)

                drawer.strokeWeight = 1.0
                drawer.stroke = ColorRGBa(1.0, 0.0, 1.0)
                drawer.lineStrip(statDelta)
            }

            //if(tic%2 == 0) {
                frame ++
           // }
            tic++
        }
    }
}