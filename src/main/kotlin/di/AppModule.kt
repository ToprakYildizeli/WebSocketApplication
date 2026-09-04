package di

import customer.CustomerConfig
import customer.CustomerFeatures
import customer.FeatureValidator
import org.koin.dsl.module

fun appModule(
    customerConfig: CustomerConfig,
    customerFeatures: CustomerFeatures
) = module {

    single<CustomerConfig> {
        customerConfig
    }

    single<CustomerFeatures> {
        customerFeatures
    }

    single<FeatureValidator> {
        FeatureValidator()
    }
}