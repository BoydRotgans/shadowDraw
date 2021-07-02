//import com.google.gson.Gson
//import org.openrndr.application
//import org.openrndr.color.ColorRGBa
//import org.openrndr.draw.FontImageMap
//import org.openrndr.draw.loadImage
//import org.openrndr.draw.tint
//import org.openrndr.ffmpeg.FFMPEGVideoPlayer
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
//    program {
//        lateinit var videoPlayer: FFMPEGVideoPlayer
//        videoPlayer = FFMPEGVideoPlayer.fromURL("file:data/videos/demo.mp4")
//        videoPlayer.start()
//        var frame = 0
//
//        extend {
//            //println(frame)
//            videoPlayer.next()
//            videoPlayer.draw(drawer)
//
//            drawer.stroke = ColorRGBa.RED
//            drawer.fill = null
//            newList.frames.filter { it.tic == frame }.forEachIndexed { index, frame ->
//                drawer.rectangle(frame.x, frame.y, frame.width, frame.height)
//            }
//
//            frame++
//        }
//    }
//}