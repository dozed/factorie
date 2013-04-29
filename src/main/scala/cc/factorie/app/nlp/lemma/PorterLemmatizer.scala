package cc.factorie.app.nlp.lemma
import cc.factorie.app.nlp._

class PorterLemmatizer extends DocumentAnnotator {
  def process1(document:Document): Document = {
    for (token <- document.tokens) token.attr += new PorterTokenLemma(token, cc.factorie.app.strings.PorterStemmer(token.string))
    document
  }
  override def tokenAnnotationString(token:Token): String = { val l = token.attr[SimplifyDigitsTokenLemma]; l.value }
  def prereqAttrs: Iterable[Class[_]] = List(classOf[Token])
  def postAttrs: Iterable[Class[_]] = List(classOf[PorterTokenLemma])
}
object PorterLemmatizer extends PorterLemmatizer

class PorterTokenLemma(token:Token, s:String) extends TokenLemma(token, s)