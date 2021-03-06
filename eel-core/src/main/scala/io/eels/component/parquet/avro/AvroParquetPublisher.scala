package io.eels.component.parquet.avro

import java.util.concurrent.atomic.AtomicBoolean

import com.sksamuel.exts.Logging
import com.sksamuel.exts.io.Using
import io.eels.component.avro.AvroDeserializer
import io.eels.component.parquet.util.ParquetIterator
import io.eels.datastream.{DataStream, Publisher, Subscriber, Subscription}
import io.eels.{Predicate, Row}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path

class AvroParquetPublisher(path: Path,
                           predicate: Option[Predicate])(implicit conf: Configuration)
  extends Publisher[Seq[Row]] with Logging with Using {

  override def subscribe(subscriber: Subscriber[Seq[Row]]): Unit = {
    try {
      val deser = new AvroDeserializer()
      val running = new AtomicBoolean(true)
      subscriber.subscribed(Subscription.fromRunning(running))
      using(AvroParquetReaderFn(path, predicate, None)) { reader =>
        ParquetIterator(reader)
          .map(deser.toRow)
          .grouped(DataStream.DefaultBatchSize)
          .takeWhile(_ => running.get)
          .foreach(subscriber.next)
      }
      subscriber.completed()
    } catch {
      case t: Throwable => subscriber.error(t)
    }
  }
}