package com.contactpick

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.*

class ContactPickModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String {
    return MODULE_NAME
  }

  override fun getConstants(): MutableMap<String, Any> =
    hashMapOf("ERR_CODE_CANCELED" to ERR_CODE_CANCELED, "ERR_CODE_PERMISSION_NOT_GRANTED" to ERR_CODE_PERMISSION_NOT_GRANTED)

  @ReactMethod
  fun pickContact(options: ReadableMap, _promise: Promise) {
    val permission = android.Manifest.permission.READ_CONTACTS
    val hasPermission = ContextCompat.checkSelfPermission(reactApplicationContext, permission) == PackageManager.PERMISSION_GRANTED

    if(!hasPermission) {
      _promise.reject(ERR_CODE_PERMISSION_NOT_GRANTED);
      return
    }

    val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
    val activity = currentActivity

    if (activity == null) {
      _promise.reject("Activity not available")
      return
    }

    promise = _promise
    contactOptions = options
    activity?.startActivityForResult(intent, PICK_CONTACT_REQUEST)
  }

  private val activityEventListener = object : BaseActivityEventListener() {
    override fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?) {
      try {
        if (requestCode == PICK_CONTACT_REQUEST) {
          if (resultCode == Activity.RESULT_OK) {
            val contactUri = data?.data ?: return

            val cursor = activity.contentResolver.query(contactUri, null, null, null, null)

            cursor?.moveToFirst()
            val contact = cursor?.let { getContactFromCursor(it) }
            cursor?.close()

            promise?.resolve(contact)
          } else {
            promise?.reject(ERR_CODE_CANCELED, "Contact picker cancelled")
          }

          promise = null
        }

      } catch ( e: Exception) {
        promise?.reject("Error", e.message)
      }
    }
  }

  private fun getContactFromCursor(cursor: Cursor): WritableMap {
    val contact = Arguments.createMap()

    val displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
    contact.putString("fullName", displayName)

    val phoneNumbers = getPhoneNumbers(cursor)
    if(phoneNumbers.size() > 0) {
      contact.putArray("phoneNumbers", phoneNumbers)
    } else {
      contact.putNull("phoneNumbers")
    }

    val emails = getEmails(cursor)
    if(emails.size() > 0) {
      contact.putArray("emails", emails)
    } else {
      contact.putNull("emails")
    }

    return contact
  }

  private fun getPhoneNumbers(cursor: Cursor): WritableArray {
    val phoneNumbers = Arguments.createArray()

    val hasPhoneNumber = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
    if (hasPhoneNumber > 0) {
      val contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
      val phoneCursor = context?.contentResolver?.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        null,
        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
        null,
        null
      )

      phoneCursor?.let {
        while (it.moveToNext()) {
          val number = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
          val type = getPhoneType(it.getInt(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)))
          val phoneNumber = Arguments.createMap().apply {
            putString("type", type)
          }
          var isDigitalPhoneNumber = contactOptions?.hasKey("isPhoneNumberDigital") == true && contactOptions?.getBoolean("isPhoneNumberDigital")!!
          if(isDigitalPhoneNumber) {
            phoneNumber.putString("number", number.replace("\\D+".toRegex(), ""))
          } else {
            phoneNumber.putString("number", number)
          }
          phoneNumbers.pushMap(phoneNumber)
        }
        it.close()
      }
    }

    return phoneNumbers
  }

  private fun getEmails(cursor: Cursor): WritableArray {
    val emails = Arguments.createArray()

    val contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID))
    val emailsCursor = context?.contentResolver?.query(
      ContactsContract.CommonDataKinds.Email.CONTENT_URI,
      null,
      ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
      arrayOf(contactId.toString()),
      null
    )
    emailsCursor?.let {
      while (it.moveToNext()) {
        val email = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS))
        val type = getEmailType(it.getInt(it.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE)))
        val emailObj = Arguments.createMap().apply {
          putString("email", email)
          putString("type", type)
        }
        emails.pushMap(emailObj)
      }
      it.close()
    }

    return emails
  }

  private fun getPhoneType(phoneType: Int): String {
    return when (phoneType) {
      ContactsContract.CommonDataKinds.Phone.TYPE_HOME -> "home"
      ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> "mobile"
      ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> "work"
      ContactsContract.CommonDataKinds.Phone.TYPE_OTHER -> "other"
      else -> "custom"
    }
  }

  private fun getEmailType(emailType: Int): String {
    return when (emailType) {
      ContactsContract.CommonDataKinds.Email.TYPE_HOME -> "home"
      ContactsContract.CommonDataKinds.Email.TYPE_WORK -> "work"
      ContactsContract.CommonDataKinds.Email.TYPE_OTHER -> "other"
      else -> "custom"
    }
  }

  init {
    reactContext.addActivityEventListener(activityEventListener)
    context = reactContext
  }

  companion object {
    const val MODULE_NAME = "ContactPick"
    private const val PICK_CONTACT_REQUEST = 100
    private var promise: Promise? = null
    private var context: ReactApplicationContext? = null
    private var contactOptions: ReadableMap? = null;

    const val ERR_CODE_CANCELED = "ERR_CANCELLED"
    const val ERR_CODE_PERMISSION_NOT_GRANTED = "ERR_CODE_PERMISSION_NOT_GRANTED"
  }
}
