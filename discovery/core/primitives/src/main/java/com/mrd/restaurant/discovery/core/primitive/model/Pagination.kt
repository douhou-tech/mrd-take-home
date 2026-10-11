package com.mrd.restaurant.discovery.core.primitive.model

data class Pagination(
    val page: Int,
    val pageSize: Int
)

data class PaginationResult<TData>(
    val items: List<TData> = emptyList(),
    val pageSize: Int = 0,
    val currentPage: Int = 0,
    val totalPages: Int = 0
)
