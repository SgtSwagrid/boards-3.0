package games.core

case class History[S <: State.AnyState] (
  state: S,
  action: Option[Action] = None,
  previous: Option[History[S]] = None
) {
  
  def update(f: S => S) = copy(state = f(state))

  def push(action: Action, state: S) = {
    History(state, Some(action), Some(this))
  }

  def push(possibility: (Action, S)) = {
    val (action, state) = possibility
    History(state, Some(action), Some(this))
  }
}