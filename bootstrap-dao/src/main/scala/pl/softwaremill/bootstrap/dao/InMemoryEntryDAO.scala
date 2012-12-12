package pl.softwaremill.bootstrap.dao

import pl.softwaremill.bootstrap.domain.Entry

class InMemoryEntryDAO extends EntryDAO {

  var entries = List[Entry]()

  def loadAll: List[Entry] = {
    entries
  }

  def countItems(): Long = {
    entries.size
  }

  def add(entry: Entry) {
    entries ::= entry
  }

  def remove(entryId: String) {
    load(entryId) match {
      case Some(entry) => entries.diff(List(entry))
      case _ =>
    }
  }

  def load(entryId: String): Option[Entry] = {
    entries.find(entry => entry._id == entryId)
  }

  def update(entryId: String, message: String) {
    load(entryId) match {
      case Some(e) => entries = entries.updated(entries.indexOf(e), e.copy(text = message))
      case _ =>
    }
  }

}
