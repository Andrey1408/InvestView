package ivbackend.common

import ivbackend.tinkoff.service.*
import ivbackend.db.dao.*
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import ru.tinkoff.piapi.core.InvestApi
object DependencyConfig {
    fun mainModule(): DI.Module {
        return DI.Module("MainModule", false) {
            bind<ShareDAO>() with singleton { ShareDAO() }
            bind<AccountDAO>() with singleton { AccountDAO() }
            bind<AccountShareDAO>() with singleton { AccountShareDAO() }

            bind<InvestApiService>() with singleton { InvestApiService(instance(), instance()) }

            bind<ShareService>() with singleton { ShareService(instance(), instance()) }
            bind<AccountService>() with singleton { AccountService(instance()) }
            bind<AccountShareService>() with singleton { AccountShareService(instance(), instance(), instance()) }
        }
    }

    fun investApiClient(token: String): DI.Module {
        return DI.Module("InvestApiClientModule", false) {
            bind<InvestApi>() with singleton { InvestApi.createSandbox(token)
            }
        }
    }
}