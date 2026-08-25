package examples.externalReferences.aggressive.client

import kotlin.String
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

public class OAuth2(
  public val accessToken: () -> String,
) : Authenticator, Interceptor {
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
