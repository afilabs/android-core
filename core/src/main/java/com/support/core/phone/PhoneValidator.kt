package com.support.core.phone

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import com.support.core.Inject

@Inject(true)
class PhoneValidator {
    private val mPhoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()

    fun isValid(code: IPhoneCode, body: String): Boolean {
        val realNumber = getNumber(body)
        val len = realNumber.length
        if (len < 8 || len > 16) return false

        val phoneNumber = Phonenumber.PhoneNumber().apply {
            try {
                nationalNumber = realNumber.toLong()
            } catch (e: Throwable) {
                return false
            }
            countryCode = code.dialCode.removePrefix("+").toInt()
        }
        return mPhoneUtil.isValidNumber(phoneNumber)
    }

    companion object {
        fun getNumber(phoneNumber: String): String {
            return phoneNumber.replace(Regex("[^0-9+]"), "")
        }

        fun getNationalPhone(rawPhoneNumber: String): Long {
            val phoneNumber = getNumber(rawPhoneNumber)
            val utils = PhoneNumberUtil.getInstance()
            try {
                for (region in utils.supportedRegions) {
                    var isValid = utils.isPossibleNumber(phoneNumber, region)
                    if (isValid) {
                        val number: Phonenumber.PhoneNumber = utils.parse(phoneNumber, region)

                        isValid = utils.isValidNumberForRegion(number, region)
                        if (isValid) return number.nationalNumber
                    }
                }
            } catch (e: NumberParseException) {
            }
            return 0
        }
    }
}