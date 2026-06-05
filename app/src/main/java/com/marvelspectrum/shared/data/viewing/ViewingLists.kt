package com.marvelspectrum.shared.data.viewing

object ViewingLists {
    val allItems: List<ViewingItem> = listOf(
        ViewingItem(
            id = "mcu-iron-man",
            title = "Iron Man",
            universe = "MCU",
            franchise = "Iron Man Collection",
            studio = "Marvel Studios",
            type = ViewingType.MOVIE,
            phase = "Phase One",
            saga = "Infinity Saga",
            category = "MCU Core",
            releaseDate = "2008-05-02",
            runtime = "126 min",
            genres = listOf("Action", "Adventure", "Sci-Fi"),
            imdbId = "tt0371746",
            tmdbId = 1726,
            imdbRating = "7.9",
            director = "Jon Favreau",
            writer = "Mark Fergus, Hawk Ostby, Art Marcum, Matt Holloway",
            actors = listOf("Robert Downey Jr.", "Gwyneth Paltrow", "Jeff Bridges"),
            language = "English",
            country = "United States",
            overview = "Tony Stark builds the first Iron Man armor and begins a cinematic universe.",
            releaseOrder = 1,
            chronologicalOrder = 5,
            phaseOrder = 1,
            youtubeVideoId = "8ugaeA-nMTc"
        ),
        ViewingItem(
            id = "mcu-captain-america-first-avenger",
            title = "Captain America: The First Avenger",
            universe = "MCU",
            franchise = "Captain America Collection",
            studio = "Marvel Studios",
            type = ViewingType.MOVIE,
            phase = "Phase One",
            saga = "Infinity Saga",
            category = "MCU Core",
            releaseDate = "2011-07-22",
            runtime = "124 min",
            genres = listOf("Action", "Adventure", "Sci-Fi"),
            imdbId = "tt0458339",
            tmdbId = 1771,
            imdbRating = "6.9",
            director = "Joe Johnston",
            writer = "Christopher Markus, Stephen McFeely",
            actors = listOf("Chris Evans", "Hayley Atwell", "Sebastian Stan"),
            language = "English",
            country = "United States",
            overview = "Steve Rogers becomes Captain America during World War II.",
            releaseOrder = 5,
            chronologicalOrder = 2,
            phaseOrder = 5,
            youtubeVideoId = "JerVrbLldXw"
        ),
        ViewingItem(
            id = "dc-man-of-steel",
            title = "Man of Steel",
            universe = "DCEU",
            franchise = "Superman Collection",
            studio = "Warner Bros. Pictures",
            type = ViewingType.MOVIE,
            phase = "DCEU",
            saga = "DCEU",
            category = "DCEU",
            releaseDate = "2013-06-14",
            runtime = "143 min",
            genres = listOf("Action", "Adventure", "Sci-Fi"),
            imdbId = "tt0770828",
            tmdbId = 49521,
            imdbRating = "7.1",
            director = "Zack Snyder",
            writer = "David S. Goyer, Christopher Nolan",
            actors = listOf("Henry Cavill", "Amy Adams", "Michael Shannon"),
            language = "English",
            country = "United States",
            overview = "Clark Kent becomes Superman as General Zod threatens Earth.",
            releaseOrder = 1,
            chronologicalOrder = 1,
            phaseOrder = 1,
            youtubeVideoId = "T6DJcgm3wNY"
        )
    )

    val allLists: List<ViewingList> = listOf(
        ViewingList(
            id = "fallback-release-order",
            title = "Cinemaverse Release Order",
            description = "Fallback Marvel/DC seed list used if the JSON catalog cannot be loaded.",
            category = "Release Order",
            itemIds = allItems.map { it.id },
            items = allItems.sortedBy { it.releaseDate }
        )
    )

    val featuredItem: ViewingItem = allItems.first()
    val featuredList: ViewingList = allLists.first()

    fun findList(id: String): ViewingList? = allLists.firstOrNull { it.id == id }
    fun findItem(id: String): ViewingItem? = allItems.firstOrNull { it.id == id }
}
