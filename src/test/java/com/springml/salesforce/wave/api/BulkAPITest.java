package com.springml.salesforce.wave.api;

import static com.springml.salesforce.wave.util.WaveAPIConstants.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.springml.salesforce.wave.impl.BulkAPIImpl;
import com.springml.salesforce.wave.model.BatchInfo;
import com.springml.salesforce.wave.model.BatchInfoList;
import com.springml.salesforce.wave.model.JobInfo;
import com.springml.salesforce.wave.util.HTTPHelper;
import com.springml.salesforce.wave.util.WaveAPIConstants;

public class BulkAPITest extends BaseAPITest {
    private BulkAPI bulkAPI;

    private static final String STR_CONTACT = "Contact";
    private static final String STR_JOB_ID = "750B0000000WlhtIAC";
    private static final String STR_BATCH_ID = "751B0000000scSHIAY";
    private static final String STR_RESULT_ID1 = "752x00000004CJE";
    private static final String STR_RESULT_ID2 = "752x00000004CJF";

    private static final String BASE_JOB_URL = "https://gs0.salesforce.com/services/async/36.0/job";
    private static final String JOB_URL = "https://gs0.salesforce.com/services/async/36.0/job/" + STR_JOB_ID;
    private static final String BASE_BATCH_URL = "https://gs0.salesforce.com/services/async/36.0/job/" + STR_JOB_ID + "/batch";
    private static final String BATCH_URL = "https://gs0.salesforce.com/services/async/36.0/job/" + STR_JOB_ID + "/batch/" + STR_BATCH_ID;
    private static final String BASE_BATCH_RESULT_URL = "https://gs0.salesforce.com/services/async/36.0/job/" + STR_JOB_ID + "/batch/" + STR_BATCH_ID + "/result";
    private static final String BATCH_RESULT_URL = "https://gs0.salesforce.com/services/async/36.0/job/" + STR_JOB_ID + "/batch/" + STR_BATCH_ID + "/result/" + STR_RESULT_ID1;

    private static final String ADD_BATCH_REQUEST = "Id,Description\n003B00000067Rnx,123456\n003B00000067Rnw,7890";

