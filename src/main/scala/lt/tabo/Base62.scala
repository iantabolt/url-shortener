package lt.tabo

object Base62 {
  val Chars: IndexedSeq[Char] = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')
  val CharIndex: Map[Char, Int] = Chars.zipWithIndex.toMap
  val Base: Int = 62

  def encode(i: Int): String = {
    if (i == 0) {
      ""
    } else {
      encode(i / 62) + Chars(i % 62)
    }
  }

  def decode(s: String): Int = {
    (0 /: s) { (sum, char) =>
      (sum * Base) + CharIndex(char)
    }
  }
}
