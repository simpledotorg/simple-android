package org.simple.clinic.util.messagesender

interface MessageSender {
  fun send(phoneNumber: String, message: String)
}
