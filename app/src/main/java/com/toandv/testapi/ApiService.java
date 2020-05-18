package com.toandv.testapi;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.toandv.testapi.Constants.KEY_EVENT_VALIDATION;
import static com.toandv.testapi.Constants.KEY_PASSWORD;
import static com.toandv.testapi.Constants.KEY_SUBMIT;
import static com.toandv.testapi.Constants.KEY_USER_NAME;
import static com.toandv.testapi.Constants.KEY_VIEW_STATE;
import static com.toandv.testapi.Constants.KEY_VIEW_STATE_GENERATOR;
import static com.toandv.testapi.Constants.TIME_OUT;

public class ApiService extends JobIntentService {

    private Map<String, String> cookies;

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, ApiService.class, 123, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        try {
            long f = System.currentTimeMillis();

            login("1651060663", "128569f62affe7686eb18d135b3cf8f8");

            Document tuitionDoc = getDoc("http://dangky.tlu.edu.vn/CMCSoft.IU.Web.Info/StudentService/StudentTuition.aspx");
            Document markDoc = getDoc("http://dangky.tlu.edu.vn/CMCSoft.IU.Web.info/StudentMark.aspx");
            Document practiseDoc = getDoc("http://dangky.tlu.edu.vn/CMCSoft.IU.Web.Info/StudentService/PractiseMarkAndStudyWarning.aspx");

            Document timetableDoc = getDoc("http://dangky.tlu.edu.vn/CMCSoft.IU.Web.Info/Reports/Form/StudentTimeTable.aspx");

            Map<String, String> data2 = new HashMap<>();
            data2.put(KEY_VIEW_STATE, timetableDoc.getElementById(KEY_VIEW_STATE).val());
            data2.put(KEY_VIEW_STATE_GENERATOR, timetableDoc.getElementById(KEY_VIEW_STATE_GENERATOR).val());
            data2.put(KEY_EVENT_VALIDATION, timetableDoc.getElementById(KEY_EVENT_VALIDATION).val());
            data2.put("hidTuitionFactorMode", timetableDoc.getElementById("hidTuitionFactorMode").val());
            data2.put("hidLoaiUuTienHeSoHocPhi", timetableDoc.getElementById("hidLoaiUuTienHeSoHocPhi").val());
            data2.put("hidStudentId", timetableDoc.getElementById("hidStudentId").val());
            data2.put("drpSemester", "e0ab262f1c0c48cd9d85c2c833aaa55c");

            timetableDoc = postDoc("http://dangky.tlu.edu.vn/CMCSoft.IU.Web.Info/Reports/Form/StudentTimeTable.aspx", data2);

            data2.put(KEY_VIEW_STATE, timetableDoc.getElementById(KEY_VIEW_STATE).val());
            data2.put(KEY_VIEW_STATE_GENERATOR, timetableDoc.getElementById(KEY_VIEW_STATE_GENERATOR).val());
            data2.put(KEY_EVENT_VALIDATION, timetableDoc.getElementById(KEY_EVENT_VALIDATION).val());
            data2.put("drpTerm", "9");

            timetableDoc = postDoc("http://dangky.tlu.edu.vn/CMCSoft.IU.Web.Info/Reports/Form/StudentTimeTable.aspx", data2);

            Document examDoc = getDoc("http://dangky.tlu.edu.vn/cmcsoft.iu.web.info/StudentViewExamList.aspx");

            Map<String, String> data3 = new HashMap<>();
            data3.put(KEY_VIEW_STATE, examDoc.getElementById(KEY_VIEW_STATE).val());
            data3.put(KEY_VIEW_STATE_GENERATOR, timetableDoc.getElementById(KEY_VIEW_STATE_GENERATOR).val());
            data3.put(KEY_EVENT_VALIDATION, examDoc.getElementById(KEY_EVENT_VALIDATION).val());
            data3.put("hidShowShiftEndTime", examDoc.getElementById("hidShowShiftEndTime").val());
            data3.put("hidEsShowRoomCode", examDoc.getElementById("hidEsShowRoomCode").val());
            data3.put("hidStudentId", examDoc.getElementById("hidStudentId").val());
            data3.put("drpSemester", "e0ab262f1c0c48cd9d85c2c833aaa55c");

            examDoc = postDoc("http://dangky.tlu.edu.vn/cmcsoft.iu.web.info/StudentViewExamList.aspx", data3);

            data3.put(KEY_VIEW_STATE, examDoc.getElementById(KEY_VIEW_STATE).val());
            data3.put(KEY_VIEW_STATE_GENERATOR, timetableDoc.getElementById(KEY_VIEW_STATE_GENERATOR).val());
            data3.put(KEY_EVENT_VALIDATION, examDoc.getElementById(KEY_EVENT_VALIDATION).val());
            data3.put("drpDotThi", "86c055b61794408f992870f72ca8e870");

            examDoc = postDoc("http://dangky.tlu.edu.vn/cmcsoft.iu.web.info/StudentViewExamList.aspx", data3);

            intent.putExtra("timetable_html", tuitionDoc.html());
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

            Log.d("aaaa", "Time: " + (System.currentTimeMillis() - f));
        } catch (IOException e) {
            Log.d("aaaa", "onHandleWork: " + e.getMessage());
        }
    }

    private boolean login(String username, String password) throws IOException {

        Document form = Jsoup.connect("http://dangky.tlu.edu.vn/CMCSoft.IU.Web.info/login.aspx").timeout(TIME_OUT).get();

        Map<String, String> data = new HashMap<>();
        data.put(KEY_VIEW_STATE, form.getElementById(KEY_VIEW_STATE).val());
        data.put(KEY_VIEW_STATE_GENERATOR, form.getElementById(KEY_VIEW_STATE_GENERATOR).val());
        data.put(KEY_EVENT_VALIDATION, form.getElementById(KEY_EVENT_VALIDATION).val());
        data.put(KEY_USER_NAME, username);
        data.put(KEY_PASSWORD, password);
        data.put(KEY_SUBMIT, "login");

        Connection.Response response = Jsoup.connect("http://dangky.tlu.edu.vn/CMCSoft.IU.Web.info/login.aspx")
                .data(data).method(Connection.Method.POST).timeout(TIME_OUT).execute();
        cookies = response.cookies();
        return response.parse().select("#lblErrorInfo").text().trim().length() > 0;
    }

    private Document getDoc(String url) throws IOException {
        Document result = Jsoup.connect(url).cookies(cookies).timeout(TIME_OUT).get();
        if (result.html().length() < 600 && result.html().contains("Trang này không tồn tại")) {
            throw new HttpStatusException("Trang này không tồn tại", 904, url);
        }
        return result;
    }

    private Document postDoc(String url, Map<String, String> data) throws IOException {
        Document result = Jsoup.connect(url).cookies(cookies).data(data).timeout(TIME_OUT).post();
        if (result.html().length() < 600 && result.html().contains("Trang này không tồn tại")) {
            throw new HttpStatusException("Trang này không tồn tại", 904, url);
        }
        return result;
    }
}
