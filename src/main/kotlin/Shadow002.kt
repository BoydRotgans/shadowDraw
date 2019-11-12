import com.google.gson.Gson
import org.openrndr.animatable.Animatable
import org.openrndr.animatable.Clock
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.ffmpeg.FFMPEGVideoPlayer
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.ffmpeg.VideoPlayerFFMPEG
import java.io.File

fun main() = application {
    configure {
        width = 1280
        height = 720
    }

    val gson = Gson()
    val jsonString = File("data/data/data-n.json").readText()
    val newList = gson.fromJson(jsonString, FrameRect::class.java)

    var cbList = mutableListOf<ColorBuffer>()



    program {

        extend(ScreenRecorder().apply {
            contentScale = 1.0
            frameRate = 60
        })

        Animatable.clock(object: Clock {
            override val time: Long
                get() {
                    return (seconds*1000).toLong()
                }
        })

        var rt = renderTarget(1280, 720) {
            colorBuffer()
        }

        drawer.isolatedWithTarget(rt) {
            drawer.background(ColorRGBa.PINK)
        }

        lateinit var videoPlayer: FFMPEGVideoPlayer
        videoPlayer = FFMPEGVideoPlayer.fromURL("file:data/videos/demo.mp4")
        videoPlayer.start()
        var frame = 0

        extend {

            rt = renderTarget(1280, 720) {
                colorBuffer()
            }

            drawer.isolatedWithTarget(rt) {
                videoPlayer.next()
                videoPlayer.draw(drawer)
            }

            if(cbList.size > 340) {
                cbList.removeAt(0)
            }

            cbList.add(rt.colorBuffer(0))

            drawer.background(ColorRGBa.PINK)

            drawer.isolated {
                cbList.forEachIndexed { index, colorBuffer ->
                    drawer.shadeStyle = shadeStyle {
                        fragmentTransform = """
                        float colorFill = 0.0; //sin((p_seconds*0.1)+(p_count*0.5));
                        float alpha = 1.0;
                        if(x_fill.r > 0.5) {
                            alpha = 0.0;
                        } 
                        if(p_count%100 > 0) {
                            alpha = 0.0;
                        }
                        
                        x_fill.rgba = vec4( p_fillIn.r*colorFill,  p_fillIn.g*colorFill, p_fillIn.b*colorFill, alpha);
                        """.trimIndent()
                    }.parameter("count", (cbList.size - index))
                     .parameter("seconds", seconds)
                        .parameter("fillIn", ColorRGBa.PINK)
                    drawer.image(colorBuffer)
                }
            }


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
                drawer.image(rt.colorBuffer(0))
            }

            drawer.isolated {

                newList.frames.filter { it.tic == frame }.forEachIndexed { index, frame ->
                    drawer.stroke = ColorRGBa.RED
                    drawer.fill = null

                    drawer.rectangle(frame.x, frame.y, frame.width, frame.height)
                }
            }


            frame++
        }
    }
}