//import com.google.gson.Gson
//import org.openrndr.animatable.Animatable
//import org.openrndr.animatable.Clock
//import org.openrndr.application
//import org.openrndr.color.ColorRGBa
//import org.openrndr.draw.*
//import org.openrndr.ffmpeg.FFMPEGVideoPlayer
//import org.openrndr.ffmpeg.ScreenRecorder
//import org.openrndr.ffmpeg.VideoPlayerFFMPEG
//import java.io.File
//
//fun main() = application {
//    configure {
//        width = 1280
//        height = 720
//    }
//
//    val gson = Gson()
//    val jsonString = File("data/data/data-n.json").readText()
//    val newList = gson.fromJson(jsonString, FrameRect::class.java)
//
//    var cbList = mutableListOf<ColorBuffer>()
//    var cbBackList = mutableListOf<ColorBuffer>()
//
//
//
//    program {
//
////        extend(ScreenRecorder().apply {
////            contentScale = 1.0
////            frameRate = 30
////        })
////
////        Animatable.clock(object: Clock {
////            override val time: Long
////                get() {
////                    return (seconds*1000).toLong()
////                }
////        })
//
//        var rt = renderTarget(1280, 720) {
//            colorBuffer()
//        }
//
//        var rtb = renderTarget(1280, 720) {
//            colorBuffer()
//        }
//
//        drawer.isolatedWithTarget(rt) {
//            drawer.background(ColorRGBa.PINK)
//        }
//
//        lateinit var videoPlayer: FFMPEGVideoPlayer
//        lateinit var backdrop: FFMPEGVideoPlayer
//
//        videoPlayer = FFMPEGVideoPlayer.fromURL("file:data/videos/demo.mp4")
//        videoPlayer.start()
//        backdrop = FFMPEGVideoPlayer.fromURL("file:data/videos/IMG_0811r.mov")
//        backdrop.start()
//        var frame = 0
//        var correct = 0
//
//        extend {
//
//            rt = renderTarget(1280, 720) {
//                colorBuffer()
//            }
//
//            drawer.isolatedWithTarget(rt) {
//                videoPlayer.next()
//                videoPlayer.draw(drawer)
//            }
//
//            rtb = renderTarget(1280, 720) {
//                colorBuffer()
//            }
//
//            drawer.isolatedWithTarget(rtb) {
//                backdrop.next()
//                backdrop.draw(drawer)
//            }
//
//            if(cbList.size > 140) {
//                cbList.removeAt(0)
//                cbBackList.removeAt(0)
//                correct++
//            }
//
//            cbList.add(rt.colorBuffer(0))
//            cbBackList.add(rtb.colorBuffer(0))
//
//            drawer.background(ColorRGBa.PINK)
//
//            drawer.isolated {
//                cbList.forEachIndexed { index, colorBuffer ->
//                    drawer.shadeStyle = shadeStyle {
//                        fragmentTransform = """
//                        vec2 textSize = textureSize(p_texture, 0);
//                        vec2 uv = 1.0 - c_screenPosition /  textSize;
//                        vec4 image = texture(p_texture, uv).rgba;
//
//                        float colorFill = 1.0; //sin((p_seconds*2.5)+(p_count*10.0));
//                        float alpha = 1.0;
//                        if(x_fill.r > 0.5) {
//                            alpha = 0.0;
//                        }
////                        if(p_count%50 > 0) {
////                            alpha = 0.0;
////                        }
//
//                        x_fill.rgba = vec4( image.r*colorFill,  image.g*colorFill, image.b*colorFill, alpha);
//                        """.trimIndent()
//                    }.parameter("texture", cbBackList.get((frame+index)%cbBackList.size))
//                     .parameter("count", (index))
//                     .parameter("seconds", seconds)
//                     .parameter("fillIn", ColorRGBa.PINK)
//                    drawer.image(colorBuffer)
//                }
//            }
//
//            drawer.isolated {
//                drawer.shadeStyle = shadeStyle {
//                    fragmentTransform = """
//                        float alpha = 1.0;
//                        if(x_fill.r > 0.5) {
//                            alpha = 0.0;
//                        }
//                        x_fill.rgba = vec4( x_fill.r,  x_fill.g,  x_fill.b, alpha);
//                        """.trimIndent()
//                }
//                drawer.image(rt.colorBuffer(0))
//            }
//
//            drawer.isolated {
//
//                newList.frames.filter { it.tic == frame }.forEachIndexed { index, frame ->
//                    drawer.stroke = ColorRGBa.RED
//                    drawer.fill = null
//
//                    drawer.rectangle(frame.x, frame.y, frame.width, frame.height)
//                }
//            }
//
//
//            frame++
//        }
//    }
//}