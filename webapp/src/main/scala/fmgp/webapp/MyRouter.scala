package fmgp.webapp

import com.raquo.laminar.api.L.{_, given}
import com.raquo.waypoint._
import org.scalajs.dom
import upickle.default._
import fmgp.did.comm.OutOfBand
import fmgp.did.comm.Payload.base64url

object MyRouter {
  sealed abstract class Page(
      val title: String,
      val icon: String // https://fonts.google.com/icons?selected=Material+Icons+Outlined
  )

  case object HomePage extends Page("Home", "home")
  case object SettingsPage extends Page("Settings", "settings")
  case class OOBPage(query_oob: String) extends Page("OutOfBand", "app_shortcut")
  object OOBPage { def apply(oob: OutOfBand) = new OOBPage(oob.data.urlBase64) }
  case object QRcodeScannerPage extends Page("QRcodeScanner", "qr_code_scanner")
  case object NFCScannerPage extends Page("NFCScanner", "nfc")
  case object DocPage extends Page("Doc", "menu_book")
  case object AgentManagementPage extends Page("AgentManagement", "manage_accounts")
  // case object DIDPage extends Page("DID", "visibility")
  // case object AgentDBPage extends Page("MessageDB", "folder_open")
  case object AgentMessageStoragePage extends Page("AgentMessageStorage", "forum")
  case class ResolverPage(did: String) extends Page("Resolver", "dns")
  case object EncryptPage extends Page("Encrypt", "enhanced_encryption")
  case object DecryptPage extends Page("Decrypt", "email")
  case object BasicMessagePage extends Page("BasicMessage", "message")
  case object TrustPingPage extends Page("TrustPing", "network_ping")
  case object TapIntoStreamPage extends Page("TapIntoStream", "chat")
  case object DAppStorePage extends Page("DAppStore", "share")
  case object MediatorPage extends Page("Mediator", "diversity_3")

  given homePageRW: ReadWriter[HomePage.type] = macroRW
  given settingsPageRW: ReadWriter[SettingsPage.type] = macroRW
  given oobPageRW: ReadWriter[OOBPage] = macroRW
  given qrcodeScannerPageRW: ReadWriter[QRcodeScannerPage.type] = macroRW
  given nfcScannerPageRW: ReadWriter[NFCScannerPage.type] = macroRW
  given docPageRW: ReadWriter[DocPage.type] = macroRW
  given keysPageRW: ReadWriter[AgentManagementPage.type] = macroRW
  // given agentDBPageRW: ReadWriter[AgentDBPage.type] = macroRW
  given agentMessageStoragePageRW: ReadWriter[AgentMessageStoragePage.type] = macroRW
  given resolverPageRW: ReadWriter[ResolverPage] = macroRW
  given encryptPageRW: ReadWriter[EncryptPage.type] = macroRW
  given decryptPageRW: ReadWriter[DecryptPage.type] = macroRW
  given basicMessagePageRW: ReadWriter[BasicMessagePage.type] = macroRW
  given trustPingPageRW: ReadWriter[TrustPingPage.type] = macroRW
  given tapIntoStreamPageRW: ReadWriter[TapIntoStreamPage.type] = macroRW
  given dAppStorePageRW: ReadWriter[DAppStorePage.type] = macroRW
  given mediatorPageRW: ReadWriter[MediatorPage.type] = macroRW

  given rw: ReadWriter[Page] = macroRW

  private val routes = List(
    // http://localhost:8080/?_oob=eyJ0eXBlIjoiaHR0cHM6Ly9kaWRjb21tLm9yZy9vdXQtb2YtYmFuZC8yLjAvaW52aXRhdGlvbiIsImlkIjoiNTk5ZjM2MzgtYjU2My00OTM3LTk0ODctZGZlNTUwOTlkOTAwIiwiZnJvbSI6ImRpZDpleGFtcGxlOnZlcmlmaWVyIiwiYm9keSI6eyJnb2FsX2NvZGUiOiJzdHJlYW1saW5lZC12cCIsImFjY2VwdCI6WyJkaWRjb21tL3YyIl19fQ
    Route.onlyQuery[OOBPage, String]( // OOB
      encode = page => page.query_oob,
      decode = arg => OOBPage(query_oob = arg),
      pattern = (root / endOfSegments) ? (param[String]("_oob")),
      Router.localFragmentBasePath
    ),
    Route[ResolverPage, String](
      encode = page => page.did,
      decode = arg => ResolverPage(did = arg),
      pattern = root / "resolver" / segment[String] / endOfSegments,
      Router.localFragmentBasePath
    ),
    Route.static(HomePage, root / endOfSegments, Router.localFragmentBasePath),
    Route.static(SettingsPage, root / "settings" / endOfSegments, Router.localFragmentBasePath),
    Route.static(DocPage, root / "doc" / endOfSegments, Router.localFragmentBasePath),
    Route.static(QRcodeScannerPage, root / "scanner" / endOfSegments, Router.localFragmentBasePath),
    Route.static(NFCScannerPage, root / "nfc" / endOfSegments, Router.localFragmentBasePath),
    Route.static(AgentManagementPage, root / "agentkeys" / endOfSegments, Router.localFragmentBasePath),
    // Route.static(AgentDBPage, root / "db" / endOfSegments, Router.localFragmentBasePath),
    Route.static(AgentMessageStoragePage, root / "agent" / endOfSegments, Router.localFragmentBasePath),
    Route.static(EncryptPage, root / "encrypt" / endOfSegments, Router.localFragmentBasePath),
    Route.static(DecryptPage, root / "decrypt" / endOfSegments, Router.localFragmentBasePath),
    Route.static(BasicMessagePage, root / "basicmessage" / endOfSegments, Router.localFragmentBasePath),
    Route.static(TrustPingPage, root / "trustping" / endOfSegments, Router.localFragmentBasePath),
    Route.static(TapIntoStreamPage, root / "stream" / endOfSegments, Router.localFragmentBasePath),
    Route.static(DAppStorePage, root / "dapp" / endOfSegments, Router.localFragmentBasePath),
    Route.static(MediatorPage, root / "mediator" / endOfSegments, Router.localFragmentBasePath),
  )

  val router = new Router[Page](
    routes = routes,
    getPageTitle = _.title, // displayed in the browser tab next to favicon
    serializePage = page => write(page)(rw), // serialize page data for storage in History API log
    deserializePage = pageStr => read(pageStr)(rw), // deserialize the above
    routeFallback = { (_: String) => HomePage },
  )(
    popStateEvents = windowEvents(_.onPopState), // this is how Waypoint avoids an explicit dependency on Laminar
    owner = unsafeWindowOwner // this router will live as long as the window
  )

  // Note: for fragment ('#') URLs this isn't actually needed.
  // See https://github.com/raquo/Waypoint docs for why this modifier is useful in general.
  def navigateTo(page: Page): Binder[HtmlElement] = Binder { el =>

    val isLinkElement = el.ref.isInstanceOf[dom.html.Anchor]

    if (isLinkElement) {
      el.amend(href(router.absoluteUrlForPage(page)))
    }

    // If element is a link and user is holding a modifier while clicking:
    //  - Do nothing, browser will open the URL in new tab / window / etc. depending on the modifier key
    // Otherwise:
    //  - Perform regular pushState transition
    (onClick
      .filter(ev => !(isLinkElement && (ev.ctrlKey || ev.metaKey || ev.shiftKey || ev.altKey)))
      .preventDefault
      --> (_ => router.pushState(page))).bind(el)
  }
}
