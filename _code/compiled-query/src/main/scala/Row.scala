trait Row

case class DissertationRow(
  name: Option[String] = None,
  birthYear: Option[Int] = None,
  dissertation: Option[String] = None) extends Row {

  def setValueForOrdinal[A](ord: Int, value: A): DissertationRow =
    ord match {
      case 0 => this.copy(name = Some(value.asInstanceOf[String]))
      case 1 => this.copy(birthYear = Some(value.asInstanceOf[Int]))
      case 2 => this.copy(dissertation = Some(value.asInstanceOf[String]))
    }
}
