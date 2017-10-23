package com.ray.rollertext.demo;

import android.text.TextUtils;

import org.junit.Test;
import org.w3c.dom.Text;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        boolean b = TextUtils.isDigitsOnly("1.2");
        assertEquals(b, true);
    }
}