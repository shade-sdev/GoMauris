package dev.shade.gomauris.di

import dev.shade.gomauris.core.DataRepository
import dev.shade.gomauris.viewmodel.HomeTabViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val dataModule = module {

    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }
    }

    single { DataRepository() }

}

val viewModelModule = module {

    viewModel { HomeTabViewModel() }

}