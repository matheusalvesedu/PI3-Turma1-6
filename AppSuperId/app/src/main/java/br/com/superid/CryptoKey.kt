package br.com.superid

object CryptoKey {
    val chaveSimetrica = "rrmpeiurtafcrirrcgtocacojpiIaoea"

    fun getKey(): ByteArray{
        return chaveSimetrica.toByteArray()
    }
}