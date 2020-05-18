package com.toandv.testapi;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.toandv.testapi.Constants.DRP_SEMESTER;
import static com.toandv.testapi.Constants.DRP_TERM;
import static com.toandv.testapi.Constants.HID_LOAI_UU_TIEN_HE_SO_HOC_PHI;
import static com.toandv.testapi.Constants.HID_STUDENT_ID;
import static com.toandv.testapi.Constants.HID_TUITION_FACTOR_MODE;
import static com.toandv.testapi.Constants.KEY_EVENT_VALIDATION;
import static com.toandv.testapi.Constants.KEY_PASSWORD;
import static com.toandv.testapi.Constants.KEY_SUBMIT;
import static com.toandv.testapi.Constants.KEY_USER_NAME;
import static com.toandv.testapi.Constants.KEY_VIEW_STATE;
import static com.toandv.testapi.Constants.KEY_VIEW_STATE_GENERATOR;
import static com.toandv.testapi.Constants.LBL_ERROR_INFO;
import static com.toandv.testapi.Constants.TIME_OUT;
import static com.toandv.testapi.Constants.URL_BASE;
import static com.toandv.testapi.Constants.URL_LOGIN;
import static com.toandv.testapi.Constants.URL_MARK;
import static com.toandv.testapi.Constants.URL_PRACTISE;
import static com.toandv.testapi.Constants.URL_TIMETABLE;
import static com.toandv.testapi.Constants.URL_TUITION;

public class JsoupServiceImp implements JsoupService {

    private Map<String, String> cookies;

    public boolean login(String username, String password) throws IOException {

        Document form = Jsoup.connect(URL_BASE + URL_LOGIN).timeout(TIME_OUT).get();

        Map<String, String> data = new HashMap<>();
        data.put(KEY_VIEW_STATE, form.getElementById(KEY_VIEW_STATE).val());
        data.put(KEY_VIEW_STATE_GENERATOR, form.getElementById(KEY_VIEW_STATE_GENERATOR).val());
        data.put(KEY_EVENT_VALIDATION, form.getElementById(KEY_EVENT_VALIDATION).val());
        data.put(KEY_USER_NAME, username);
        data.put(KEY_PASSWORD, password);
        data.put(KEY_SUBMIT, "login");

        Connection.Response response = Jsoup.connect(URL_BASE + URL_LOGIN)
                .data(data).method(Connection.Method.POST).timeout(TIME_OUT).execute();
        cookies = response.cookies();

        return response.parse().getElementById(LBL_ERROR_INFO).text().trim().length() > 0;
    }


    @Override
    public Document getTuitionDoc() throws IOException {
        return getDoc(URL_BASE + URL_TUITION);
    }

    @Override
    public Document getMarkDoc() throws IOException {
        return getDoc(URL_BASE + URL_MARK);
    }

    @Override
    public Document getPractiseDoc() throws IOException {
        return getDoc(URL_BASE + URL_PRACTISE);
    }

    @Override
    public Document getTimetableDoc(String semester, String term) throws IOException {
        Document timetableDoc = getDoc(URL_BASE + URL_TIMETABLE);

        Map<String, String> data = new HashMap<>();
        data.put(KEY_VIEW_STATE, timetableDoc.getElementById(KEY_VIEW_STATE).val());
        data.put(KEY_VIEW_STATE_GENERATOR, timetableDoc.getElementById(KEY_VIEW_STATE_GENERATOR).val());
        data.put(KEY_EVENT_VALIDATION, timetableDoc.getElementById(KEY_EVENT_VALIDATION).val());
        data.put(HID_TUITION_FACTOR_MODE, timetableDoc.getElementById(HID_TUITION_FACTOR_MODE).val());
        data.put(HID_LOAI_UU_TIEN_HE_SO_HOC_PHI, timetableDoc.getElementById(HID_LOAI_UU_TIEN_HE_SO_HOC_PHI).val());
        data.put(HID_STUDENT_ID, timetableDoc.getElementById(HID_STUDENT_ID).val());
        data.put(DRP_SEMESTER, semester);

        timetableDoc = postDoc(URL_BASE + URL_TIMETABLE, data);
        if (term.trim().length() == 0) return timetableDoc;

        data.put(KEY_VIEW_STATE, timetableDoc.getElementById(KEY_VIEW_STATE).val());
        data.put(KEY_VIEW_STATE_GENERATOR, timetableDoc.getElementById(KEY_VIEW_STATE_GENERATOR).val());
        data.put(KEY_EVENT_VALIDATION, timetableDoc.getElementById(KEY_EVENT_VALIDATION).val());
        data.put(DRP_TERM, term);

        return postDoc(URL_BASE + URL_TIMETABLE, data);
    }

    @Override
    public Document getExamTimetableDoc() throws IOException {
        return null;
    }


    private Document getDoc(String url) throws IOException {
        return noError(Jsoup.connect(url).cookies(cookies).timeout(TIME_OUT).get());
    }

    private Document postDoc(String url, Map<String, String> data) throws IOException {
        return noError(Jsoup.connect(url).cookies(cookies).data(data).timeout(TIME_OUT).post());
    }

    private Document noError(Document doc) throws HttpStatusException {
        if (doc.html().length() < 600 && doc.html().contains("Trang này không tồn tại")) {
            throw new HttpStatusException("Trang này không tồn tại", 904, doc.baseUri());
        }
        return doc;
    }
}