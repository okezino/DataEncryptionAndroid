package com.example.androidfileencryption

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

@RequiresApi(Build.VERSION_CODES.M)
class KeyManager {
    private val keystore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }
    private val encryptCipher = Cipher.getInstance(TRANSFORMATION).apply {
        init(Cipher.ENCRYPT_MODE, getKey())
    }

    fun decryptCipherForIv(iv : ByteArray) : Cipher{
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, getKey(), IvParameterSpec(iv) )
        }
    }

    private fun getKey() : SecretKey{
        val existKeys = keystore.getEntry("secret", null) as? KeyStore.SecretKeyEntry
        return existKeys?.secretKey ?: createKey()
    }


    private  fun createKey():SecretKey{
        return KeyGenerator.getInstance(ALGORITHM).apply {
            init(
                KeyGenParameterSpec.Builder("secret",
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(BLOCK_MODE)
                    .setEncryptionPaddings(PADDING)
                    .setUserAuthenticationRequired(false)
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )
        }.generateKey()
    }

    companion object{
        //These are called the Cipher
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val TRANSFORMATION= "$ALGORITHM/$BLOCK_MODE/$PADDING"

    }


    fun encrypt(byteArray: ByteArray, outputStream : OutputStream) : ByteArray{
        val encryptedBytes = encryptCipher.doFinal(byteArray)
        outputStream.use {
            it.write(encryptCipher.iv.size)
            it.write(encryptCipher.iv)
            it.write(encryptedBytes.size)
            it.write(encryptedBytes)
        }
      return encryptedBytes
    }

    fun decrypt(inputStream : InputStream) : ByteArray{
        return inputStream.use {

            val ivSize = it.read()
            val iv = ByteArray(ivSize)
            it.read(iv)

            val encryptedByteSize = it.read()
            val encryptedByte = ByteArray(encryptedByteSize)
            it.read(encryptedByte)

            decryptCipherForIv(iv).doFinal(encryptedByte)

        }
    }
}