package chromahub.rhythm.app.shared.data.viewing

object ViewingLists {
    private val releaseItems = listOf(
        item("iron-man", "Iron Man", "2008", "tt0371746", 1726, "Phase One", "Infinity Saga", 1, 3, "126 min", listOf("Action", "Adventure", "Sci-Fi"), "Tony Stark builds the first Iron Man armor and begins a new cinematic universe."),
        item("incredible-hulk", "The Incredible Hulk", "2008", "tt0800080", 1724, "Phase One", "Infinity Saga", 2, 5, "112 min", listOf("Action", "Adventure", "Sci-Fi"), "Bruce Banner searches for a cure while being hunted by forces that want his power."),
        item("iron-man-2", "Iron Man 2", "2010", "tt1228705", 10138, "Phase One", "Infinity Saga", 3, 4, "124 min", listOf("Action", "Adventure", "Sci-Fi"), "Tony Stark faces legacy, rivalry, and the consequences of revealing his identity."),
        item("thor", "Thor", "2011", "tt0800369", 10195, "Phase One", "Infinity Saga", 4, 6, "115 min", listOf("Action", "Fantasy", "Adventure"), "A banished Asgardian prince learns humility on Earth."),
        item("captain-america-first-avenger", "Captain America: The First Avenger", "2011", "tt0458339", 1771, "Phase One", "Infinity Saga", 5, 1, "124 min", listOf("Action", "Adventure", "Sci-Fi"), "Steve Rogers becomes Captain America during World War II."),
        item("the-avengers", "The Avengers", "2012", "tt0848228", 24428, "Phase One", "Infinity Saga", 6, 7, "143 min", listOf("Action", "Adventure", "Sci-Fi"), "Earth's mightiest heroes assemble against Loki and the Chitauri."),
        item("iron-man-3", "Iron Man 3", "2013", "tt1300854", 68721, "Phase Two", "Infinity Saga", 7, 8, "130 min", listOf("Action", "Adventure", "Sci-Fi"), "Tony Stark confronts trauma and a mysterious terrorist threat."),
        item("thor-dark-world", "Thor: The Dark World", "2013", "tt1981115", 76338, "Phase Two", "Infinity Saga", 8, 9, "112 min", listOf("Action", "Adventure", "Fantasy"), "Thor battles the Dark Elves as the Nine Realms align."),
        item("captain-america-winter-soldier", "Captain America: The Winter Soldier", "2014", "tt1843866", 100402, "Phase Two", "Infinity Saga", 9, 10, "136 min", listOf("Action", "Adventure", "Thriller"), "Steve Rogers uncovers a conspiracy inside S.H.I.E.L.D."),
        item("guardians-galaxy", "Guardians of the Galaxy", "2014", "tt2015381", 118340, "Phase Two", "Infinity Saga", 10, 11, "121 min", listOf("Action", "Adventure", "Comedy"), "A band of misfits becomes an unlikely cosmic family."),
        item("avengers-age-ultron", "Avengers: Age of Ultron", "2015", "tt2395427", 99861, "Phase Two", "Infinity Saga", 11, 13, "141 min", listOf("Action", "Adventure", "Sci-Fi"), "The Avengers face Ultron after a peacekeeping program goes wrong."),
        item("ant-man", "Ant-Man", "2015", "tt0478970", 102899, "Phase Two", "Infinity Saga", 12, 14, "117 min", listOf("Action", "Comedy", "Sci-Fi"), "Scott Lang learns to use a shrinking suit for a heist with heroic stakes."),
        item("captain-america-civil-war", "Captain America: Civil War", "2016", "tt3498820", 271110, "Phase Three", "Infinity Saga", 13, 15, "147 min", listOf("Action", "Adventure", "Sci-Fi"), "The Avengers fracture over accountability and loyalty."),
        item("doctor-strange", "Doctor Strange", "2016", "tt1211837", 284052, "Phase Three", "Infinity Saga", 14, 16, "115 min", listOf("Action", "Adventure", "Fantasy"), "A surgeon discovers mystic arts and a larger universe."),
        item("guardians-galaxy-vol-2", "Guardians of the Galaxy Vol. 2", "2017", "tt3896198", 283995, "Phase Three", "Infinity Saga", 15, 12, "136 min", listOf("Action", "Adventure", "Comedy"), "The Guardians learn more about Peter Quill's origins."),
        item("spider-man-homecoming", "Spider-Man: Homecoming", "2017", "tt2250912", 315635, "Phase Three", "Infinity Saga", 16, 17, "133 min", listOf("Action", "Adventure", "Comedy"), "Peter Parker balances school life with being Spider-Man."),
        item("thor-ragnarok", "Thor: Ragnarok", "2017", "tt3501632", 284053, "Phase Three", "Infinity Saga", 17, 18, "130 min", listOf("Action", "Adventure", "Comedy"), "Thor races to save Asgard from Hela."),
        item("black-panther", "Black Panther", "2018", "tt1825683", 284054, "Phase Three", "Infinity Saga", 18, 19, "134 min", listOf("Action", "Adventure", "Sci-Fi"), "T'Challa returns to Wakanda and faces a challenge to the throne."),
        item("avengers-infinity-war", "Avengers: Infinity War", "2018", "tt4154756", 299536, "Phase Three", "Infinity Saga", 19, 20, "149 min", listOf("Action", "Adventure", "Sci-Fi"), "The Avengers and Guardians try to stop Thanos from completing the Infinity Gauntlet."),
        item("ant-man-wasp", "Ant-Man and the Wasp", "2018", "tt5095030", 363088, "Phase Three", "Infinity Saga", 20, 21, "118 min", listOf("Action", "Adventure", "Comedy"), "Scott Lang and Hope van Dyne explore the Quantum Realm."),
        item("captain-marvel", "Captain Marvel", "2019", "tt4154664", 299537, "Phase Three", "Infinity Saga", 21, 2, "123 min", listOf("Action", "Adventure", "Sci-Fi"), "Carol Danvers discovers her powers and past in the 1990s."),
        item("avengers-endgame", "Avengers: Endgame", "2019", "tt4154796", 299534, "Phase Three", "Infinity Saga", 22, 22, "181 min", listOf("Action", "Adventure", "Drama"), "The remaining heroes attempt to undo Thanos' snap."),
        item("spider-man-far-from-home", "Spider-Man: Far From Home", "2019", "tt6320628", 429617, "Phase Three", "Infinity Saga", 23, 23, "129 min", listOf("Action", "Adventure", "Comedy"), "Peter Parker faces grief, illusions, and responsibility abroad."),
        item("black-widow", "Black Widow", "2021", "tt3480822", 497698, "Phase Four", "Multiverse Saga", 24, 24, "134 min", listOf("Action", "Adventure", "Sci-Fi"), "Natasha Romanoff confronts the history she left behind."),
        item("shang-chi", "Shang-Chi and the Legend of the Ten Rings", "2021", "tt9376612", 566525, "Phase Four", "Multiverse Saga", 25, 25, "132 min", listOf("Action", "Adventure", "Fantasy"), "Shang-Chi is pulled into the legacy of the Ten Rings."),
        item("eternals", "Eternals", "2021", "tt9032400", 524434, "Phase Four", "Multiverse Saga", 26, 26, "156 min", listOf("Action", "Adventure", "Fantasy"), "Ancient protectors reveal their role in Earth's history."),
        item("spider-man-no-way-home", "Spider-Man: No Way Home", "2021", "tt10872600", 634649, "Phase Four", "Multiverse Saga", 27, 27, "148 min", listOf("Action", "Adventure", "Fantasy"), "A spell gone wrong brings multiverse consequences to Peter Parker."),
        item("doctor-strange-mom", "Doctor Strange in the Multiverse of Madness", "2022", "tt9419884", 453395, "Phase Four", "Multiverse Saga", 28, 28, "126 min", listOf("Action", "Adventure", "Fantasy"), "Doctor Strange navigates dangerous alternate realities."),
        item("thor-love-thunder", "Thor: Love and Thunder", "2022", "tt10648342", 616037, "Phase Four", "Multiverse Saga", 29, 29, "118 min", listOf("Action", "Adventure", "Comedy"), "Thor faces Gorr the God Butcher and searches for purpose."),
        item("black-panther-wakanda-forever", "Black Panther: Wakanda Forever", "2022", "tt9114286", 505642, "Phase Four", "Multiverse Saga", 30, 30, "161 min", listOf("Action", "Adventure", "Drama"), "Wakanda grieves and defends its future after T'Challa."),
        item("ant-man-quantumania", "Ant-Man and the Wasp: Quantumania", "2023", "tt10954600", 640146, "Phase Five", "Multiverse Saga", 31, 31, "124 min", listOf("Action", "Adventure", "Comedy"), "The Ant-Man family encounters Kang in the Quantum Realm."),
        item("guardians-galaxy-vol-3", "Guardians of the Galaxy Vol. 3", "2023", "tt6791350", 447365, "Phase Five", "Multiverse Saga", 32, 32, "150 min", listOf("Action", "Adventure", "Comedy"), "The Guardians rally around Rocket's past and their future."),
        item("the-marvels", "The Marvels", "2023", "tt10676048", 609681, "Phase Five", "Multiverse Saga", 33, 33, "105 min", listOf("Action", "Adventure", "Fantasy"), "Carol Danvers, Monica Rambeau, and Kamala Khan become linked across space."),
        item("deadpool-wolverine", "Deadpool & Wolverine", "2024", "tt6263850", 533535, "Phase Five", "Multiverse Saga", 34, 34, "128 min", listOf("Action", "Comedy", "Sci-Fi"), "Deadpool and Wolverine collide in a multiverse adventure.")
    )

