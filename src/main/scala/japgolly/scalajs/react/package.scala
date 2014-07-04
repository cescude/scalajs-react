package japgolly.scalajs

import org.scalajs.dom
import scala.scalajs.js._

package object react {

  @inline def React = Dynamic.global.React.asInstanceOf[React]

  type MountedComponent[Props, State, Backend] = ComponentScopeM[Props, State, Backend]

  // ===================================================================================================================

  // TODO WrapObj was one of the first things I did when starting with ScalaJS. Reconsider.
  /** Allows Scala classes to be used in place of `Object`. */
  trait WrapObj[+A] extends Object { val v: A }
  def WrapObj[A](v: A) =
    Dynamic.literal("v" -> v.asInstanceOf[Any]).asInstanceOf[WrapObj[A]]

  /**
   * A named reference to an element in a React VDOM.
   */
  case class Ref[T <: dom.Element](name: String) {
    @inline final def apply(scope: ComponentScope_M): UndefOr[ProxyConstructorM[T]] = apply(scope.refs)
    @inline final def apply(refs: RefsObject): UndefOr[ProxyConstructorM[T]] = refs[T](name)
  }

  class WComponentConstructor[Props, State, Backend](u: ComponentConstructor[Props, State, Backend]) {
    def apply(props: Props, children: Any*) = u(WrapObj(props), children: _*)
  }

  // ===================================================================================================================

  @inline implicit def autoUnWrapObj[A](a: WrapObj[A]): A = a.v
  implicit final class AnyExtReact[A](val a: A) extends AnyVal {
    @inline def wrap: WrapObj[A] = WrapObj(a)
  }

  implicit final class ReactExt(val u: React) extends AnyVal {
    @inline def renderComponentC[P, S, B](c: ProxyConstructorUT[P, S, B], n: dom.Node)(callback: ComponentScopeM[P, S, B] => Unit) =
      u.renderComponent(c, n, callback)
  }

  implicit final class ComponentScope_P_Ext[Props](val u: ComponentScope_P[Props]) extends AnyVal {
    @inline def props = u._props.v
  }

  implicit final class ComponentScope_S_Ext[State](val u: ComponentScope_S[State]) extends AnyVal {
    @inline def state = u._state.v
  }

  implicit final class ComponentScope_SS_Ext[State](val u: ComponentScope_SS[State]) extends AnyVal {
    @inline def setState(s: State): Unit = u._setState(WrapObj(s))
    @inline def setState(s: State, callback: => Unit): Unit = u._setState(WrapObj(s), (() => callback): Function)
    @inline def modState(f: State => State) = u.setState(f(u.state))
  }

  implicit final class SyntheticEventExt[N <: dom.Node](val u: SyntheticEvent[N]) extends AnyVal {
    def keyboardEvent = u.nativeEvent.asInstanceOf[dom.KeyboardEvent]
    def messageEvent  = u.nativeEvent.asInstanceOf[dom.MessageEvent]
    def mouseEvent    = u.nativeEvent.asInstanceOf[dom.MouseEvent]
    def mutationEvent = u.nativeEvent.asInstanceOf[dom.MutationEvent]
    def storageEvent  = u.nativeEvent.asInstanceOf[dom.StorageEvent]
    def textEvent     = u.nativeEvent.asInstanceOf[dom.TextEvent]
    def touchEvent    = u.nativeEvent.asInstanceOf[dom.TouchEvent]
  }

  implicit final class UndefProxyConstructorMExt[T <: dom.HTMLElement](val u: UndefOr[ProxyConstructorM[T]]) extends AnyVal {
    def tryFocus(): Unit = u.foreach(_.getDOMNode().focus())
  }

  implicit final class ProxyConstructorUTExt[Props, State, Backend](val u: ProxyConstructorUT[Props, State, Backend]) extends AnyVal {
    def render(n: dom.Node) = React.renderComponent(u, n)
  }
}