package com.example.salesforce

import com.google.gson.Gson
import scala.collection.mutable.{Map,
      SynchronizedMap, HashMap}



case class DescribeField(name: String, `type`: String)

case class DescribeResponse(fields: Array[DescribeField])

case class Response(
    totalSize: Int,
    done: Boolean,
    nextRecordsUrl: String,
    records: Array[Any]
)

case class CountObject(count: Int, name: String)

case class RecordCountResponse(sObjects: List[CountObject])
