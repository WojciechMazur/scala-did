package fmgp.webapp

import scala.scalajs.js.annotation.JSExportTopLevel
import scala.scalajs.js.annotation.JSExport

import org.scalajs.dom
import com.raquo.laminar.codecs._
import com.raquo.laminar.api.L._

import MyRouter._

@JSExportTopLevel("AppUtils")
object AppUtils { // TODO RENAME TO UIUtils

  def onEnterPress = onKeyPress.filter(_.keyCode == dom.KeyCode.Enter)

  val menuClickObserver = Observer[dom.MouseEvent](onNext = ev => {
    import typings.materialDrawer.mod.MDCDrawer
    val tmp = MDCDrawer.attachTo(dom.window.document.querySelector(".mdc-drawer"))
    tmp.open_=(!tmp.open)
  })

  val optionsClickObserver = Observer[dom.MouseEvent](onNext = ev => {
    import typings.materialMenu.mod.MDCMenu
    val tmp = MDCMenu.attachTo(dom.window.document.querySelector(".mdc-menu"))
    tmp.open_=(!tmp.open)
  })

  def topBarHeader(title: Signal[String]) = { // (title: String) = {
    val menuButton = button(
      className("material-icons mdc-top-app-bar__navigation-icon mdc-icon-button"),
      aria.label("Options"),
      onClick --> menuClickObserver,
      "menu"
    )
    typings.materialRipple.mod.MDCRipple.attachTo(menuButton.ref)

    def makeLi(didName: String, icon: String) =
      li(
        className("mdc-list-item"),
        role("menuitem"),
        span(className("mdc-list-item__ripple")),
        i(className("material-icons mdc-list-item__graphic"), icon), // FIXME icon make a make a clone of icon
        // span(
        //   className("mdc-list-item__graphic mdc-menu__selection-group-icon"),
        //   i(aria.label("Atomium"), atomiumSVG)
        // ),
        span(className("mdc-list-item__text"), didName),
        onClick --> Observer[org.scalajs.dom.MouseEvent](onNext = ev => Global.selectAgentByName(didName))
      )

    val options = {
      div(
        className("mdc-menu mdc-menu-surface"),
        minWidth("200px"),
        ul(
          className("mdc-list"),
          role("menu"),
          aria.hidden(true),
          aria.orientation("vertical"),
          tabIndex(-1),
          li(className("mdc-list-divider"), role("separator")),
          li(
            ul(
              className("mdc-menu__selection-group"),
              children <-- Global.dids.map {
                _.map { did => makeLi(did, "person_outline") } :+
                  makeLi(Global.noneOption, "person_outline")
              }
            )
          ),
        )
      )
    }

    val optionsButton = button(
      className("material-icons mdc-top-app-bar__navigation-icon mdc-icon-button"),
      aria.label("Open navigation menu"),
      onClick --> optionsClickObserver,
      "more_vert"
    )
    typings.materialMenu.mod.MDCMenu.attachTo(options.ref)

    headerTag(
      className("mdc-top-app-bar"),
      div(
        className("mdc-top-app-bar__row"),
        sectionTag(
          className("mdc-top-app-bar__section mdc-top-app-bar__section--align-start"),
          menuButton,
          span(className("mdc-top-app-bar__title"), child.text <-- title)
        ),
        sectionTag(
          className("mdc-top-app-bar__section mdc-top-app-bar__section--align-end"),
          role("toolbar"),
          a(
            className("material-icons mdc-top-app-bar__action-item mdc-icon-button"),
            href("https://github.com/FabioPinheiro/fmgp-generative-design"),
            i(aria.label("Github")),
          ),
          // button(
          //   className("material-icons mdc-top-app-bar__action-item mdc-icon-button"),
          //   aria.label("Search"),
          //   "search"
          // ),
          select(
            value <-- Global.selectAgent,
            onChange.mapToValue.map(e => Global.providerNow.getAgentByName(e)) --> Global.agentVar,
            children <-- Global.dids.map {
              _.map { step => option(value := step, step) } :+
                option(value := Global.noneOption, selected := true, Global.noneOption)
            }
          ),
          div(
            className("mdc-menu-surface--anchor"),
            optionsButton,
            options
          )
        ),
      ),
    )
  }

  val drawerScrim = div(className("mdc-drawer-scrim"))
  def drawer(linkPages: List[Page], currentPage: Signal[Page]) =
    asideTag(
      className("mdc-drawer mdc-drawer--modal"),
      div(
        className("mdc-drawer__header"),
        h3(className("mdc-drawer__title"), "FMGP scala-did"),
        h6(className("mdc-drawer__subtitle"), "fabiomgpinheiro@gmail.com"),
      ),
      div(
        className("mdc-drawer__content"),
        navTag(
          className("mdc-list"),
          linkPages.map(page =>
            a(
              className <-- currentPage.map { p =>
                if (p == page) "mdc-list-item mdc-list-item--activated" else "mdc-list-item"
              },
              aria.current := "page",
              tabIndex(0),
              span(className("mdc-list-item__ripple")),
              i(
                className("material-icons mdc-list-item__graphic"),
                aria.hidden(true),
                page.icon
              ),
              navigateTo(page),
              span(className("mdc-list-item__text"), page.title),
            ),
          )
        )
      )
    )

  def myButton(text: String) = {
    div(
      className("mdc-touch-target-wrapper"),
      button(
        className("mdc-button mdc-button--touch mdc-button--raised"),
        span(className("mdc-button__ripple")),
        span(className("mdc-button__touch")),
        span(className("mdc-button__label"), text),
      )
    )

  }

}
