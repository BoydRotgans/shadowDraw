import com.google.gson.Gson
import org.openrndr.animatable.Animatable
import org.openrndr.animatable.Clock
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.ffmpeg.FFMPEGVideoPlayer
import org.openrndr.ffmpeg.MP4Profile
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.ffmpeg.VideoPlayerFFMPEG
import org.openrndr.filter.blur.GaussianBlur
import org.openrndr.math.Vector2
import java.io.File

fun main() = application {
    configure {
        width = 1920
        height = 1080
    }

    val gson = Gson()
//    val jsonString = File("data/data/data-n.json").readText()
//    val newList = gson.fromJson(jsonString, FrameRect::class.java)

    var cbList = mutableListOf<ColorBuffer>()
    var cbBackList = mutableListOf<ColorBuffer>()
    var cbBackList2 = mutableListOf<ColorBuffer>()



    program {


        var rt = renderTarget(1920, 1080) {
            colorBuffer()
        }

        var rtb = renderTarget(1280, 720) {
            colorBuffer()
        }

        val rtb2 = renderTarget(1920, 1080) {
            colorBuffer()
        }

        drawer.isolatedWithTarget(rt) {
            drawer.background(ColorRGBa.PINK)
        }

        lateinit var videoPlayer: FFMPEGVideoPlayer
        lateinit var backdrop: FFMPEGVideoPlayer

        videoPlayer = FFMPEGVideoPlayer.fromURL("file:data/frames/Mask_OUTPUT_2.mp4")
        videoPlayer.start()
        backdrop = FFMPEGVideoPlayer.fromURL("file:data/frames/RGB_OUTPUT_1.mp4")
        backdrop.start()
        //var img = loadImage("data/images/space.png")
        var frame = 0
        var correct = 0
//
        extend(ScreenRecorder().apply {
            contentScale = 1.0
            frameRate = 60
            multisample = BufferMultisample.SampleCount(8)
//            maximumDuration = 350.0
            profile = MP4Profile()
        })

        var blur = GaussianBlur()

        extend {

            rt = renderTarget(1920, 1080) {
                colorBuffer()
            }

            drawer.isolatedWithTarget(rt) {
                videoPlayer.next()
                translate(Vector2(3.0, 3.0))
                videoPlayer.draw(drawer)
            }

            rtb = renderTarget(1920, 1080) {
                colorBuffer()
            }

            drawer.isolatedWithTarget(rtb) {
                backdrop.next()
                backdrop.draw(drawer)
            }

            drawer.isolatedWithTarget(rtb2) {
                backdrop.draw(drawer)
//                drawer.fill = null
//                drawer.stroke =ColorRGBa.BLACK
//                drawer.strokeWeight = 0.5
//                var stepX = width / 100.0
//                var stepY = height / 1.0
//
//
//
//                for(y in 0..1) {
//                    for( x in 0..100) {
//
//
//                        drawer.rectangle(x*stepX, y*stepY, stepX, stepY)
//                    }
//                }
            }

            if(cbList.size > 150) {
                cbList.removeAt(0)
                cbBackList.removeAt(0)
                cbBackList2.removeAt(0)
                correct++
            }
//            val silhoutete = colorBuffer(width, height)
//            blur.apply {
////                window = 3
//                spread = 1.0
////                gain = 1.5
//            }
//            blur.apply(rt.colorBuffer(0), silhoutete)

            cbList.add(rt.colorBuffer(0))
            cbBackList.add(rtb.colorBuffer(0))
            cbBackList2.add(rtb2.colorBuffer(0))

            drawer.background(ColorRGBa.BLACK)

            drawer.image(rtb.colorBuffer(0))
            blur.apply {
                spread = 3.0

//                gain = 1.5
            }
            val canvas = colorBuffer(width, height)


            drawer.isolated {
                cbList.forEachIndexed { index, colorBuffer ->
//                    blur.apply {
//                        spread =  5.0 + Math.abs(Math.sin(seconds*0.1+index*0.1)) * 10.0
//
////                gain = 1.5
//                    }
//                    blur.apply( cbBackList.get( index), canvas)
                    drawer.shadeStyle = shadeStyle {
                        fragmentTransform = """
                        vec2 textSize = textureSize(p_texture, 0);
                        //vec2 uv = (c_screenPosition + vec2(p_count, 0.0) ) /  textSize;
                        //vec2 uv = (c_screenPosition - vec2( 0.0, abs(cos(((p_count*0.1)-p_seconds*0.5)))*10.0)) / textSize;
                        vec2 uv = (c_screenPosition) /  textSize;

                        vec4 image = texture(p_texture,  1.0-vec2(1.0-uv.x, uv.y)).rgba;
                        float colorFill = 1.0  - (sin((p_seconds*0.01)+(p_count*0.1))) *0.25 ; //  1.0; //
                        
                        float alpha = 1.0;
                        if(x_fill.r < 0.5) {
                            alpha = 0.0;
                        }
                        
                        x_fill.rgba = vec4( image.r*colorFill,  image.g*colorFill, image.b*colorFill, alpha);
                        """.trimIndent()
                    }.parameter("texture", cbBackList.get( index))
                     .parameter("count", (index))
                     .parameter("seconds", seconds)
                     .parameter("fillIn", ColorRGBa.PINK)

                    drawer.image(colorBuffer)
                }
            }







            drawer.isolated {
                drawer.shadeStyle = shadeStyle {
                    fragmentTransform = """

                        vec2 textSize = textureSize(p_texture, 0);
                        vec2 uv = (c_screenPosition) / textSize;
                        vec4 image = texture(p_texture,  1.0-vec2(1.0-uv.x, uv.y)).rgba;

                        float alpha = 1.0;
                        if(x_fill.r < 0.5) {
                            alpha = 0.0;
                        }
                        x_fill.rgba = vec4( image.r, image.g, image.b, alpha);
                        """.trimIndent()
                }.parameter("texture", cbBackList2.get((frame)%cbBackList2.size))
                drawer.image(rt.colorBuffer(0))
            }




            frame++
        }
    }
}