    val allItems: List<ViewingItem> = releaseItems

    val allLists: List<ViewingList> = buildList {
        add(ViewingList("infinity-release", "Infinity Saga Release Order", "The core Infinity Saga in theatrical release order.", phase = "Phases One–Three", saga = "Infinity Saga", franchise = "Marvel Cinematic Universe", items = releaseItems.filter { it.saga == "Infinity Saga" }))
        add(ViewingList("infinity-chronological", "Infinity Saga Chronological Order", "The Infinity Saga arranged by story timeline.", phase = "Phases One–Three", saga = "Infinity Saga", franchise = "Marvel Cinematic Universe", items = releaseItems.filter { it.saga == "Infinity Saga" }.sortedBy { it.chronologicalOrder ?: it.releaseOrder ?: Int.MAX_VALUE }))
        add(ViewingList("multiverse-release", "Multiverse Saga Release Order", "Phase Four and Five titles in release order.", phase = "Phases Four–Five", saga = "Multiverse Saga", franchise = "Marvel Cinematic Universe", items = releaseItems.filter { it.saga == "Multiverse Saga" }))
        listOf("Phase One", "Phase Two", "Phase Three", "Phase Four", "Phase Five").forEach { phase ->
            add(ViewingList(phase.lowercase().replace(" ", "-"), phase, "A focused collection for $phase.", phase = phase, franchise = "Marvel Cinematic Universe", items = releaseItems.filter { it.phase == phase }))
        }
        add(ViewingList("specials-one-shots", "Specials / One-Shots", "A reserved list for specials, shorts, and bonus timeline entries.", phase = "Specials", saga = "Curated", franchise = "Marvel Cinematic Universe", items = emptyList()))
        add(ViewingList("avengers", "Avengers Collection", "Team-up milestones and ensemble event titles.", saga = "Infinity Saga", franchise = "Marvel Cinematic Universe", items = releaseItems.filter { it.title.contains("Avengers") || it.title == "Captain America: Civil War" }))
        add(ViewingList("guardians", "Guardians Collection", "Cosmic Guardians adventures.", saga = "Infinity Saga", franchise = "Marvel Cinematic Universe", items = releaseItems.filter { it.title.contains("Guardians") }))
        add(ViewingList("spider-man", "Spider-Man Collection", "Peter Parker's MCU arc and multiverse crossover.", saga = "Infinity Saga / Multiverse Saga", franchise = "Marvel Cinematic Universe", items = releaseItems.filter { it.title.contains("Spider-Man") }))
        add(ViewingList("doctor-strange", "Doctor Strange Collection", "Mystic and multiverse-focused entries.", saga = "Multiverse Saga", franchise = "Marvel Cinematic Universe", items = releaseItems.filter { it.title.contains("Doctor Strange") }))
        add(ViewingList("black-panther", "Black Panther Collection", "Wakanda-focused stories.", saga = "Infinity Saga / Multiverse Saga", franchise = "Marvel Cinematic Universe", items = releaseItems.filter { it.title.contains("Black Panther") }))
    }

