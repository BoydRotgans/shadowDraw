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
//    var cbBackList2 = mutableListOf<ColorBuffer>()
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
//        var rtb2 = renderTarget(1280, 720) {
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
//        backdrop = FFMPEGVideoPlayer.fromURL("file:data/videos/air3.mov")
//        backdrop.start()
//        //var img = loadImage("data/images/space.png")
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
//
//            }
//
//            rtb = renderTarget(1280, 720) {
//                colorBuffer()
//            }
//
//            drawer.isolatedWithTarget(rtb) {
//                backdrop.next()
//                backdrop.draw(drawer)
//
//
//            }
//
//            drawer.isolatedWithTarget(rtb2) {
//                backdrop.draw(drawer)
////                drawer.fill = null
////                drawer.stroke =ColorRGBa.BLACK
////                drawer.strokeWeight = 0.5
////                var stepX = width / 100.0
////                var stepY = height / 1.0
////
////
////
////                for(y in 0..1) {
////                    for( x in 0..100) {
////
////
////                        drawer.rectangle(x*stepX, y*stepY, stepX, stepY)
////                    }
////                }
//            }
//
//            if(cbList.size > 140) {
//                cbList.removeAt(0)
//                cbBackList.removeAt(0)
//                cbBackList2.removeAt(0)
//                correct++
//            }
//
//            cbList.add(rt.colorBuffer(0))
//            cbBackList.add(rtb.colorBuffer(0))
//            cbBackList2.add(rtb2.colorBuffer(0))
//
//            drawer.background(ColorRGBa.PINK)
//
//            drawer.image(rtb.colorBuffer(0))
//
//            drawer.isolated {
//                cbList.forEachIndexed { index, colorBuffer ->
//                    drawer.shadeStyle = shadeStyle {
//                        fragmentTransform = """
//                        vec2 textSize = textureSize(p_texture, 0);
//                        //vec2 uv = (c_screenPosition + vec2(p_count, 0.0) ) /  textSize;
//                        vec2 uv = (c_screenPosition - vec2( (cos(  ((p_count*0.1)+p_seconds*0.5)))*40.0, 0.0)) /  textSize;
//                        vec4 image = texture(p_texture,  1.0-vec2(1.0-uv.x, uv.y)).rgba;
//                        float colorFill = 1.0; //sin((p_seconds*2.5)+(p_count*10.0));
//                        float alpha = 1.0;
////                        if(p_count%50 > 25) {
////                            alpha = 0.25;
////                        }
//                        if(x_fill.r > 0.5) {
//                            alpha = 0.0;
//                        }
//
//
//                        x_fill.rgba = vec4( image.r*colorFill,  image.g*colorFill, image.b*colorFill, alpha);
//                        """.trimIndent()
//                    }.parameter("texture", cbBackList2.get((frame+index)%cbBackList2.size))
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