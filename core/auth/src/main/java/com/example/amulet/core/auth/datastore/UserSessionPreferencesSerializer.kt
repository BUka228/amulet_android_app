package com.example.amulet.core.auth.datastore

import androidx.datastore.core.Serializer
import com.example.amulet.core.auth.session.proto.UserSessionPreferences
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object UserSessionPreferencesSerializer : Serializer<UserSessionPreferences> {
    override val defaultValue: UserSessionPreferences = UserSessionPreferences.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): UserSessionPreferences =
        try {
            UserSessionPreferences.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            defaultValue
        }

    override suspend fun writeTo(t: UserSessionPreferences, output: OutputStream) {
        t.writeTo(output)
    }
}