    val featuredList: ViewingList = allLists.first()
    val featuredItem: ViewingItem = releaseItems.first { it.id == "avengers-endgame" }

    fun findItem(id: String): ViewingItem? = allItems.firstOrNull { it.id == id || it.imdbId == id || it.tmdbId?.toString() == id }
    fun findList(id: String): ViewingList? = allLists.firstOrNull { it.id == id }

    fun search(query: String): Pair<List<ViewingItem>, List<ViewingList>> {
        val normalized = query.trim().lowercase()
        if (normalized.isBlank()) return allItems.take(8) to allLists.take(6)
        return allItems.filter { item ->
            listOfNotNull(item.title, item.year, item.phase, item.saga, item.franchise, item.director)
                .any { it.lowercase().contains(normalized) } ||
                item.genres.any { it.lowercase().contains(normalized) } ||
                item.actors.any { it.lowercase().contains(normalized) }
        } to allLists.filter { list ->
            listOfNotNull(list.title, list.description, list.phase, list.saga, list.franchise)
                .any { it.lowercase().contains(normalized) } ||
                list.items.any { it.title.lowercase().contains(normalized) }
        }
    }

    private fun item(
        id: String,
        title: String,
        year: String,
        imdbId: String,
        tmdbId: Int,
        phase: String,
        saga: String,
        releaseOrder: Int,
        chronologicalOrder: Int,
        runtime: String,
        genres: List<String>,
        plot: String
    ) = ViewingItem(
        id = id,
        title = title,
        year = year,
        imdbId = imdbId,
        tmdbId = tmdbId,
        phase = phase,
        saga = saga,
        franchise = "Marvel Cinematic Universe",
        studio = "Marvel Studios",
        releaseOrder = releaseOrder,
        chronologicalOrder = chronologicalOrder,
        phaseOrder = releaseOrder,
        runtime = runtime,
        genres = genres,
        plot = plot,
        overview = plot,
        trailerUrl = "https://www.youtube.com/results?search_query=${title.replace(" ", "+")}+trailer",
        trailerSource = TrailerSource.MANUAL
    )
}
