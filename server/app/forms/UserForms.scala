package forms

object UserForms {

  final val MinUsernameSize = 3
  final val MaxUsernameSize = 30

  final val MinPasswordSize = 5
  final val MaxPasswordSize = 30

  sealed trait InvalidUser

  case class LoginForm private (username: Username, password: Password)
  type Login = Either[InvalidUser, LoginForm]

  object LoginForm {

    def apply(
        username: Either[InvalidUsername, Username],
        password: Either[InvalidPassword, Password]
    ): Login = {
      
      for (username <- username; password <- password)
        yield LoginForm(username, password)
    }

    def apply(form: Map[String, Seq[String]]): Login = {
      LoginForm(Username(form), Password(form))
    }
  }

  case class RegisterForm private (username: Username, password: Password)
  type Registration = Either[InvalidUser, RegisterForm]

  object RegisterForm {
    
    def apply(
        username: Either[InvalidUsername, Username],
        password: Either[InvalidPassword, Password]
    ): Registration = {
      
      for (username <- username; password <- password)
        yield RegisterForm(username, password)
    }

    def apply(form: Map[String, Seq[String]]): Registration = {
      RegisterForm(Username(form), Password(form))
    }
  }

  case class Username private (username: String)

  sealed trait InvalidUsername extends InvalidUser
  case object UnknownUsername extends InvalidUser
  case object UsernameTaken extends InvalidUser
  case object UsernameTooShort extends InvalidUsername
  case object UsernameTooLong extends InvalidUsername
  case object IllegalUsernameChar extends InvalidUsername
  case object NoUsername extends InvalidUsername

  object Username {
    
    def apply(username: String): Either[InvalidUsername, Username] = {

      Right(new Username(username))
        .filterOrElse(_.username.size >= MinUsernameSize, UsernameTooShort)
        .filterOrElse(_.username.size <= MaxUsernameSize, UsernameTooLong)
        .filterOrElse(_.username.forall(_.isValidChar), IllegalUsernameChar)
    }

    def apply(form: Map[String, Seq[String]]):
        Either[InvalidUsername, Username] = {
      
      form.get("username") match {
        case Some(Seq(username)) => Username(username)
        case _ => Left(NoUsername)
      }
    }
  }

  case class Password private (password: String)

  sealed trait InvalidPassword extends InvalidUser
  case object IncorrectPassword extends InvalidUser
  case object PasswordTooShort extends InvalidPassword
  case object PasswordTooLong extends InvalidPassword
  case object IllegalPasswordChar extends InvalidPassword
  case object NoPassword extends InvalidPassword

  object Password {
    
    def apply(password: String): Either[InvalidPassword, Password] = {

      Right(new Password(password))
        .filterOrElse(_.password.size >= MinPasswordSize, PasswordTooShort)
        .filterOrElse(_.password.size <= MaxPasswordSize, PasswordTooLong)
        .filterOrElse(_.password.forall(_.isValidChar), IllegalPasswordChar)
    }

    def apply(form: Map[String, Seq[String]]):
        Either[InvalidPassword, Password] = {
      
      form.get("password") match {
        case Some(Seq(password)) => Password(password)
        case _ => Left(NoPassword)
      }
    }
  }
}