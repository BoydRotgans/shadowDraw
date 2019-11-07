import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.FontImageMap
import org.openrndr.draw.loadImage
import org.openrndr.draw.tint
import org.openrndr.ffmpeg.FFMPEGVideoPlayer
import org.openrndr.ffmpeg.VideoPlayerFFMPEG

fun main() = application {
    configure {
        width = 1280
        height = 720
    }

    program {
        lateinit var videoPlayer: FFMPEGVideoPlayer
        videoPlayer = FFMPEGVideoPlayer.fromURL("file:data/videos/demo.mp4")
        videoPlayer.start()
        var frame = 0

        extend {
            println(frame)
            videoPlayer.next()
            videoPlayer.draw(drawer)
            frame++
        }
    }
}