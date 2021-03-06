package com.kozhevin.rootchecks.util;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.kozhevin.rootchecks.data.TotalResult;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;

import static com.kozhevin.rootchecks.constant.GeneralConst.CH_STATE_CHECKED_ERROR;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;


@RunWith(AndroidJUnit4.class)
public class CheckTaskTest {
    private final CountDownLatch mLatch;
    private final CountDownLatch mLatch1;
    private final CountDownLatch mLatch2;
    private final CountDownLatch mLatch3;
    private final CountDownLatch mLatchCanceledTask1;
    private boolean isOkOnProcessStarted = false;
    private boolean isOkOnProcessStarted1 = false;
    private CheckTask mCanceledTask;
    private CheckTask mCanceledTask1;

    public CheckTaskTest() {
        mLatch = new CountDownLatch(1);
        mLatch1 = new CountDownLatch(1);
        mLatch2 = new CountDownLatch(1);
        mLatch3 = new CountDownLatch(1);
        mLatchCanceledTask1 = new CountDownLatch(1);
    }

    @Test
    public void executeCheckTaskIsEmptyTrue() throws Exception {

        IChecksResultListener listener = new IChecksResultListener() {
            @Override
            public void onProcessStarted() {
                isOkOnProcessStarted = true;
            }

            @Override
            public void onUpdateResult(TotalResult result) {
                assertNotNull(result);
            }

            @Override
            public void onProcessFinished(TotalResult result) {
                assertNotNull(result);
                mLatch.countDown();
            }
        };
        CheckTask mTask = new CheckTask(listener, true);
        mTask.execute((Void[]) null);
        mLatch.await();
        assertThat(isOkOnProcessStarted, is(true));

    }

    @Test
    public void executeCheckTaskIsEmptyFalse() throws Exception {
        final String description = "executeCheckTaskIsEmptyFalse";
        System.out.println(description + " - started");

        IChecksResultListener listener = new IChecksResultListener() {
            @Override
            public void onProcessStarted() {
                isOkOnProcessStarted1 = true;
            }

            @Override
            public void onUpdateResult(TotalResult result) {
                assertNotNull(result);
            }

            @Override
            public void onProcessFinished(TotalResult result) {
                assertNotNull(result);
                mLatch1.countDown();
            }
        };
        CheckTask mTask = new CheckTask(listener, false);
        mTask.execute((Void[]) null);
        mLatch1.await();
        assertThat(isOkOnProcessStarted1, is(true));
        System.out.println(description + " - finished");
    }

    @Test
    public void executeCheckTaskCanceled() throws Exception {
        final String description = "executeCheckTaskCanceled";
        System.out.println(description + " - started");
        IChecksResultListener listener = new IChecksResultListener() {
            @Override
            public void onProcessStarted() {

            }

            @Override
            public void onUpdateResult(TotalResult result) {
                assertNotNull(result);
                mCanceledTask.cancel(false);
                if (mLatch2.getCount() == 1) {
                    mLatch2.countDown();
                }
            }

            @Override
            public void onProcessFinished(TotalResult result) {
                assertNotNull(result);
                if (mLatch2.getCount() == 1) {
                    mLatch2.countDown();
                }
            }
        };

        mCanceledTask = new CheckTask(listener, false);
        mCanceledTask.execute((Void[]) null);
        mLatch2.await();

        System.out.println(description + " - finished");
    }
    @Test
    public void executeCheckTaskCanceled1() throws Exception {

        IChecksResultListener listener = new IChecksResultListener() {
            @Override
            public void onProcessStarted() {

            }

            @Override
            public void onUpdateResult(TotalResult result) {
                assertNotNull(result);
                mCanceledTask1.cancel(true);
                if (mLatchCanceledTask1.getCount() == 1) {
                    mLatchCanceledTask1.countDown();
                }
            }

            @Override
            public void onProcessFinished(TotalResult result) {
                assertNotNull(result);
                if (mLatchCanceledTask1.getCount() == 1) {
                    mLatchCanceledTask1.countDown();
                }
            }
        };

        mCanceledTask1 = new CheckTask(listener, false);
        mCanceledTask1.execute((Void[]) null);
        mLatchCanceledTask1.await();


    }

    @Test
    public void executeCheckTaskLibraryIsNotLoaded() throws Exception {
        final String description = "executeCheckTaskLibraryIsNotLoaded";
        System.out.println(description + " - started");
        IChecksResultListener listener = new IChecksResultListener() {
            @Override
            public void onProcessStarted() {
            }

            @Override
            public void onUpdateResult(TotalResult result) {
                assertNotNull(result);
            }

            @Override
            public void onProcessFinished(TotalResult result) {
                assertNotNull(result);
                assertThat(result.getCheckState(), is(CH_STATE_CHECKED_ERROR));
                mLatch3.countDown();
            }
        };
        Field field = MeatGrinder.class.getDeclaredField("isUnderTest");
        field.setAccessible(true);
        field.set(null, true);
        Field field0 = MeatGrinder.class.getDeclaredField("isLoaded");
        field0.setAccessible(true);
        field0.set(null, false);
        CheckTask mTask = new CheckTask(listener, false);
        mTask.execute((Void[]) null);
        mLatch3.await();

        System.out.println(description + " - finished");

    }

}