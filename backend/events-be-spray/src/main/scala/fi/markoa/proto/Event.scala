package fi.markoa.proto

import java.util.Date

case class Event(
    id: Option[String],
    title: String,
    category: String,
    description: Option[String],
    startTime: Date,
    duration: Int
)

