package io.eels.datastream;

case class CountAction(ds: DataStream) extends Action {
  def execute: Long = {
    var count = 0
    ds.coalesce.iterator.foreach(_ => count = count + 1)
    count
  }
}