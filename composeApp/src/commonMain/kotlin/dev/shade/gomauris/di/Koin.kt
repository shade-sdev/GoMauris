package dev.shade.gomauris.di

import dev.shade.gomauris.core.DataRepository
import dev.shade.gomauris.core.service.MapService
import dev.shade.gomauris.viewmodel.HomeTabViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val dataModule = module {

    single { DataRepository() }

}

val serviceModule = module {

    single { MapService() }

}

val viewModelModule = module {

    viewModel { HomeTabViewModel(get()) }

}