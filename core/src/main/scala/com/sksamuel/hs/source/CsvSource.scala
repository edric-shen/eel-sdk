package com.sksamuel.hs.source

import java.nio.file.Path

import com.github.tototoshi.csv.CSVReader
import com.sksamuel.hs.Source
import com.sksamuel.hs.sink.{Field, Column, Row}

case class CsvSource(path: Path) extends Source {
  override def loader: Iterator[Row] = new Iterator[Row] {
    val reader = CSVReader.open(path.toFile)
    lazy val (iterator, headers) = {
      val iterator = reader.iterator
      val headers = iterator.next.map(Column.apply)
      (iterator, headers)
    }
    override def hasNext: Boolean = iterator.hasNext
    override def next(): Row = Row(headers, iterator.next.map(Field.apply))
  }
}
