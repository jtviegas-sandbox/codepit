package org.aprestos.labs.snippets.impl.java8.lambdas.domain;

import java.util.ArrayList;
import java.util.List;

public abstract class MusicChapter {

    protected final List<Artist> artists;
    protected final List<Album> albums;

    public MusicChapter() {
        artists = new ArrayList<>();
        albums = new ArrayList<>();
        loadData("");
    }

    private void loadData(String file) {
        
    }

}
