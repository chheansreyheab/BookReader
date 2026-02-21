package com.example.bookreader.data


fun Book.toEntity() = BookEntity(
    uriString = uriString,
    title = title,
    author = author,
    coverRes = coverRes,
    coverBytes = coverBytes,
    currentRead = currentRead,
    totalRead = totalRead,
    description = description
)

fun BookEntity.toDomain() = Book(
    uriString = uriString,
    title = title,
    author = author,
    coverRes = coverRes,
    coverBytes = coverBytes,
    currentRead = currentRead,
    totalRead = totalRead,
    description = description
)