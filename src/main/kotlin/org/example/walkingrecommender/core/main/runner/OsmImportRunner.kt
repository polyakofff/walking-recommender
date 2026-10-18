package org.example.walkingrecommender.core.main.runner

import org.example.walkingrecommender.WalkingRecommenderApplication
import org.example.walkingrecommender.core.osm.OsmImporter
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder

object OsmImportRunner {

    @JvmStatic
    fun main(args: Array<String>) {
        val context = SpringApplicationBuilder(
            WalkingRecommenderApplication::class.java,
        )
            .web(WebApplicationType.NONE)
            .run(*args)

        try {
            val importer = context.getBean(OsmImporter::class.java)
            importer.importAll()
        } finally {
            context.close()
        }
    }
}