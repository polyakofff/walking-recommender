package org.example.walkingrecommender.core.main.runner

import org.example.walkingrecommender.WalkingRecommenderApplication
import org.example.walkingrecommender.core.task.CalculateSegmentsTask
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder

object CalculateSegmentsRunner {

    @JvmStatic
    fun main(args: Array<String>) {
        val context = SpringApplicationBuilder(
            WalkingRecommenderApplication::class.java,
        )
            .web(WebApplicationType.NONE)
            .run(*args)

        try {
            val task = context.getBean(CalculateSegmentsTask::class.java)
            task.run()
        } finally {
            context.close()
        }
    }
}