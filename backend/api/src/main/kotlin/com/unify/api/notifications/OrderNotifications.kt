
package com.unify.api.notifications

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

data class OrderStatusChangedEvent(
    val orderId: Int,
    val userId: String,
    val status: String
)

@Component
class OrderNotifications(
    private val tokenRepository: FcmTokenRepository,
    private val firebaseMessaging: FirebaseMessaging
) {

    private val log =
        LoggerFactory.getLogger(OrderNotifications::class.java)

    @TransactionalEventListener(
        phase = TransactionPhase.AFTER_COMMIT
    )
    fun sendOrderNotification(event: OrderStatusChangedEvent) {

        val devices = tokenRepository.findByUserId(event.userId)

        if (devices.isEmpty()) {
            log.info("No FCM devices registered for order {}", event.orderId)
            return
        }

        for (device in devices) {

            val message = Message.builder()
                .setToken(device.token)
                .setNotification(
                    Notification.builder()
                        .setTitle("Unify Retail - Order Update")
                        .setBody(
                            "Your order #${event.orderId} is now ${event.status}"
                        )
                        .build()
                )
                .putData("type", "ORDER_STATUS_UPDATED")
                .putData("orderId", event.orderId.toString())
                .putData("status", event.status)
                .build()

            try {
                firebaseMessaging.send(message)

                log.info(
                    "Notification sent for order {}",
                    event.orderId
                )

            } catch (e: FirebaseMessagingException) {
                log.error(
                    "Failed to send notification for order {}",
                    event.orderId,
                    e
                )
            }
        }
    }
}
