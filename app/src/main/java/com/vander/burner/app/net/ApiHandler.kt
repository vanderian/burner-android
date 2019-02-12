package com.vander.burner.app.net

import android.content.Context
import android.widget.Toast
import com.vander.burner.R
import com.vander.burner.app.net.model.ErrorModel
import com.vander.burner.app.ui.ShowDialogEvent
import com.vander.burner.app.ui.showConfirmDialog
import com.vander.scaffold.screen.Event
import com.vander.scaffold.screen.Screen
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import pm.gnosis.ethereum.RequestFailedException
import timber.log.Timber
import java.net.UnknownHostException
import java.util.concurrent.atomic.AtomicInteger

// general handling

private val codes = mapOf(
    400 to "error_bad_request",
    401 to "error_unauthorized",
    403 to "error_forbidden",
    404 to "error_not_found",
    405 to "error_not_allowed",
    406 to "error_not_acceptable",
    408 to "error_timeout",
    409 to "error_conflict",
    415 to "error_unsupported_media",
    422 to "error_unprocessable_entity",
    500 to "error_internal",
    501 to "error_not_implemented",
    502 to "error_bad_gateway",
    503 to "error_unavailable",
    504 to "error_gateway_timeout"
)

const val ERROR_NO_INTERNET = "error_no_internet"
const val ERROR_ETH = "error_eth"

fun parseError(throwable: Throwable): ErrorModel =
    when (throwable) {
      is UnknownHostException -> ErrorModel(ERROR_NO_INTERNET, throwable.message.orEmpty(), resId = R.string.error_net_no_host)
      is RequestFailedException -> ErrorModel(ERROR_ETH, throwable.message.orEmpty(), R.string.error_net_eth, arrayOf(throwable.message.orEmpty()))
      else -> ErrorModel("error_default", throwable.message.orEmpty(), resId = R.string.error_net_default)
    }

fun handleException(doOnError: (ErrorModel) -> Unit): (Throwable) -> Unit = {
  Timber.e(it)
  doOnError.invoke(parseError(it))
}

fun <T> Maybe<T>.handleError(doOnError: (ErrorModel) -> Unit): Maybe<T> = this.doOnError(handleException(doOnError))

fun <U : Screen.State, V : Screen.Intents> Screen<U, V>.onError(): Observable<*> =
    this.event(ErrorModel::class)
        .map { it.getString(this.requireContext()) }
        .doOnNext { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }

fun ErrorModel.getString(ctx: Context): String =
    (resId ?: 0)
        .let { if (it == 0) ctx.getString(R.string.error_net_default) else ctx.getString(it, *args) }
        .also { Timber.d("Error message: $it: $message") }

// api calls

fun Screen<*, *>.showRetryDialog(errorModel: ErrorModel): Maybe<Unit> =
    requireContext().showConfirmDialog(
        ShowDialogEvent(
            title = R.string.dialog_error_title,
            contentString = errorModel.getString(requireContext()),
            positiveButton = R.string.action_try_again
        )
    ).map { Unit }

fun Completable.safeApiCall(eventObserver: Observer<Event>, errorEffect: ((ErrorModel) -> Unit)? = null): Completable =
    this.toSingleDefault(Unit).safeApiCall(eventObserver, errorEffect).ignoreElement()

fun <T> Single<T>.safeApiCall(eventObserver: Observer<Event>, errorEffect: ((ErrorModel) -> Unit)? = null): Maybe<T> =
    this
        .loadingCall(eventObserver)
        .errorHandlingCall(eventObserver, errorEffect)

fun <T> Single<T>.safeApiCall(eventObserver: Observer<Event>, retryIntents: RetryIntents): Maybe<T> =
    this
        .loadingCall(eventObserver)
        .toMaybe()
        .retry { count, throwable -> throwable is UnknownHostException && count <= NetModule.RETRY_COUNT }
        .retryWhen { errors ->
          errors
              .map { parseError(it) }
              .flatMapMaybe { retryIntents.retrySignal(it).subscribeOn(AndroidSchedulers.mainThread()) }
        }

fun <T> Single<T>.loadingCall(eventObserver: Observer<Event>): Single<T> =
    compose {
      it.doOnSubscribe { eventObserver.onNext(Started) }
          .doOnEvent { _, _ -> eventObserver.onNext(Done) }
    }

fun <T> Single<T>.errorHandlingCall(eventObserver: Observer<Event>, errorEffect: ((ErrorModel) -> Unit)? = null): Maybe<T> =
    this
        .toMaybe()
        .handleError {
          errorEffect?.invoke(it)
          eventObserver.onNext(it)
        }
        .onErrorResumeNext(Maybe.empty())

object Started : Event
object Done : Event

interface RetryIntents : Screen.Intents {
  fun retrySignal(errorModel: ErrorModel): Maybe<Unit>
}

class ApiHandler(val loading: (Boolean) -> Unit) {
  private val apiCount: AtomicInteger = AtomicInteger(0)
  private val showLoading = Runnable { loading(true) }
  private var errorComponent: Maybe<Unit>? = null

  fun eventHandler(screen: Screen<*, *>, onError: ((ErrorModel) -> Unit)? = null) = listOf(
      screen.event(Started::class).doOnNext { if (apiCount.incrementAndGet() == 1) screen.view?.postDelayed(showLoading, 0) }
          .doOnDispose { screen.view?.removeCallbacks(showLoading) },
      screen.event(Done::class).doOnNext {
        if (apiCount.decrementAndGet() == 0) {
          screen.view?.removeCallbacks(showLoading)
          loading(false)
        }
      },
      screen.onError()
  ) + (onError?.let { listOf(screen.event(ErrorModel::class).doOnNext(it)) } ?: listOf())

  fun retrySignal(errorModel: ErrorModel, screen: Screen<*, *>): Maybe<Unit> {
    if (errorComponent == null) {
      errorComponent = screen.showRetryDialog(errorModel).cache().doOnEvent { _, _ -> errorComponent = null }
    }
    return errorComponent!!
  }
}

fun Screen<*, *>.apiHandler(loading: (Boolean) -> Unit) = apiHandler(loading, null)
fun Screen<*, *>.apiHandler(loading: (Boolean) -> Unit, onError: ((ErrorModel) -> Unit)?) = ApiHandler(loading).eventHandler(this, onError)