    private static final String CREATE_JOB_RESPONSE = "{\"apexProcessingTime\":0,\"apiActiveProcessingTime\":0,\"apiVersion\":36.0,\"assignmentRuleId\":null,\"concurrencyMode\":\"Parallel\",\"contentType\":\"CSV\",\"createdById\":\"005B0000001LERtIAO\",\"createdDate\":\"2016-03-15T06:25:24.000+0000\",\"externalIdFieldName\":null,\"fastPathEnabled\":false,\"id\":\"750B0000000WlhtIAC\",\"numberBatchesCompleted\":0,\"numberBatchesFailed\":0,\"numberBatchesInProgress\":0,\"numberBatchesQueued\":0,\"numberBatchesTotal\":0,\"numberRecordsFailed\":0,\"numberRecordsProcessed\":0,\"numberRetries\":0,\"object\":\"Contact\",\"operation\":\"update\",\"state\":\"Open\",\"systemModstamp\":\"2016-03-15T06:25:24.000+0000\",\"totalProcessingTime\":0}";
    private static final String ADD_BATCH_RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><batchInfo xmlns=\"http://www.force.com/2009/06/asyncapi/dataload\"><id>751B0000000scSHIAY</id><jobId>750B0000000WlhtIAC</jobId><state>Queued</state><createdDate>2016-03-15T06:25:25.000Z</createdDate><systemModstamp>2016-03-15T06:25:25.000Z</systemModstamp><numberRecordsProcessed>0</numberRecordsProcessed><numberRecordsFailed>0</numberRecordsFailed><totalProcessingTime>0</totalProcessingTime><apiActiveProcessingTime>0</apiActiveProcessingTime><apexProcessingTime>0</apexProcessingTime></batchInfo>";
    private static final String CLOSE_JOB_RESPONSE = "{\"apexProcessingTime\":0,\"apiActiveProcessingTime\":29,\"apiVersion\":36.0,\"assignmentRuleId\":null,\"concurrencyMode\":\"Parallel\",\"contentType\":\"CSV\",\"createdById\":\"005B0000001LERtIAO\",\"createdDate\":\"2016-03-15T06:25:24.000+0000\",\"externalIdFieldName\":null,\"fastPathEnabled\":false,\"id\":\"750B0000000WlhtIAC\",\"numberBatchesCompleted\":1,\"numberBatchesFailed\":0,\"numberBatchesInProgress\":0,\"numberBatchesQueued\":0,\"numberBatchesTotal\":1,\"numberRecordsFailed\":0,\"numberRecordsProcessed\":2,\"numberRetries\":0,\"object\":\"Contact\",\"operation\":\"update\",\"state\":\"Closed\",\"systemModstamp\":\"2016-03-15T06:25:24.000+0000\",\"totalProcessingTime\":93}";
    private static final String GET_BATCHLIST_RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><batchInfoList xmlns=\"http://www.force.com/2009/06/asyncapi/dataload\"><batchInfo><id>751B0000000scSHIAY</id><jobId>750B0000000WlhtIAC</jobId><state>Completed</state><createdDate>2016-03-15T06:25:25.000Z</createdDate><systemModstamp>2016-03-15T06:25:26.000Z</systemModstamp><numberRecordsProcessed>2</numberRecordsProcessed><numberRecordsFailed>0</numberRecordsFailed><totalProcessingTime>93</totalProcessingTime><apiActiveProcessingTime>29</apiActiveProcessingTime><apexProcessingTime>0</apexProcessingTime></batchInfo></batchInfoList>";
    private static final String GET_BATCH_RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><batchInfo><id>751B0000000scSHIAY</id><jobId>750B0000000WlhtIAC</jobId><state>Completed</state><createdDate>2016-03-15T06:25:25.000Z</createdDate><systemModstamp>2016-03-15T06:25:26.000Z</systemModstamp><numberRecordsProcessed>2</numberRecordsProcessed><numberRecordsFailed>0</numberRecordsFailed><totalProcessingTime>93</totalProcessingTime><apiActiveProcessingTime>29</apiActiveProcessingTime><apexProcessingTime>0</apexProcessingTime></batchInfo>";
    private static final String GET_BATCH_RESULT_ID_RESPONSE = "<result-list xmlns=\"http://www.force.com/2009/06/asyncapi/dataload\"><result>752x00000004CJE</result><result>752x00000004CJF</result></result-list>";
    private static final String GET_BATCH_RESULT = "\"Id\",\"Name\"\n" +
        "\"001x000xxx4TU4JAAW\",\"name161268--1296595660659\"\n" +
        "\"001x000xxx4TU4KAAW\",\"name161269--1296595660659\"\n";

    private String getBatchPath(String jobId, String batchId) {
        StringBuilder batchPath = new StringBuilder();
        batchPath.append(getBatchPath(jobId));
        batchPath.append('/');
        batchPath.append(batchId);
        return batchPath.toString();
    }

    private String getBatchPath(String jobId) {
        StringBuilder batchPath = new StringBuilder();
        batchPath.append(getJobPath(jobId));
        batchPath.append(PATH_BATCH);

        return batchPath.toString();
    }

    private String getBatchResultPath(String jobId, String batchId) {
        StringBuilder batchResultPath = new StringBuilder();
        batchResultPath.append(getBatchPath(jobId, batchId));
        batchResultPath.append(PATH_RESULT);

        return batchResultPath.toString();
    }

    private String getBatchResultPath(String jobId, String batchId, String resultId) {
        StringBuilder batchResultPath = new StringBuilder();
        batchResultPath.append(getBatchResultPath(jobId, batchId));
        batchResultPath.append('/');
        batchResultPath.append(resultId);

        return batchResultPath.toString();
    }


    private String getJobPath(String jobId) {
        StringBuilder jobPath = new StringBuilder();
        jobPath.append(getJobPath());
        jobPath.append('/');
        jobPath.append(jobId);
        return jobPath.toString();
    }

    private String getJobPath() {
        StringBuilder jobPath = new StringBuilder();
        jobPath.append(SERVICE_ASYNC_PATH);
        jobPath.append(sfConfig.getApiVersion());
        jobPath.append(PATH_JOB);
        return jobPath.toString();
    }

