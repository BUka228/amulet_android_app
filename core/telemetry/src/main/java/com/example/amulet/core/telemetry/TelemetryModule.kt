package com.example.amulet.core.telemetry

import com.example.amulet.core.telemetry.logging.NetworkTelemetryReporter
import com.example.amulet.core.telemetry.logging.TelemetryReporter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TelemetryModule {

    @Binds
    abstract fun bindTelemetryReporter(impl: NetworkTelemetryReporter): TelemetryReporter
}
