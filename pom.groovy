import groovy.swing.SwingBuilder
import java.awt.*
import javax.swing.*

class Controller {
  
  def frame
  def finishedFrame
  def buttonStartPom
  def buttonCloseFinished
  boolean runPom = true
  static final POM_LENGTH = 25 * 60
  static final ALARM_REPEAT_COUNT = 5

  void init() {
    frame.show()
    Thread.start {
      def serverSocket = new ServerSocket(3307)
      while(runPom) {
        def socket = serverSocket.accept()
        def bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.outputStream))
        bufferedWriter << "\n"
        bufferedWriter << frame.title
        bufferedWriter << "\n"
        bufferedWriter << "\n"
        bufferedWriter.flush()
        bufferedWriter.close()
        socket.close()
      }
      serverSocket.close()
    }
    Thread.start {
      def serverSocket = new ServerSocket(3308)
      while(runPom) {
        def socket = serverSocket.accept()
        def bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.outputStream))
        def output = """
<html>
  <head>
    <title>Time left</title>
  </head>
  <body>
    <h1>Andy's busy for: ${frame.title}</h1>
    <p>...sorry for any inconvenience caused!</p>
    <p>Best to IM or email him and he will get back to you as soon as he can (i.e. at end of said time when he will help to the utmost of his ability)!</p>
    <p>Please feel free to interrupt if urgent (interruptions dealt with on a boy who cried wolf basis)
    <p>For the command line orientated there's also a telnet interface to this tool running on port 3307.</p>
  </body>
</html>
"""
        bufferedWriter << "HTTP/1.1 200 OK\r\n"
        bufferedWriter << "Content-Type: text/html\r\n"
        bufferedWriter << "Content-Length: ${output.size()}\r\n"
        bufferedWriter << "\r\n"
        bufferedWriter << output
        bufferedWriter.flush()
        bufferedWriter.close()
        socket.close()
      }
      serverSocket.close()
    }
  }

  void buttonStartPom() {
    int secondsLeft = POM_LENGTH
    buttonStartPom.enabled = false
    Thread.start {
      while(secondsLeft + 1 && runPom) {
        def mins = (secondsLeft / 60).toInteger()
        def secs = secondsLeft % 60
        frame.title = "$mins mins, $secs secs"
        sleep(1000)
        secondsLeft--
      }
      if(runPom) {
        finishedFrame.show()
        buttonCloseFinished.enabled = true
        buttonStartPom.enabled = true
        1.upto(ALARM_REPEAT_COUNT) {
            java.awt.Toolkit.getDefaultToolkit().beep()
            sleep(1000)
        }
      }
    }
  }

  void destroy() {
    frame.dispose()
    finishedFrame.dispose()
    runPom = false
    new Socket("localhost", 3307).close()
    new Socket("localhost", 3308).close()
  }

}

controller = new Controller()

/* main frame for starting POMs and closing the application */
controller.frame = new SwingBuilder().frame(
  title: "0 mins, 0 secs",
  size: new Dimension(300, 65),
  location: new Point(500, 500),
  windowClosing: { controller.destroy() }
) {
  controller.buttonStartPom = button(text: "Start Pom", actionPerformed: {controller.buttonStartPom()})
}

/* alert frame that displays the finished method */
controller.finishedFrame = new SwingBuilder().frame(
  title: "POM Finished",
  size: new Dimension(300, 65),
  location: new Point(500, 500),
  windowClosing: {
    controller.finishedFrame.hide()
  }
) {
  controller.buttonCloseFinished = button(text: "Close Window", actionPerformed: {controller.finishedFrame.hide()})
}

controller.init()
