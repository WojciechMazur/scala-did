package fmgp.did

import zio._
import fmgp.crypto._

/** Indentity have is a DID and keys
  *   - has keys
  *   - can have encryption preferences
  *   - can have resolver preferences
  */
trait Indentity {
  def id: DID
  def keys: Seq[PrivateKey] // use KeyStore
  def keyStore = KeyStore(keys.toSet)
}