    @Before
    public void setup() throws Exception {
        super.setup();

        URI baseJobURI = new URI(BASE_JOB_URL);
        URI jobURI = new URI(JOB_URL);
        URI baseBatchURI = new URI(BASE_BATCH_URL);
        URI batchURI = new URI(BATCH_URL);
        URI baseBatchResultURI = new URI(BASE_BATCH_RESULT_URL);
        URI batchResultURI = new URI(BATCH_RESULT_URL);

        when(sfConfig.getRequestURI(conn, getJobPath())).thenReturn(baseJobURI);
        when(sfConfig.getRequestURI(conn, getBatchPath(STR_JOB_ID))).thenReturn(baseBatchURI);
        when(sfConfig.getRequestURI(conn, getJobPath(STR_JOB_ID))).thenReturn(jobURI);
        when(sfConfig.getRequestURI(conn, getBatchPath(STR_JOB_ID, STR_BATCH_ID))).thenReturn(batchURI);
        when(sfConfig.getRequestURI(conn, getBatchResultPath(STR_JOB_ID, STR_BATCH_ID))).thenReturn(baseBatchResultURI);
        when(sfConfig.getRequestURI(conn, getBatchResultPath(STR_JOB_ID, STR_BATCH_ID, STR_RESULT_ID1))).thenReturn(batchResultURI);

        httpHelper = mock(HTTPHelper.class);
        when(httpHelper.post(baseBatchURI, SESSION_ID, ADD_BATCH_REQUEST,
                WaveAPIConstants.CONTENT_TYPE_TEXT_CSV, true)).thenReturn(ADD_BATCH_RESPONSE);
        when(httpHelper.get(baseBatchURI, SESSION_ID, true)).thenReturn(GET_BATCHLIST_RESPONSE);
        when(httpHelper.get(batchURI, SESSION_ID, true)).thenReturn(GET_BATCH_RESPONSE);
        when(httpHelper.get(baseBatchResultURI, SESSION_ID, true)).thenReturn(GET_BATCH_RESULT_ID_RESPONSE);
        when(httpHelper.get(batchResultURI, SESSION_ID, true)).thenReturn(GET_BATCH_RESULT);

        bulkAPI = APIFactory.getInstance().bulkAPI("dummyusername",
                "dummypassword", "https://login.salesforce.com", API_VERSION);
        ((BulkAPIImpl) bulkAPI).setHttpHelper(httpHelper);
        ((BulkAPIImpl) bulkAPI).setSfConfig(sfConfig);
        ((BulkAPIImpl) bulkAPI).setObjectMapper(objectMapper);
    }

    @Test
    @Ignore("This can be only executed with actual salesforce username and password")
    public void testBulkAPI() throws Exception {
        BulkAPI bulkAPI = APIFactory.getInstance().bulkAPI("xxx@xxx.com", "xxxx",
                "https://login.salesforce.com", API_VERSION);
        JobInfo jobInfo = bulkAPI.createJob(STR_CONTACT);
        assertEquals(STR_CONTACT, jobInfo.getObject());
        assertNotNull(jobInfo.getId());

        String jobId = jobInfo.getId();
        BatchInfo batch = bulkAPI.addBatch(jobId, ADD_BATCH_REQUEST);
        assertEquals(jobId, batch.getJobId());
        assertNotNull(batch.getId());

        JobInfo closeJob = bulkAPI.closeJob(jobId);
        assertEquals(jobId, closeJob.getId());
        assertEquals("Closed", closeJob.getState());

        // Since the batch is very small, just sleeping for 5 seconds
        Thread.sleep(5000);
        assertTrue(bulkAPI.isCompleted(jobId));
    }

    @Test
    public void testCreateJob() throws Exception {
        when(httpHelper.post(any(URI.class), anyString(), anyString(), anyBoolean(), anyList())).thenReturn(CREATE_JOB_RESPONSE);

        JobInfo jobInfo = bulkAPI.createJob(STR_CONTACT);
        assertEquals(STR_CONTACT, jobInfo.getObject());
        assertNotNull(jobInfo.getId());
        assertEquals(STR_JOB_ID, jobInfo.getId());
    }

