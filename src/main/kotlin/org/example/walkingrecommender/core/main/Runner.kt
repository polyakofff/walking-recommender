package org.example.walkingrecommender.core.main

import org.example.walkingrecommender.WalkingRecommenderApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder

object Runner {

    @JvmStatic
    fun main(args: Array<String>) {
        val context = SpringApplicationBuilder(
            WalkingRecommenderApplication::class.java,
        )
            .web(WebApplicationType.NONE)
            .run(*args)

        try {
            val importer = context.getBean(OsmImporter::class.java)
//            importer.importGraph()
//            importer.importAll()

//            context.getBean(CalculateSegmentsTask::class.java).run()

            context.getBean(RouteGeneratorTest::class.java).run()
        } finally {
            context.close()
        }
    }
}