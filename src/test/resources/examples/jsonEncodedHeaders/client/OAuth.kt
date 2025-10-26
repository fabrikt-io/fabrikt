package examples.jsonEncodedHeaders.client

import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class OAuth2(val accessToken: () -> String) : Authenticator, Interceptor {

    override fun authenticate(route: Route?, response: Response): Request =
        response.request.newBuilder()
            .header("Authorization", "Bearer ${accessToken().trim()}")
            .build()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("Authorization", "Bearer ${accessToken().trim()}")
            .build()
        return chain.proceed(request)
    }
}
