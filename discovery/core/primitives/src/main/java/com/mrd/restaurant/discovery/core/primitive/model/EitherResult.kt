package com.mrd.restaurant.discovery.core.primitive.model

sealed interface EitherResult<TData, TError> {
    data class Success<TData, TError>(val data: TData) : EitherResult<TData, TError>
    data class Error<TData, TError>(val error: TError) : EitherResult<TData, TError>
}