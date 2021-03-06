package scalajs.chat

import scala.scalajs.js
import org.scalajs.dom
import org.scalajs.jquery._
import org.scalajs.dom.WebSocket
import org.scalajs.dom.MessageEvent
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.Any.fromFunction1
import scala.scalajs.js.Any.fromString
import scala.scalajs.js.Any.stringOps
import shared._
import scalajs.chat.{ ChatMessagesTransformer => transformer }
import org.scalajs.spickling._
import org.scalajs.spickling.jsany._
import java.util.NoSuchElementException

@JSExport
object ChatClient {

  lazy val host = dom.location.host

  private def chatUrl = s"ws://$host/chat/$user"

  private def user = jQuery("#user").value.toString
  private def message = jQuery("#message").value.toString

  var ws: WebSocket = _

  @JSExport
  def main(args: Array[String]) {
    jQuery("#chat").hide

    jQuery("#login-button").click(() => {
      ws = new WebSocket(chatUrl)
      ws.onmessage = onMessage

      jQuery("#login").hide
      jQuery("#chat").show
      jQuery("#message").focus
    })

    jQuery("#message-button").click(() => {
      val json = transformer.asJson(ChatMessage(user, message))
      ws.send(json)
      jQuery("#message").value("")
      jQuery("#message").focus
    })

  }

  private def onMessage: (MessageEvent) => Unit = (message: MessageEvent) => {
    val json = message.data.toString
    transformer.fromJson(json) map (_ match {
      case JoinChat(user) => s"$user joined"
      case ChatMessage(user, message) => s"$user > $message"
      case ExitChat(user) => s"$user left"
    }) map { msg =>
      val p = jQuery("<p>").html(s"$msg")
      jQuery("#messages").append(p)
      jQuery("#messages").scrollTop(jQuery("#messages").height)
    } getOrElse {
      dom.console.log(s"could not parse message: $json")
    }
  }
}