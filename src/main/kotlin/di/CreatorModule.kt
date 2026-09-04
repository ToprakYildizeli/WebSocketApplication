package di

import customer.ScreenCatalog
import org.koin.dsl.module

fun creatorModule() = module {

    single<ScreenCatalog> {
        ScreenCatalog()
    }
}
