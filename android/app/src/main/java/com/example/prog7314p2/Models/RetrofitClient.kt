import java.util.concurrent.TimeUnit
import com.example.prog7314p2.Models.ApiService
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://unify-api-743468044726.europe-west1.run.app/"

    private val authInterceptor = Interceptor { chain ->
        val user = FirebaseAuth.getInstance().currentUser
        val request = if (user != null) {
            try {
                val token = Tasks.await(user.getIdToken(false)).token
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } catch (e: Exception) {
                chain.request()
            }
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)
    }
}