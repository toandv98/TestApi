package com.toandv.testapi;

import org.jsoup.nodes.Document;

import java.io.IOException;

public interface JsoupService {
    Document getTuitionDoc() throws IOException;

    Document getMarkDoc() throws IOException;

    Document getPractiseDoc() throws IOException;

    Document getTimetableDoc(String semester, String term) throws IOException;

    Document getExamTimetableDoc() throws IOException;
}
