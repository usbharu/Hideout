package utils

import com.ninja_squad.dbsetup.operation.Insert
import java.sql.Timestamp
import java.time.Instant

fun Insert.Builder.postsValues(index: Long = 1, actor: Long = 1) {
    values(
        index,
        actor,
        1832779642545639424,
        null,
        "<p>test</p>",
        "test",
        Timestamp.from(Instant.parse("2020-01-01T00:00:00Z").plusSeconds(index.toLong())),
        "PUBLIC",
        "http://localhost:8081/users/a/posts/1832779994749734912",
        2,
        2,
        false,
        "http://localhost:8081/users/a/posts/1832779994749734912$index",
        false,
        false,
        null,
        false
    )
}