package com.example.myapplication.message

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri
import com.example.myapplication.R
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil

enum class MessageSender(
    val id: String,
    val displayName: String
) {
    WhatsApp("WHATSAPP", "WhatsApp") {
        override fun isAvailable(context: Context): Boolean {
            return whatsAppPackage(context) != null
        }

        override fun sendMessage(context: Context, rawPhone: String, message: String) {
            val util = PhoneNumberUtil.createInstance(context.applicationContext)
            val phone = whatsappPhoneNumber(rawPhone, context.resources.configuration.locales[0].country, util)
            if (phone == null) {
                Toast.makeText(context, context.getString(R.string.err_invalid_phone_country_code), Toast.LENGTH_LONG).show()
                return
            }
            val packageName = whatsAppPackage(context)
            if (packageName == null) {
                Toast.makeText(context, context.getString(R.string.err_whatsapp_not_installed), Toast.LENGTH_SHORT).show()
                return
            }
            val intent = Intent(
                Intent.ACTION_VIEW,
                "https://wa.me/$phone?text=${Uri.encode(message)}".toUri()
            ).apply {
                setPackage(packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(intent)
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(context, context.getString(R.string.err_whatsapp_not_installed), Toast.LENGTH_SHORT).show()
            }
        }
    },

    Sms("SMS", "SMS") {
        override fun isAvailable(context: Context): Boolean = true

        override fun sendMessage(context: Context, rawPhone: String, message: String) {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = "smsto:$rawPhone".toUri()
                putExtra("sms_body", message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(intent)
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(context, context.getString(R.string.err_no_sms_app), Toast.LENGTH_SHORT).show()
            }
        }
    };

    abstract fun isAvailable(context: Context): Boolean
    abstract fun sendMessage(context: Context, rawPhone: String, message: String)

    companion object {
        fun fromId(id: String?): MessageSender = entries.firstOrNull { it.id == id } ?: WhatsApp
    }
}

private fun whatsAppPackage(context: Context): String? =
    listOf("com.whatsapp", "com.whatsapp.w4b").firstOrNull { packageName ->
        Intent(Intent.ACTION_VIEW, "https://wa.me/".toUri())
            .setPackage(packageName).resolveActivity(context.packageManager) != null
    }

/** wa.me expects international digits, even when the stored number is local. */
internal fun whatsappPhoneNumber(rawPhone: String, region: String, util: PhoneNumberUtil): String? {
    fun parse(value: String) = try {
        util.parse(value, region).takeIf(util::isValidNumber)
    } catch (_: NumberParseException) {
        null
    }
    val number = parse(rawPhone) ?: if (!rawPhone.trim().startsWith('+')) {
        parse("+${PhoneNumberUtil.normalizeDigitsOnly(rawPhone)}")
    } else null
    return number?.let { util.format(it, PhoneNumberUtil.PhoneNumberFormat.E164).removePrefix("+") }
}
