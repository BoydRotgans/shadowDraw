//import com.google.gson.Gson
//import org.openrndr.animatable.Animatable
//import org.openrndr.animatable.Clock
//import org.openrndr.application
//import org.openrndr.color.ColorRGBa
//import org.openrndr.draw.*
//import org.openrndr.ffmpeg.FFMPEGVideoPlayer
//import org.openrndr.ffmpeg.ScreenRecorder
//import org.openrndr.ffmpeg.VideoPlayerFFMPEG
//import org.openrndr.math.Vector2
//import org.openrndr.shape.Rectangle
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
//    var cbList = mutableListOf<ColorBuffer>()
//
//    program {
//
////        extend(ScreenRecorder().apply {
////            contentScale = 1.0
////            frameRate = 60
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
//        val font = FontImageMap.fromUrl("file:data/fonts/IBMPlexMono-Regular.ttf", 64.0)
//
//        drawer.isolatedWithTarget(rt) {
//            drawer.background(ColorRGBa.PINK)
//        }
//
//        val videoPlayer: FFMPEGVideoPlayer = FFMPEGVideoPlayer.fromURL("file:data/videos/demo.mp4")
//        videoPlayer.start()
//
//        var frame = 0
//        var tic = 0
//
//        var correct = 0
//
//        var movingX = 0.0
//        var movingY = 0.0
//        var lastXPos = 0.0
//        var deltaX = 0.0
//        var movingDelta = 0.0
//
//
//        var line = mutableListOf<Vector2>()
//        var statDelta = mutableListOf<Vector2>()
//
//        extend {
//
//            rt = renderTarget(1280, 720) {
//                colorBuffer()
//            }
//
//            drawer.isolatedWithTarget(rt) {
//                if(tic%2 == 0) {
//                   videoPlayer.next()
//                }
//
//                videoPlayer.draw(drawer)
//            }
//
//            drawer.image(rt.colorBuffer(0))
//
//
//            drawer.isolated {
//
//                drawer.fontMap = font
//                drawer.fill = ColorRGBa.RED
//                drawer.text("$frame realtime", 50.0, 50.0)
//
//
//                if(newList.frames.filter { it.tic == frame && it.height > (height / 1.5)  }.sortedBy { it.height }.isNotEmpty()) {
//                    val frameS = newList.frames.filter { it.tic == frame && it.height > (height / 1.5) }.sortedBy { it.height }.last()
//                    drawer.stroke = ColorRGBa.RED
//                    drawer.strokeWeight = 1.0
//                    drawer.fill = null
//
//                    val rect = Rectangle(frameS.x, frameS.y, frameS.width, frameS.height)
//                    drawer.rectangle(rect)
//                    drawer.strokeWeight = 5.0
//                    drawer.circle(rect.center, 30.0)
//
//                    movingX = rect.center.x*0.4 + movingX *0.6
//                    movingY = rect.center.y*0.4 + movingY *0.6
//
//                    drawer.stroke = ColorRGBa.GREEN
//                    drawer.circle(Vector2(movingX, movingY), 30.0)
//
//                    line.add(Vector2(movingX, movingY))
//
//
//
//                    deltaX = Math.abs(movingX - lastXPos)
//
//                    movingDelta = deltaX*0.01 + movingDelta*0.99
//
//                    statDelta.add(Vector2(50+statDelta.size.toDouble(), (200-movingDelta)))
//
//                    if(statDelta.size > width-100) {
//                        statDelta.clear()
//                    }
//
//                    drawer.fill = ColorRGBa(1.0, 0.0, 1.0)
//                    drawer.text("deltaX $movingDelta", 50+statDelta.size.toDouble(), (200-movingDelta))
//
//                    if(movingDelta < 2.0) {
//                        drawer.fill = ColorRGBa.GREEN
//                        drawer.text("person still", 50.0, 100.0)
//                    } else {
//                        drawer.fill = ColorRGBa.BLUE
//                        drawer.text("person moving", 50.0, 100.0)
//                    }
//
//                    lastXPos = movingX
//                }
//
//                drawer.strokeWeight = 1.0
//                drawer.stroke = ColorRGBa.BLUE
//                drawer.lineStrip(line)
//
//                drawer.strokeWeight = 1.0
//                drawer.stroke = ColorRGBa(1.0, 0.0, 1.0)
//                drawer.lineStrip(statDelta)
//
//
//            }
//
//            if(tic%1 == 0) {
//                frame ++
//            }
//
//            tic++
//        }
//    }
//}