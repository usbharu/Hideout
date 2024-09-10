package dev.usbharu.hideout.core.infrastructure.exposed

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.vendors.currentDialect
import java.net.URI

class UriColumnType(val colLength: Int) : ColumnType<URI>() {
    override fun sqlType(): String = currentDialect.dataTypeProvider.varcharType(colLength)

    override fun valueFromDB(value: Any): URI? = when (value) {
        is URI -> value
        is String -> URI(value)
        is CharSequence -> URI(value.toString())
        else -> error("Unexpected value of type String: $value of ${value::class.qualifiedName}")
    }

    override fun notNullValueToDB(value: URI): Any = value.toString()
}

fun Table.uri(name: String, colLength: Int): Column<URI> = registerColumn(name, UriColumnType(colLength))