    @Test
    public void testCreateJobWithCustomHeaders() throws Exception {
        Header customHeader = new BasicHeader("Sforce-Enable-PKChunking", "true");
        List<Header> customHeaders = new ArrayList<Header>();
        customHeaders.add(customHeader);

        when(httpHelper.post(any(URI.class), anyString(), anyString(), anyBoolean(), eq(customHeaders))).thenReturn(CREATE_JOB_RESPONSE);

        JobInfo inputJobInfo = new JobInfo(STR_CONTACT);
        JobInfo jobInfo = bulkAPI.createJob(inputJobInfo, customHeaders);
        assertEquals(STR_CONTACT, jobInfo.getObject());
        assertNotNull(jobInfo.getId());
        assertEquals(STR_JOB_ID, jobInfo.getId());
    }

    @Test
    public void testAddBatch() throws Exception {
        BatchInfo batchInfo = bulkAPI.addBatch(STR_JOB_ID, ADD_BATCH_REQUEST);
        assertNotNull(batchInfo.getId());
        assertEquals(STR_BATCH_ID, batchInfo.getId());
        assertEquals(STR_JOB_ID, batchInfo.getJobId());
    }

    @Test
    public void testCloseJob() throws Exception {
        when(httpHelper.post(any(URI.class), anyString(), anyString(), anyBoolean())).thenReturn(CLOSE_JOB_RESPONSE);

        JobInfo jobInfo = bulkAPI.closeJob(STR_JOB_ID);
        assertNotNull(jobInfo.getId());
        assertEquals(STR_JOB_ID, jobInfo.getId());
        assertEquals(WaveAPIConstants.STR_CLOSED, jobInfo.getState());
    }

    @Test
    public void testIsCompleted() throws Exception {
        assertTrue(bulkAPI.isCompleted(STR_JOB_ID));
    }

    @Test
    public void testIsCompletedFalseCase() throws Exception {
        final String GET_BATCHLIST_RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><batchInfoList xmlns=\"http://www.force.com/2009/06/asyncapi/dataload\"><batchInfo><id>751B0000000scSHIAY</id><jobId>750B0000000WlhtIAC</jobId><state>InProgress</state></batchInfo><batchInfo><id>751B0000000scSHIAZ</id><jobId>750B0000000WlhtIAC</jobId><state>Completed</state></batchInfo></batchInfoList>";
        URI baseBatchURI = new URI(BASE_BATCH_URL);
        when(httpHelper.get(baseBatchURI, SESSION_ID, true)).thenReturn(GET_BATCHLIST_RESPONSE);

        assertFalse(bulkAPI.isCompleted(STR_JOB_ID));

    }

    @Test
    public void testGetBatchInfoList() throws Exception {
        BatchInfoList batchInfoList = bulkAPI.getBatchInfoList(STR_JOB_ID);
        assertNotNull(batchInfoList);

        List<BatchInfo> batchInfos = batchInfoList.getBatchInfo();
        assertNotNull(batchInfos);
        assertTrue(!batchInfos.isEmpty());
        assertEquals(1, batchInfos.size());

        BatchInfo batchInfo = batchInfos.get(0);
        assertEquals(STR_BATCH_ID, batchInfo.getId());
        assertEquals(STR_JOB_ID, batchInfo.getJobId());
    }

    @Test
    public void testGetBatchInfo() throws Exception {
        BatchInfo batchInfo = bulkAPI.getBatchInfo(STR_JOB_ID, STR_BATCH_ID);
        assertNotNull(batchInfo);
        assertEquals(STR_BATCH_ID, batchInfo.getId());
        assertEquals(STR_JOB_ID, batchInfo.getJobId());
    }

    @Test
    public void testGetBatchResultIds() throws Exception {
        List<String> batchResultIds = bulkAPI.getBatchResultIds(STR_JOB_ID, STR_BATCH_ID);
        assertNotNull(batchResultIds);
        assertEquals(batchResultIds.get(0), STR_RESULT_ID1);
        assertEquals(batchResultIds.get(1), STR_RESULT_ID2);
    }

    @Test
    public void testGetBatchResult() throws Exception {
        String batchResult = bulkAPI.getBatchResult(STR_JOB_ID, STR_BATCH_ID, STR_RESULT_ID1);
        assertEquals(batchResult, GET_BATCH_RESULT);
    }
}
