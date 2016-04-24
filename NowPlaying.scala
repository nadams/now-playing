import java.io.File
import java.net.URLDecoder
import scala.sys.process.{ Process, ProcessIO }
import scala.io.Source

object NowPlaying {
	def main(args: Array[String]): Unit = {
	  val processBuilder = Process("qdbus-qt5 org.mpris.MediaPlayer2.audacious /org/mpris/MediaPlayer2 org.mpris.MediaPlayer2.Player.Metadata")
	  val processIo = new ProcessIO(
	  	_ => (),
	  	stdout => {
	  		val wantedLines = Source.fromInputStream(stdout).getLines().map { line =>
	  			val values = line.split(": ")

	  			if(values.length > 0) {
	  				(values(0), values.drop(1).foldLeft("")((x, y) => x + y))
	  			} else {
	  				("", "")
	  			}
	  		}.toMap.filter(x => x._1 == "xesam:title" || x._1 == "xesam:artist" || x._1 == "xesam:location")

	  		val nowPlayingResult = NowPlayingResult(wantedLines.get("xesam:title"), wantedLines.get("xesam:artist"), wantedLines.get("xesam:location"))

	  		nowPlayingResult match {
	  			case NowPlayingResult(None, None, location) => { 
	  				val filename = new File(location.get).getName

	  				val decodedFilename = URLDecoder.decode(filename, "UTF-8")

	  				println(s"/me is playing $decodedFilename")
	  			}
	  			case NowPlayingResult(title, artist, _) => println(s"/me is playing ${artist.get} - ${title.get}")
	  			case _ => println("/me is playing I have no idea")
	  		}
	  	},
	  	_ => ()
	 	)

	 	processBuilder.run(processIo)
	}
}

case class NowPlayingResult(title: Option[String], artist: Option[String], absolutePath: Option[String])
